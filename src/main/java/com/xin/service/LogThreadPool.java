package com.xin.service;

import com.xin.model.AlarmServiceRegister;
import com.xin.model.LogAlarmConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class LogThreadPool implements InitializingBean {
    private static Logger logger = LogManager.getLogger(LogThreadPool.class);

    ThreadPoolExecutor threadPoolExecutor;
    @Value("${query.threadPool.size}")
    Integer poolSize;

    private String LOCK_KEY = "alarm:service:lock";

    private String SERVICE_LIST_KEY = "alarm:service:register:list";

    private long SERVICE_EXPIRE = 90 * 1000;

    private AlarmServiceRegister localServiceRegister;

    @Resource
    RedisTemplate redisTemplate;

    public boolean add(List<LogAlarmConfig> configs, Consumer<LogAlarmConfig> task){
        //拿到有效服务数量
        int effectiveServices = getEffectiveServices();

        //如果当前score > 服务数量，重新计算当前服务的score
        if(localServiceRegister.getScore() >= effectiveServices){
            effectiveServices = registerAgain();
        }
        logger.info("有效服务数量:{}，当前分片:{}",effectiveServices,localServiceRegister.getScore());
        for(int i = 0; i < configs.size(); i++){
            int mor = i % effectiveServices;
            //取模的结果 == 本实例score，那么执行该任务
            if(mor == localServiceRegister.getScore()){
                logger.info("当前分片:{},执行config:{}",localServiceRegister.getScore(),configs.get(i).getId());
                LogAlarmConfig logAlarmConfig = configs.get(i);
                threadPoolExecutor.execute(() ->task.accept(logAlarmConfig));
            }
        }
        //向redis续约服务,重新计算当前服务的score
        int i = registerAgain();
        return true;
    }
    /**
     * 向redis中续约服务
     * @return
     */
    public int registerAgain(){
        //先加锁
        while (true){
            Boolean lock = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, "lock", 5, TimeUnit.SECONDS);
            if (lock){
                break;
            }
        }
        //拿到有效服务数量
        int effectiveServices = getEffectiveServices();

        //重新设置3分钟后过期.
        localServiceRegister.setExpire(System.currentTimeMillis() + SERVICE_EXPIRE);

        if(localServiceRegister.getScore() < effectiveServices){
            //正常续约就可以了
            redisTemplate.opsForHash().put(SERVICE_LIST_KEY,localServiceRegister.getScore()+"",localServiceRegister);
            //释放锁
            redisTemplate.delete(LOCK_KEY);
            return effectiveServices;
        }

        //score大于等于 有效数量时，
        int score = computeScore(effectiveServices);
        localServiceRegister.setScore(score);
        redisTemplate.opsForHash().put(SERVICE_LIST_KEY,score+"",localServiceRegister);
        effectiveServices = effectiveServices+1;
        //释放锁
        redisTemplate.delete(LOCK_KEY);
        return effectiveServices;
    }

    /**
     * 清理无效服务，计算可用的score
     * @return
     */
    int computeScore(int effectiveServices){
        //获取所有服务（包括失效的）清理失效key，并且取代其score
        List<AlarmServiceRegister> allServices = redisTemplate.opsForHash().values(SERVICE_LIST_KEY);
        int score = -1;
        Set<Integer> list = new HashSet<>();
        for(AlarmServiceRegister serviceRegister : allServices){
            if(serviceRegister.getExpire() < System.currentTimeMillis()){
                //服务已经过期,删掉
                redisTemplate.opsForHash().delete(SERVICE_LIST_KEY,serviceRegister.getScore()+"");
            }else {
                list.add(serviceRegister.getScore());
            }
        }
        //找一个最小的score没有被占用的，如果找不到，取最大值
        for (int i = 0; i <= effectiveServices; i++){
            if(!list.contains(i)){
                score = i;
                break;
            }
        }
        return score;
    }





    /**
     * 向redis中注册自己的服务
     * @return
     */
    public int register(){
        //先加锁
        while (true){
            Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, "lock", 5, TimeUnit.SECONDS);
            if (aBoolean){
                break;
            }
        }
        //获取到锁之后,查找现有服务总数量（包括失效的）
        List<AlarmServiceRegister> serviceRegisters = redisTemplate.opsForHash().values(SERVICE_LIST_KEY);

        localServiceRegister.setScore(serviceRegisters.size());
        localServiceRegister.setExpire(System.currentTimeMillis() + SERVICE_EXPIRE);

        //注册到列表。
        redisTemplate.opsForHash().put(SERVICE_LIST_KEY,localServiceRegister.getScore()+"",localServiceRegister);
        //释放锁
        redisTemplate.delete(LOCK_KEY);

        return serviceRegisters.size() + 1;
    }




    /**
     * 获取有效服务数量
     * @return
     */
    private int getEffectiveServices(){
        long currentTimeMillis = System.currentTimeMillis();
        //拿到服务实例数量
        List<AlarmServiceRegister> services = redisTemplate.opsForHash().values(SERVICE_LIST_KEY);
        List<AlarmServiceRegister> collect = services.stream()
                .filter(s -> s.getExpire() > currentTimeMillis)
                .collect(Collectors.toList());
        return collect.size();
    }

    @Override
    public void afterPropertiesSet(){
        localServiceRegister = new AlarmServiceRegister();
        try {
            localServiceRegister.setIpHash(InetAddress.getLocalHost().hashCode()+"");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        threadPoolExecutor = new ThreadPoolExecutor(poolSize, 2* poolSize,
                200, TimeUnit.SECONDS,new ArrayBlockingQueue<>(20),
                Executors.defaultThreadFactory(),
                (r, executor) -> logger.info("服务繁忙，应该不再向redis续约")
        );
        int register = register();
        logger.info("注册到redis成功,现存服务数量:{}",register);
    }


}

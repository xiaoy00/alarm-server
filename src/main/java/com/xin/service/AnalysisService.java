package com.xin.service;

import com.alibaba.fastjson.JSON;
import com.xin.model.AlarmModel;
import com.xin.model.LogAlarmConfig;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 分析服务，负责分析接收到的异常
 * @Author: songxiaoyue
 * @Date: 2020/4/21 19:06
 */
@Service
public class AnalysisService {


    private static Logger logger = LogManager.getLogger(AnalysisService.class);

    @Resource
    RedisTemplate redisTemplate;

    public void analysis(List<AlarmModel> list, LogAlarmConfig config) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        ValueOperations valueOperations = redisTemplate.opsForValue();

        Long frequency = config.getFrequency();
        //key 应该是配置中的id。
        String alarmKey = "alarm:count:" + config.getEnv() + ":configId:" + config.getId();
        String silentKey = "alarm:silent:" + config.getEnv() + ":configId:" + config.getId();

        final long insertNum = list.stream().filter(alarm -> {
            long logTime = alarm.getLogTime();
            return zSetOperations.add(alarmKey, alarm, logTime);
        }).count();
        logger.info("insert into redis:{},num:{}",alarmKey,insertNum);
        long currentTimeMillis = System.currentTimeMillis();

        //list全部存入redis，之后判断异常数量。
        Long count = zSetOperations.count(alarmKey, (currentTimeMillis - frequency), currentTimeMillis);
        Set<AlarmModel> range = zSetOperations.range(alarmKey, 0, count);
        if(range.size() < config.getAlarmNum()){
            return;
        }

        //异常数量未达到阀值
        //开始报警,应该有一个沉默期，否则当该方法频繁被调用，会导致不停的发送报警。默认沉默期
        Object silentPeriod = valueOperations.get(silentKey);
        //沉默期内，不触发报警
        if(silentPeriod != null){
            logger.info("沉默期内，不触发报警");
            return;
        }
        //发送报警，如果报警成功，设置沉默期
        boolean send = sendWeChat(config,range);
        if(send){
            valueOperations.set(silentKey,"",config.getSilentPeriod(), TimeUnit.MILLISECONDS);
            Long size = zSetOperations.size(alarmKey);
            zSetOperations.removeRange(alarmKey,0,size);
            logger.info("已经发送报警，清空key数量:{}",size);
        }
        logger.info("已经发送告警");
        ReentrantLock r = new ReentrantLock();
        Condition condition = r.newCondition();
        r.lock();
        try {
            condition.await();
        } catch (InterruptedException e) {

        }
        condition.signal();
    }




    /**
     *
     * @return
     */
    boolean sendWeChat(LogAlarmConfig config,Set<AlarmModel> list){
        String msg4= list.stream().map(alarmModel -> {
            return alarmModel.getKey();
        }).collect(Collectors.joining(";"));
        Map<String,String> params = new HashMap<>();
        params.put("userName",config.getReceiver());
        params.put("msg1","["+config.getEnv()+"]"+config.getSysName()+"异常");
        params.put("msg2",config.getSendLabel());
        params.put("msg3","异常次数达到阀值:"+config.getAlarmNum()+"\n 实际发生数量:"+list.size());
        params.put("msg4",msg4);

        logger.info("发送微信消息:"+config.getSendLabel()+":"+config.getReceiver());
        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod("就一个地址");
        try {
            RequestEntity entity = new StringRequestEntity(JSON.toJSONString(params), "application/json; encoding=utf-8", "utf-8");
            post.setRequestEntity(entity);
            httpClient.executeMethod(post);
            String html = post.getResponseBodyAsString();
            return html != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}

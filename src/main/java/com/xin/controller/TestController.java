package com.xin.controller;

import com.xin.model.AlarmServiceRegister;
import com.xin.service.LogService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import javax.annotation.Resource;
import java.io.IOException;

import java.util.List;


@RestController
@RequestMapping("/monitor")
public class TestController {

    @Resource
    private LogService logService;

    @Autowired
    RedisTemplate redisTemplate;
    /**
     * 接受skywalking发来的请求。
     * 需要转换告警信息。
     * @param name
     * @return
     */
    @RequestMapping("/alarm")
    public String alarm(String name) {

        return name+"hello";
    }

    /**
     * 接受skywalking发来的请求。
     * 需要转换告警信息。
     * @param name
     * @return
     */
    @RequestMapping("/test")
    public String test(String name) {
        logService.queryLog(1);
        return name+"hello";
    }

    @RequestMapping("/clear")
    public String send(String key) throws IOException {
        Boolean delete = redisTemplate.delete("alarm:service:register:list");
        return delete+"";
    }
    @RequestMapping("/add")
    public Object add(Integer score){
        AlarmServiceRegister register = new AlarmServiceRegister(score,
                System.currentTimeMillis(),"asdasd");

        //注册到列表。
        redisTemplate.opsForHash().put("alarm:service:register:list",register.getScore()+"",register);
        return true;
    }
    @RequestMapping("/all")
    public Object all(){

        List<AlarmServiceRegister> values = redisTemplate.opsForHash().values("alarm:service:register:list");
        return values;
    }
}

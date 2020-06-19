package com.xin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.xin.dao")
@EnableConfigurationProperties
@EnableScheduling
public class AlarmServerApp {
    public static void main(String[] args) {
        SpringApplication.run(AlarmServerApp.class, args);
    }
}

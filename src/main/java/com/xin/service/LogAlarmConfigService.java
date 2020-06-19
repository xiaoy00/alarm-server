package com.xin.service;

import com.xin.dao.LogAlarmConfigMapper;
import com.xin.model.LogAlarmConfig;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.List;

@Configuration
public class LogAlarmConfigService {


    String DEFAULT_LOG_ACCESS_ADDRESS = "http://alilog.xin.com";

    Integer DEFAULT_CONDITION_TYPE = 1;

    Long DEFAULT_FREQUENCY = 1 * 60 * 1000L;

    Long DEFAULT_SILENT_PERIOD = 1 * 60 * 1000L;

    Integer DEFAULT_ALARM_NUM = 2;

    @Resource
    LogAlarmConfigMapper logAlarmConfigMapper;

    public List<LogAlarmConfig> getConfig() {
        List<LogAlarmConfig> logAlarmConfigs = logAlarmConfigMapper.selectAllConfig();

        logAlarmConfigs.forEach(config -> {
            if(config.getLogAccessAddress() == null){
                config.setLogAccessAddress(DEFAULT_LOG_ACCESS_ADDRESS);
            }
            if(config.getConditionType() == null){
                config.setConditionType(DEFAULT_CONDITION_TYPE);
            }
            if(config.getFrequency() == null){
                config.setFrequency(DEFAULT_FREQUENCY);
            }
            if(config.getAlarmNum() == null){
                config.setAlarmNum(DEFAULT_ALARM_NUM);
            }
            if(config.getSilentPeriod() == null){
                config.setSilentPeriod(DEFAULT_SILENT_PERIOD);
            }
        });
        return logAlarmConfigs;
    }




}

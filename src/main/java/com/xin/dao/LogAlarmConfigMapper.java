package com.xin.dao;

import com.xin.model.LogAlarmConfig;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogAlarmConfigMapper {

    @Select("select id,env,sys_name sysName,log_name logName,condition_type conditionType," +
            "keyword,receiver,alarm_num alarmNum,log_address logAccessAddress,log_index logIndex," +
            "frequency,silent_period,send_label sendLabel from uxin_alarm_config")
    List<LogAlarmConfig> selectAllConfig();

}

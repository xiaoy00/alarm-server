package com.xin.task;

import com.xin.service.LogService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 * @author songxiaoyue
 */
@Component
public class LogAlarmTask /*implements SchedulingConfigurer*/{

    private static Logger logger = LogManager.getLogger(LogAlarmTask.class);
    @Autowired
    LogService logService;

    /**
     *  每分钟查询一次elk
     */
    @Scheduled(cron="0 0/1 * * * ?")
    public void sendAlarmOne() {
       try {
           long start = System.currentTimeMillis();
           logger.info("【日志报警定时任务】--开始");
           logService.queryLog(1);
           logger.info("【日志报警定时任务】--结束,共耗时:{} ms",(System.currentTimeMillis()-start));
       }catch (Exception e){
           logger.error("【日志报警定时任务】--异常：{}",e);
       }
    }

}

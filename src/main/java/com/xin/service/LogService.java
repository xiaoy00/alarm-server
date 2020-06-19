package com.xin.service;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xin.model.AlarmModel;
import com.xin.model.LogAlarmConfig;
import com.xin.util.AlilogUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogService {


    private static Logger logger = LogManager.getLogger(LogService.class);

    ZoneOffset of = ZoneOffset.of("+0");
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @Resource
    AnalysisService analysisService;

    @Resource
    LogAlarmConfigService logAlarmConfigService;
    @Resource
    AlilogUtils alilogUtils;
    @Resource
    LogThreadPool logThreadPool;

    public void queryLog(Integer key) {
        //获取所有配置列表
        List<LogAlarmConfig> logAlarmList = logAlarmConfigService.getConfig();
        logger.info("日志报警配置列表:{}",logAlarmList);
        //提交到负载均衡
        logThreadPool.add(logAlarmList, this::test);
    }

    /**
     *
     * @param config
     */
    private void test(LogAlarmConfig config){
        //调用log.xin.com
        String log = getLogXinInfo(config, config.getLogAccessAddress(), config.getLogIndex());
        //解析结果
        List<AlarmModel> alarmModels = parse(log,config);
        //将结果发给分析服务
        analysisService.analysis(alarmModels,config);

    }


    /**
     * 解析log
     * @param log
     * @return
     */
    private List<AlarmModel> parse(String log,LogAlarmConfig config) {
        JSONObject object = JSONObject.parseObject(log);
        JSONArray jsonArray = object.getJSONArray("responses");
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        JSONObject hitsObject = jsonObject.getJSONObject("hits");
        JSONArray hitsArray = hitsObject.getJSONArray("hits");
        List<AlarmModel> alarmModels = hitsArray.stream().map(h -> {
            JSONObject hit = (JSONObject) h;
            JSONObject source =  hit.getJSONObject("_source");
            String key = source.getString("pid");
            String env = source.getString("env");
            String serviceName = source.getString("serviceName");
            JSONObject fields = source.getJSONObject("fields");

            String timestamp= source.getString("@timestamp");
            long time = LocalDateTime.parse(timestamp,dateTimeFormatter).toInstant(of).toEpochMilli();
            String hostip = fields.getString("hostip");

            AlarmModel alarmModel = new AlarmModel();
            alarmModel.setKey(key);
            alarmModel.setEnv(env);
            alarmModel.setBusinessName(serviceName);
            alarmModel.setSysName(config.getSysName());
            alarmModel.setClientIp(hostip);
            alarmModel.setError(1);
            alarmModel.setLogTime(time);
            alarmModel.setId(hit.getString("_id"));
            return alarmModel;
        }).collect(Collectors.toList());
        return alarmModels;
    }


    /**
     * 拼接查询条件
     * 获取日志
     */
    private String getLogXinInfo(LogAlarmConfig logAlarmConfig, String url, String logIndexTypeName){
        String result = "" ;
        try{
            //查询范围，当前时间的上2分钟,, 先用5分钟测试
            long endDate = System.currentTimeMillis();
            long startDate = endDate - 30*60*1000;

            //拼接条件
            StringBuilder queryCon = new StringBuilder();


            url = url+"/elasticsearch/_msearch";

            queryCon.append("{\"index\":[\"").append(logIndexTypeName).append("\"],\"ignore_unavailable\":true,\"preference\":"+endDate+"}\n");
            queryCon.append("{\"size\":10,\"sort\":[{\"@timestamp\":{\"order\":\"desc\",\"unmapped_type\":\"boolean\"}}],\"query\":{\"bool\":{\"must\":[");

            queryCon.append("{\"query_string\":{\"query\":\"");
            if(!StringUtils.isEmpty(logAlarmConfig.getLogName())){
                queryCon.append("\\\"").append(logAlarmConfig.getLogName()).append("\\\" ");
            }
            if(!StringUtils.isEmpty(logAlarmConfig.getEnv())){
                queryCon.append(" AND env:\\\"").append(logAlarmConfig.getEnv()).append("\\\" ");
            }
            if(logAlarmConfig.getConditionType()==1){
                //获取查询条件
                String[] keyWordArry = logAlarmConfig.getKeyWord().split(";");
                for (String keyStr : keyWordArry) {
                    queryCon.append(" AND \\\"").append(keyStr).append("\\\"");
                }
            }
            // TODO: 2020/5/15   自定义查询条件待测试
            if(logAlarmConfig.getConditionType()==2){
                //获取查询条件
                if(!StringUtils.isEmpty(logAlarmConfig.getKeyWord())){
                    queryCon.append(" AND ").append(logAlarmConfig.getKeyWord().replaceAll("\"","\\\\\""));
                }
            }
            queryCon.append("\",\"analyze_wildcard\":true,\"default_field\":\"*\"}}");

            queryCon.append(",{\"range\":{\"@timestamp\":{\"gte\":").append(startDate).append(",\"lte\":").append(endDate).append(",\"format\":\"epoch_millis\"}}}],\"filter\":[],\"should\":[],\"must_not\":[]}}}\n");
            result = alilogUtils.post(queryCon.toString(),url);
        }catch (Exception e){
            logger.error("【日志报警】--根据条件查询日志异常:",e);
        }
        return result;
    }





}

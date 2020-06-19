package com.xin.model;

/**
 * @Author: songxiaoyue
 * @Date: 2020/4/21 18:57
 */
public class AlarmModel {

    /**
     *   系统名称
     */
    String sysName;

    /**
     * 主键，反查用
     */
    String key;
    /**
     *     //业务类型
     */
    String businessName;
    /**
     *     //总耗时
     */
    Integer costTime;
    /**
     *     //是否异常  1:异常
     */
    Integer Error;
    /**
     *     //异常描述
     */
    String ErrorMsg ;
    /**
     *     //客户端ip
     */
    String ClientIp;
    /**
     *     //记录时间，如 2018-02-23 10:29:00
     */
    Long LogTime;
    /**
     * 环境
     */
    String env;
    /**
     * 日志id
     */
    String id;

    public String getSysName() {
        return sysName;
    }

    public void setSysName(String sysName) {
        this.sysName = sysName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public Integer getCostTime() {
        return costTime;
    }

    public void setCostTime(Integer costTime) {
        this.costTime = costTime;
    }

    public Integer getError() {
        return Error;
    }

    public void setError(Integer error) {
        Error = error;
    }

    public String getErrorMsg() {
        return ErrorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        ErrorMsg = errorMsg;
    }

    public String getClientIp() {
        return ClientIp;
    }

    public void setClientIp(String clientIp) {
        ClientIp = clientIp;
    }

    public Long getLogTime() {
        return LogTime;
    }

    public void setLogTime(Long logTime) {
        LogTime = logTime;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "AlarmModel{" +
                "sysName='" + sysName + '\'' +
                ", key='" + key + '\'' +
                ", businessName='" + businessName + '\'' +
                ", costTime=" + costTime +
                ", Error=" + Error +
                ", ErrorMsg='" + ErrorMsg + '\'' +
                ", ClientIp='" + ClientIp + '\'' +
                ", LogTime=" + LogTime +
                ", env='" + env + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}

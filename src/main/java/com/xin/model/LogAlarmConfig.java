package com.xin.model;

import java.io.Serializable;

public class LogAlarmConfig implements Serializable{

    private Integer id; //主键
    private String env;//环境
    private String sysName; //系统名称
    private String logName; //日志来源
    private Integer conditionType;//条件类型 1关键词 2自定义条件
    private String keyWord; //关键词
    private String receiver; //收件人
    private Integer alarmNum; //报警个数
    private String logAccessAddress; //日志访问地址
    private String logIndex; //日志索引id
    private Long frequency;//监控频率
    private Long silentPeriod;//沉默期，不可配置
    private String sendLabel;//发送内容的标签



    public Integer getConditionType() {
        return conditionType;
    }

    public void setConditionType(Integer conditionType) {
        this.conditionType = conditionType;
    }

    public Long getFrequency() {
        return frequency;
    }

    public void setFrequency(Long frequency) {
        this.frequency = frequency;
    }

    public String getSysName() {
        return sysName;
    }

    public void setSysName(String sysName) {
        this.sysName = sysName;
    }

    public Integer getAlarmNum() {
        return alarmNum;
    }

    public void setAlarmNum(Integer alarmNum) {
        this.alarmNum = alarmNum;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLogName() {
        return logName;
    }

    public void setLogName(String logName) {
        this.logName = logName;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }


    public String getLogAccessAddress() {
        return logAccessAddress;
    }

    public void setLogAccessAddress(String logAccessAddress) {
        this.logAccessAddress = logAccessAddress;
    }

    public String getLogIndex() {
        return logIndex;
    }

    public void setLogIndex(String logIndex) {
        this.logIndex = logIndex;
    }

    public String getSendLabel() {
        return sendLabel;
    }

    public void setSendLabel(String sendLabel) {
        this.sendLabel = sendLabel;
    }

    public Long getSilentPeriod() {
        return silentPeriod;
    }

    public void setSilentPeriod(Long silentPeriod) {
        this.silentPeriod = silentPeriod;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    @Override
    public String toString() {
        return "LogAlarmConfig{" +
                "id=" + id +
                ", logName='" + logName + '\'' +
                ", keyWord='" + keyWord + '\'' +
                ", conditionType=" + conditionType +
                ", receiver='" + receiver + '\'' +
                ", alarmNum=" + alarmNum +
                ", sysName='" + sysName + '\'' +
                ", logAccessAddress='" + logAccessAddress + '\'' +
                ", logIndex='" + logIndex + '\'' +
                ", frequency=" + frequency +
                ", sendLabel='" + sendLabel + '\'' +
                ", silentPeriod=" + silentPeriod +
                ", env='" + env + '\'' +
                '}';
    }
}

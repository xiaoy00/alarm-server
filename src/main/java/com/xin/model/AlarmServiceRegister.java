package com.xin.model;


import java.io.Serializable;

public class AlarmServiceRegister {

    private Integer score;

    private Long expire;

    private String ipHash;

    public AlarmServiceRegister() {
    }

    public AlarmServiceRegister(Integer score, Long expire, String ipHash) {
        this.score = score;
        this.expire = expire;
        this.ipHash = ipHash;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Long getExpire() {
        return expire;
    }

    public void setExpire(Long expire) {
        this.expire = expire;
    }

    public String getIpHash() {
        return ipHash;
    }

    public void setIpHash(String ipHash) {
        this.ipHash = ipHash;
    }
}

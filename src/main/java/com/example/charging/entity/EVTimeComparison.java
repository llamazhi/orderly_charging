package com.example.charging.entity;

import java.math.BigDecimal;

public class EVTimeComparison {
    private String uid;
    private BigDecimal oldStartTime;
    private BigDecimal oldEndTime;
    private BigDecimal newStartTime;
    private BigDecimal newEndTime;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public BigDecimal getOldStartTime() {
        return oldStartTime;
    }

    public void setOldStartTime(BigDecimal oldStartTime) {
        this.oldStartTime = oldStartTime;
    }

    public BigDecimal getOldEndTime() {
        return oldEndTime;
    }

    public void setOldEndTime(BigDecimal oldEndTime) {
        this.oldEndTime = oldEndTime;
    }

    public BigDecimal getNewStartTime() {
        return newStartTime;
    }

    public void setNewStartTime(BigDecimal newStartTime) {
        this.newStartTime = newStartTime;
    }

    public BigDecimal getNewEndTime() {
        return newEndTime;
    }

    public void setNewEndTime(BigDecimal newEndTime) {
        this.newEndTime = newEndTime;
    }



    @Override
    public String toString() {
        return "AlgorithmComparison{" +
                "id='" + uid + '\'' +
                ", oldStartTime=" + oldStartTime +
                ", oldEndTime=" + oldEndTime +
                ", newStartTime=" + newStartTime +
                ", newEndTime=" + newEndTime +
                '}';
    }
}

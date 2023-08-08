package com.example.charging.entity;

import java.math.BigDecimal;

public class EVTimeComparison {
    private String id;
    private BigDecimal oldStartTime;
    private BigDecimal oldEndTime;
    private BigDecimal newStartTime;
    private BigDecimal newEndTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
                "id='" + id + '\'' +
                ", oldStartTime=" + oldStartTime +
                ", oldEndTime=" + oldEndTime +
                ", newStartTime=" + newStartTime +
                ", newEndTime=" + newEndTime +
                '}';
    }
}
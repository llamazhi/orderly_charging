package com.example.charging.entity;
import java.sql.Time;

// 此类存有一辆电动汽车的返回与离开时间信息
public class EVTimeComparison {
    private String uid;
    private String evid;
    private Time oldStartTime;
    private Time oldEndTime;
    private Time newStartTime;
    private Time newEndTime;

    private Time leavingTime;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Time getOldStartTime() {
        return oldStartTime;
    }

    public String getEvid() {
        return evid;
    }

    public void setEvid(String evid) {
        this.evid = evid;
    }

    public void setOldStartTime(Time oldStartTime) {
        this.oldStartTime = oldStartTime;
    }

    public Time getOldEndTime() {
        return oldEndTime;
    }

    public void setOldEndTime(Time oldEndTime) {
        this.oldEndTime = oldEndTime;
    }

    public Time getNewStartTime() {
        return newStartTime;
    }

    public void setNewStartTime(Time newStartTime) {
        this.newStartTime = newStartTime;
    }

    public Time getNewEndTime() {
        return newEndTime;
    }

    public void setNewEndTime(Time newEndTime) {
        this.newEndTime = newEndTime;
    }

    public Time getLeavingTime() {
        return leavingTime;
    }

    public void setLeavingTime(Time leavingTime) {
        this.leavingTime = leavingTime;
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

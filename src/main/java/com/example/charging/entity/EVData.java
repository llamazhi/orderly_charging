package com.example.charging.entity;

import java.io.Serializable;
import java.util.UUID;

// 此类存有一辆新能源车辆的电池信息以及返回及离开时间
public class EVData implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID uuid = UUID.randomUUID();
    private double remainingSOC;
    private double returningTime;
    private double leavingTime;
    private double chargingTime;
    private double chargingStartTime;
    private double chargingEndTime;
    private int useFastCharging;
    private String modelName;
    private double chargingPower;
    private double maxSOC;

    private boolean ifAcceptOrderlyCharging;
    private boolean isAlreadyOptimized;

    public EVData() {

    }
    public EVData(String modelName) {
        this.modelName = modelName;
    }

    public double getRemainingSOC() {
        return remainingSOC;
    }

    public void setRemainingSOC(double remainingSOC) {
        this.remainingSOC = remainingSOC;
    }

    public double getReturningTime() {
        return returningTime;
    }

    public void setReturningTime(double returningTime) {
        this.returningTime = returningTime;
    }

    public double getLeavingTime() {
        return leavingTime;
    }

    public void setLeavingTime(double leavingTime) {
        this.leavingTime = leavingTime;
    }

    public double getChargingTime() {
        return chargingTime;
    }

    public void setChargingTime(double chargingTime) {
        this.chargingTime = chargingTime;
    }

    public double getChargingEndTime() {
        return chargingEndTime;
    }

    public void setChargingEndTime(double chargingEndTime) {
        this.chargingEndTime = chargingEndTime;
    }

    public int getUseFastCharging() {
        return useFastCharging;
    }

    public void setUseFastCharging(int useFastCharging) {
        this.useFastCharging = useFastCharging;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public double getChargingPower() {
        return chargingPower;
    }

    public void setChargingPower(double chargingPower) {
        this.chargingPower = chargingPower;
    }

    public double getMaxSOC() {
        return maxSOC;
    }

    public void setMaxSOC(double maxSOC) {
        this.maxSOC = maxSOC;
    }

    public double getChargingStartTime() {
        return chargingStartTime;
    }

    public void setChargingStartTime(double chargingStartTime) {
        this.chargingStartTime = chargingStartTime;
    }

    public boolean isIfAcceptOrderlyCharging() {
        return ifAcceptOrderlyCharging;
    }

    public void setIfAcceptOrderlyCharging(boolean ifAcceptOrderlyCharging) {
        this.ifAcceptOrderlyCharging = ifAcceptOrderlyCharging;
    }

    public boolean isAlreadyOptimized() {
        return isAlreadyOptimized;
    }

    public void setAlreadyOptimized(boolean alreadyOptimized) {
        isAlreadyOptimized = alreadyOptimized;
    }

    public UUID getUuid() {
        return uuid;
    }
}

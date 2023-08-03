public class EVData {
    private double remainingSOC;
    private double returningTime;
    private double leavingTime;
    private double chargingTime;
    private double chargingEndTime;

    private int useFastCharging;
    private String modelName;

    private double chargingPower;

    private double maxSOC;

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
}

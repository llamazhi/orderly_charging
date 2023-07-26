/* This class is to simulate possible parameters a typical residential community can have
 * 此 Class 将模拟一个小区可能有的参数 */

import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


public class ResidentialAreaData {
    /* Necessary parameters
     必要参数 */

    private double EVPenetrationRate; // 电动汽车渗透率
    private double[] travelDistances; // 出行里程
    private double[] startingSOCs; // 起始SOC
    private Date[] returningTime; // 返回时刻
    private Date[] leavingTime; // 离开时刻
    private double[] chargingTime; // 充电时长
    private String[] batteryTypes; // 电池类型
    private double transformerCapacity; // 小区变压器功率上限
    private int loopNumber; // 蒙特卡洛法循环次数

    private int vehicleNumber; // 车辆数量

    private static final Logger logger = LogManager.getLogger(ResidentialAreaData.class);

    public ResidentialAreaData(double EVPenetrationRate, double[] travelDistances, double[] startingSOCs,
                               Date[] returningTime, Date[] leavingTime, double[] chargingTime,
                               String[] batteryTypes, double transformerCapacity, int loopNumber, int vehicleNumber) {
        this.EVPenetrationRate = EVPenetrationRate;
        this.travelDistances = travelDistances;
        this.startingSOCs = startingSOCs;
        this.returningTime = returningTime;
        this.leavingTime = leavingTime;
        this.chargingTime = chargingTime;
        this.batteryTypes = batteryTypes;
        this.transformerCapacity = transformerCapacity;
        this.loopNumber = loopNumber;
        this.vehicleNumber = vehicleNumber;
    }

    public double getEVPenetrationRate() {
        return EVPenetrationRate;
    }

    public double[] getTravelDistances() {
        return travelDistances;
    }

    public double[] getStartingSOCs() {
        return startingSOCs;
    }

    public Date[] getReturningTime() {
        return returningTime;
    }

    public Date[] getLeavingTime() {
        return leavingTime;
    }

    public double[] getChargingTime() {
        return chargingTime;
    }

    public String[] getBatteryTypes() {
        return batteryTypes;
    }

    public double getTransformerCapacity() {
        return transformerCapacity;
    }

    public int getLoopNumber() {
        return loopNumber;
    }

    public int getVehicleNumber() {
        return vehicleNumber;
    }

    public void setEVPenetrationRate(double EVPenetrationRate) {
        this.EVPenetrationRate = EVPenetrationRate;
    }

    public void setTravelDistances(double[] travelDistances) {
        this.travelDistances = travelDistances;
    }

    public void setStartingSOCs(double[] startingSOCs) {
        this.startingSOCs = startingSOCs;
    }

    public void setReturningTime(Date[] returningTime) {
        this.returningTime = returningTime;
    }

    public void setLeavingTime(Date[] leavingTime) {
        this.leavingTime = leavingTime;
    }

    public void setChargingTime(double[] chargingTime) {
        this.chargingTime = chargingTime;
    }

    public void setBatteryTypes(String[] batteryTypes) {
        this.batteryTypes = batteryTypes;
    }

    public void setTransformerCapacity(double transformerCapacity) {
        this.transformerCapacity = transformerCapacity;
    }

    public void setLoopNumber(int loopNumber) {
        this.loopNumber = loopNumber;
    }

    public void setVehicleNumber(int vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }
}


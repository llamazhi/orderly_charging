/* This class aims to simulate how an orderly charging method would affect a residential area's power load capacity
 * 此 Class 致力于模拟有序充电如何影响一个居民小区的电力负荷
 *  Also, the orderlyCharging would extend from the NormalCharging
 *  同时，我们将从 NormalCharging 继承共有的参数
 */

import org.apache.commons.math3.special.Gamma;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.io.IOException;

import com.opencsv.*;

import java.util.*;

public class OrderlyCharging extends NormalCharging {
    private static final Logger logger = LogManager.getLogger(OrderlyCharging.class);
    private final Utils utils;
    private List<EVData> EVDatabase;

    // initialize with EV specs
    // 初始化电动汽车参数
    // Use the data of top 5 sales of EV models in 2023.6
    // 使用2023 6 月销售前五的电动汽车数据
    public OrderlyCharging() {
        this.initializeEVModels();
        this.setMarketPermeability(0.3);
        this.setTotalVehicleNumber(400);
        this.utils = new Utils();
        this.EVDatabase = new ArrayList<>();
    }

    public void exportTimeSlotData() {
        int loop = 1000;
        List<String[]> timeToAvgChargingPower = this.multipleSimulateMCM(loop);
        List<String[]> timeToDailyLoad = this.simulateResidentialDailyPowerLoad();

        // 将所有参数统一放到一个表内
        List<String[]> timeToAllParams = new ArrayList<>();
        for (int i = 0; i < 96; i++) {
            String time = timeToAvgChargingPower.get(i)[0];
            String dailyLoad = timeToDailyLoad.get(i)[1];
            String[] params = new String[]{time, dailyLoad};
            timeToAllParams.add(params);
        }

        this.utils.writeToNewCSV("./src/data/timeToAllParams.csv", timeToAllParams,
                new String[]{"Time", "Daily_Load"});
    }

    public void exportEVData() {
        List<EVData> EVDatabase = this.simulateEVDatabase();
        List<String[]> EVList = new ArrayList<>();

//        int count = 0;
        // need: maxSOC, remainingSOC, chargingPower, returningTime, leavingTime
        for (EVData ev : EVDatabase) {
            String maxSOC = this.utils.limitToThreeDecimal(ev.getMaxSOC());
            String remainingSOC = this.utils.limitToThreeDecimal(ev.getRemainingSOC());
            String chargingPower = String.valueOf(ev.getChargingPower());
            String returningTime = this.utils.limitToThreeDecimal(ev.getReturningTime());
            String leavingTime = this.utils.limitToThreeDecimal(ev.getLeavingTime());
            String chargingTime = this.utils.limitToThreeDecimal(ev.getChargingTime());
            EVList.add(new String[]{maxSOC, remainingSOC, chargingPower, returningTime, leavingTime, chargingTime});
        }
        this.utils.writeToNewCSV("./src/data/EVDatabase.csv", EVList,
                new String[]{"Max_SOC, Remaining_SOC, Charging_Power", "Returning_Time", "Leaving_Time", "Charging_Time"});
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        OrderlyCharging oc = new OrderlyCharging();
        oc.exportEVData();
        oc.exportTimeSlotData();
        long endTime = System.nanoTime();
        long elapsedTime = (endTime - startTime) / 1000000;
        logger.info("elapsed time: " + elapsedTime + "ms");
    }
}

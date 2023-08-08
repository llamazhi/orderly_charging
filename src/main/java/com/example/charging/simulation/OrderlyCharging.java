package com.example.charging.simulation;/* This class aims to simulate how an orderly charging method would affect a residential area's power load capacity
 * 此 Class 致力于模拟有序充电如何影响一个居民小区的电力负荷
 *  Also, the orderlyCharging would extend from the com.example.charging.simulation.NormalCharging
 *  同时，我们将从 com.example.charging.simulation.NormalCharging 继承共有的参数
 */

import com.example.charging.entity.EVTimeComparison;
import com.example.charging.entity.LoadComparison;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.moeaframework.Executor;
import org.moeaframework.core.*;
import org.moeaframework.core.variable.EncodingUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import com.example.charging.utils.Utils;


public class OrderlyCharging extends NormalCharging {
    private static final Logger logger = LogManager.getLogger(OrderlyCharging.class);
    private final Utils utils;
    private List<EVData> EVDatabase;
    private List<String[]> timeToDailyLoad;
    private List<String[]> timeToChargingLoad;
    private List<double[]> timeToTotalLoad;
    private List<double[]> timeToLoadList;
    private List<double[]> EVList;
    private double[] newChargingStartTime;
    private int EVNum;
    private static final int TIME_NUMS = 96; // 24h/day, in every 15 min

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

    // 返回： 模拟所得用电负荷参数
    public void setTimeToLoadList(List<String[]> timeToDailyLoad, List<String[]> timeToAvgChargingLoad) {
        // 将所有参数统一放到一个表内
        List<double[]> timeToDaily = new ArrayList<>();
        for (int i = 0; i < 96; i++) {
            String time = timeToAvgChargingLoad.get(i)[0];
            String dailyLoad = timeToDailyLoad.get(i)[1];
            String avgChargingLoad = timeToAvgChargingLoad.get(i)[1];
//            String[] params = new String[]{time, dailyLoad, avgChargingLoad};
            double[] params = new double[]{Double.parseDouble(time), Double.parseDouble(dailyLoad),
            Double.parseDouble(avgChargingLoad)};
            timeToDaily.add(params);
        }
        this.setTimeToDailyLoad(timeToDailyLoad);
        this.setTimeToChargingLoad(timeToAvgChargingLoad);
        this.setTimeToLoadList(timeToDaily);
//        return timeToDaily;
    }

    // 返回： 模拟所得电动汽车参数
    public void setEVDataList(List<EVData> data) {
        this.setEVDatabase(this.simulateEVDatabase());
        this.setEVNum(this.getEVDatabase().size());
        List<double[]> EVList = new ArrayList<>();

        // params: maxSOC, remainingSOC, chargingPower, returningTime, leavingTime, chargingTime, endTime
        for (EVData ev : data) {
            double maxSOC = ev.getMaxSOC();
            double remainingSOC = ev.getRemainingSOC();
            double chargingPower = ev.getChargingPower();
            double returningTime = ev.getReturningTime();
            double leavingTime = ev.getLeavingTime();
            double chargingTime = ev.getChargingTime();
            double endTime = ev.getChargingEndTime();
            EVList.add(new double[]{maxSOC, remainingSOC, chargingPower, returningTime, leavingTime,
                    chargingTime, endTime});
        }
        this.setEVList(EVList);
//        return EVList;
    }

    public List<String[]> updateOptimizedSolution() {
        NondominatedPopulation result = new Executor()
                .withProblemClass(ChargingStrategy.class, this.getTimeToLoadList(),
                        this.getEVList(), this.getEVNum(), TIME_NUMS)
                .withAlgorithm("OMOPSO")
                .withMaxEvaluations(100000)
                .distributeOnAllCores()
                .run();

        // 所得solutions是优化过的电动汽车开始充电时间分布
        Solution solution = result.get(0);
        double[] solutions = EncodingUtils.getReal(solution);
        this.setNewChargingStartTime(solutions);

        this.timeToChargingLoad = new ArrayList<>();
        for (double time = 0; time < 24; time += 0.25) {
            double currPower = 0;
            for (int j = 0; j < this.EVDatabase.size(); j++) {
                EVData ev = this.EVDatabase.get(j);
                double newEndTime = solutions[j] + ev.getChargingTime();
                if (newEndTime > 24) {
                    newEndTime -= 24;
                }
                if (time >= solutions[j] && time <= newEndTime) {
                    currPower += ev.getChargingPower();
                }
            }
            this.timeToChargingLoad.add(new String[]{String.valueOf(time), String.valueOf(currPower)});
        }
        return this.timeToChargingLoad;
    }

    public void setTimeToTotalLoad() {
        List<double[]> totalLoad = new ArrayList<>();
        for (int i = 0; i < this.timeToDailyLoad.size(); i++) {
            double time = Double.parseDouble(timeToDailyLoad.get(i)[0]);
            double daily = Double.parseDouble(this.timeToDailyLoad.get(i)[1]);
            double charging = Double.parseDouble(this.timeToChargingLoad.get(i)[1]);
            double total = daily + charging;
            totalLoad.add(new double[]{time, total});
        }
        this.setTimeToTotalLoad(totalLoad);
//        return totalLoad;
//        this.utils.writeToNewCSV(title, totalLoad,
//                new String[]{"Time", "Total_Load"});
    }

    public List<LoadComparison> createLCList(List<double[]> oldTimeToTotalLoad,
                                             List<double[]> newTimeToTotalLoad) {
        List<LoadComparison> lcList = new ArrayList<>();
        for (int i = 0; i < oldTimeToTotalLoad.size(); i++) {
            LoadComparison lc = new LoadComparison();
            lc.setId(String.valueOf(i));
            BigDecimal time = BigDecimal.valueOf(oldTimeToTotalLoad.get(i)[0]).
                    setScale(2, RoundingMode.HALF_UP);
            BigDecimal oldLoad = BigDecimal.valueOf(oldTimeToTotalLoad.get(i)[1]).
                    setScale(2, RoundingMode.HALF_UP);
            BigDecimal newLoad = BigDecimal.valueOf(newTimeToTotalLoad.get(i)[1]).
                    setScale(2, RoundingMode.HALF_UP);
            lc.setTime(time);
            lc.setOldLoad(oldLoad);
            lc.setNewLoad(newLoad);
            lcList.add(lc);
        }
        return lcList;
    }

    public List<EVTimeComparison> createETCList(List<double[]> EVList, double[] solutions) {
        List<EVTimeComparison> etcList = new ArrayList<>();

        // params: maxSOC, remainingSOC, chargingPower, returningTime, leavingTime, chargingTime, endTime
        for (int i = 0; i < EVList.size(); i++) {
            EVTimeComparison etc = new EVTimeComparison();
            etc.setId(String.valueOf(i));
            BigDecimal oldStartTime = BigDecimal.valueOf(EVList.get(i)[3]).
                    setScale(2, RoundingMode.HALF_UP);
            BigDecimal oldEndTime = BigDecimal.valueOf(EVList.get(i)[6]).
                    setScale(2, RoundingMode.HALF_UP);
            BigDecimal newStartTime = BigDecimal.valueOf(solutions[i]).
                    setScale(2, RoundingMode.HALF_UP);
            BigDecimal newEndTime = BigDecimal.valueOf(solutions[i] + EVList.get(i)[5]).
                    setScale(2, RoundingMode.HALF_UP);
            etc.setOldStartTime(oldStartTime);
            etc.setOldEndTime(oldEndTime);
            etc.setNewStartTime(newStartTime);
            etc.setNewEndTime(newEndTime);
            etcList.add(etc);
        }
        return etcList;
    }

    public List<EVData> getEVDatabase() {
        return EVDatabase;
    }

    public void setEVDatabase(List<EVData> EVDatabase) {
        this.EVDatabase = EVDatabase;
    }

    public List<String[]> getTimeToDailyLoad() {
        return timeToDailyLoad;
    }

    public void setTimeToDailyLoad(List<String[]> timeToDailyLoad) {
        this.timeToDailyLoad = timeToDailyLoad;
    }

    public List<String[]> getTimeToChargingLoad() {
        return timeToChargingLoad;
    }

    public void setTimeToChargingLoad(List<String[]> timeToChargingLoad) {
        this.timeToChargingLoad = timeToChargingLoad;
    }

    public List<double[]> getTimeToTotalLoad() {
        return timeToTotalLoad;
    }

    public void setTimeToTotalLoad(List<double[]> timeToTotalLoad) {
        this.timeToTotalLoad = timeToTotalLoad;
    }

    public List<double[]> getTimeToLoadList() {
        return timeToLoadList;
    }

    public void setTimeToLoadList(List<double[]> timeToLoadList) {
        this.timeToLoadList = timeToLoadList;
    }

    public List<double[]> getEVList() {
        return EVList;
    }

    public void setEVList(List<double[]> EVList) {
        this.EVList = EVList;
    }

    public int getEVNum() {
        return EVNum;
    }

    public void setEVNum(int EVNum) {
        this.EVNum = EVNum;
    }

    public double[] getNewChargingStartTime() {
        return newChargingStartTime;
    }

    public void setNewChargingStartTime(double[] newChargingStartTime) {
        this.newChargingStartTime = newChargingStartTime;
    }

    public static void main(String[] args) throws IOException {
        long startTime = System.nanoTime();
        OrderlyCharging oc = new OrderlyCharging();
        int loop = 10;
        // 模拟生成必要参数
        List<String[]> timeToAvgChargingPower = oc.multipleSimulateMCM(loop);
        List<String[]> timeToDailyLoad = oc.simulateResidentialDailyPowerLoad();
        oc.setEVDatabase(oc.simulateEVDatabase());

        // 整合打包参数传入优化算法
        oc.setEVDataList(oc.getEVDatabase());
        oc.setTimeToLoadList(timeToDailyLoad, timeToAvgChargingPower);
        oc.setTimeToTotalLoad();
        List<double[]> oldTimeToTotalLoad = oc.getTimeToTotalLoad();

        // 优化算法更新汽车充电时间
        List<String[]> newTimeToChargingLoad = oc.updateOptimizedSolution();
        oc.setTimeToChargingLoad(newTimeToChargingLoad);

        oc.setTimeToTotalLoad();
        List<double[]> newTimeToTotalLoad = oc.getTimeToTotalLoad();

        // 将本次循环所得数据传入数据库
        ExportData ed = new ExportData();
//        List<LoadComparison> lcList = oc.createLCList(oldTimeToTotalLoad, newTimeToTotalLoad);
//        ed.exportLoadComparison(lcList);

        List<EVTimeComparison> etcList = oc.createETCList(oc.getEVList(), oc.getNewChargingStartTime());
        ed.exportEVTimeComparison(etcList);

        long endTime = System.nanoTime();
        long elapsedTime = (endTime - startTime) / 1000000;
        logger.info("elapsed time: " + elapsedTime + "ms");

    }
}

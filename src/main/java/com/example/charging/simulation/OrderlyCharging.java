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
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;
import com.example.charging.utils.Utils;


public class OrderlyCharging extends NormalCharging {
    private static final Logger logger = LogManager.getLogger(OrderlyCharging.class);
    private final Utils utils;
    private List<EVData> EVDatabase;
    private List<String[]> timeToDailyLoad;
    private List<String[]> timeToChargingLoad;
    private List<double[]> timeToTotalLoad;
    private double[] newChargingStartTime;
    private int EVNum;
    private int maxEvaluation;
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
        this.timeToChargingLoad = new ArrayList<>();
    }

    // 返回： 模拟所得用电负荷参数
    public void setTimeLoadList(List<String[]> timeToDailyLoad, List<String[]> timeToAvgChargingLoad) {
        // 将所有参数统一放到一个表内
        this.setTimeToDailyLoad(timeToDailyLoad);
        this.setTimeToChargingLoad(timeToAvgChargingLoad);
    }

    public void updateOptimizedSolution() {
        NondominatedPopulation result = new Executor()
                .withProblemClass(ChargingStrategy.class, this.getTimeToDailyLoad(),
                        this.getEVDatabase(), this.getEVNum(), TIME_NUMS)
                .withAlgorithm("OMOPSO")
                .withMaxEvaluations(this.getMaxEvaluation())
                .distributeOnAllCores()
                .run();

        // 所得solutions是优化过的电动汽车开始充电时间分布
        Solution solution = result.get(0);
        double[] newStartTime = EncodingUtils.getReal(solution);
        this.setNewChargingStartTime(newStartTime);

        // 计算新的每个时刻对应的充电负荷
        List<String[]> temp = new ArrayList<>();
        for (double time = 0; time < 24; time += 0.25) {
            double currPower = 0;
            for (int j = 0; j < this.EVDatabase.size(); j++) {
                EVData ev = this.EVDatabase.get(j);
                double newEndTime = newStartTime[j] + ev.getChargingTime();
                newEndTime = this.utils.convertTimeToNextDay(newEndTime);
                if (this.utils.timeIsInRange(time, newStartTime[j], newEndTime)) {
                    currPower += ev.getChargingPower();
                }
            }
            temp.add(new String[]{String.valueOf(time), String.valueOf(currPower)});
        }
        this.setTimeToChargingLoad(temp);
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
    }

    public List<LoadComparison> createLCList(List<double[]> oldTimeToTotalLoad,
                                             List<double[]> newTimeToTotalLoad, String count, String timeStamp) {
        List<LoadComparison> lcList = new ArrayList<>();

        String uid = "Simulation_" + count + "_" + timeStamp;
        for (int i = 0; i < oldTimeToTotalLoad.size(); i++) {
            LoadComparison lc = new LoadComparison();
            lc.setUid(uid);
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

    public List<EVTimeComparison> createETCList(double[] solutions, String count,
                                                String timeStamp) {
        List<EVTimeComparison> etcList = new ArrayList<>();
        String uid = "Simulation_" + count + "_" + timeStamp;

        // params: maxSOC, remainingSOC, chargingPower, returningTime, leavingTime, chargingTime, endTime
        for (int i = 0; i < this.getEVDatabase().size(); i++) {
            EVData ev = this.getEVDatabase().get(i);
            EVTimeComparison etc = new EVTimeComparison();
            etc.setUid(uid);
            etc.setEvid("EV_" + i);
            Time oldStartTime = this.utils.convertHoursToExactTime(ev.getReturningTime());
            Time oldEndTime = this.utils.convertHoursToExactTime(ev.getChargingEndTime());
            Time newStartTime = this.utils.convertHoursToExactTime(solutions[i]);
            double endTime = solutions[i] + ev.getChargingTime();
            endTime = endTime > 24 ? (endTime - 24) : endTime;
            Time newEndTime = this.utils.convertHoursToExactTime(endTime);
            Time leavingTime = this.utils.convertHoursToExactTime(ev.getLeavingTime());

            etc.setOldStartTime(oldStartTime);
            etc.setOldEndTime(oldEndTime);
            etc.setNewStartTime(newStartTime);
            etc.setNewEndTime(newEndTime);
            etc.setLeavingTime(leavingTime);
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

    public int getMaxEvaluation() {
        return maxEvaluation;
    }

    public void setMaxEvaluation(int maxEvaluation) {
        this.maxEvaluation = maxEvaluation;
    }

    public double calculateTotalChargingLoad(List<String[]> chargingLoad) {
        double sum = 0;
        for (String[] s : chargingLoad) {
            sum += Double.parseDouble(s[1]);
        }
        return sum;
    }

    public static void main(String[] args) throws IOException {
        long startTime = System.nanoTime();
        OrderlyCharging oc = new OrderlyCharging();
        int loop = 10;

        // 模拟生成必要参数
        List<String[]> timeToAvgChargingPower = oc.multipleSimulateMCM(loop);

        List<String[]> timeToDailyLoad = oc.simulateResidentialDailyPowerLoad();
        oc.setTimeLoadList(timeToDailyLoad, timeToAvgChargingPower);
        oc.setEVDatabase(oc.simulateEVDatabase());
        oc.setEVNum(oc.getEVDatabase().size());

        // 记录总负荷
        oc.setTimeToTotalLoad();
        List<double[]> oldTimeToTotalLoad = oc.getTimeToTotalLoad();

        // 优化算法更新汽车充电时间
        oc.setMaxEvaluation(100000);
        oc.updateOptimizedSolution();
        oc.setTimeToTotalLoad();
        List<double[]> newTimeToTotalLoad = oc.getTimeToTotalLoad();

        // 将本次循环所得数据传入数据库
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        ExportData ed = new ExportData();
        List<LoadComparison> lcList = oc.createLCList(oldTimeToTotalLoad, newTimeToTotalLoad,
                "2", timeStamp);
        ed.exportLoadComparison(lcList);

        List<EVTimeComparison> etcList = oc.createETCList(oc.getNewChargingStartTime(),
                "2", timeStamp);
        ed.exportEVTimeComparison(etcList);

        long endTime = System.nanoTime();
        long elapsedTime = (endTime - startTime) / 1000000;
        logger.info("elapsed time: " + elapsedTime + "ms");

    }
}

package com.example.charging.simulation;/* This class aims to simulate how a normal charging (non-orderly) would affect a residential community's
 * power load capacity
 * 此 Class 致力于模拟无序充电如何影响一个居民小区的电力负荷 */

import org.apache.commons.math3.special.Gamma;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import java.util.*;
import com.example.charging.utils.Utils;

public class NormalCharging {
    private static final Logger logger = LogManager.getLogger(NormalCharging.class);
    private Map<String, int[]> EVModelsData;
    private String[] EVModels;
    private final Random generator = new Random();
    private double marketPermeability;
    private int totalVehicleNumber;
    private final Utils utils;

    // Initialize
    public NormalCharging() {
        this.initializeEVModels();
        this.setMarketPermeability(0.3);
        this.setTotalVehicleNumber(400);
        this.utils = new Utils();
    }

    public void initializeEVModels() {
        // initialize with EV specs
        // 初始化电动汽车参数
        // Use the data of top 5 sales of EV models in 2023.6
        // 使用2023 6 月销售前五的电动汽车数据
        this.EVModels = new String[]{"特斯拉 Model Y", "比亚迪 宋PLUS新能源", "比亚迪 海豚",
                "比亚迪 元PLUS", "特斯拉 Model 3"};
        this.EVModelsData = new HashMap<>();
        List<int[]> chargingSpec = new ArrayList<>(); // 格式为： [慢充功率，快充功率，电池容量]
        chargingSpec.add(new int[]{11, 250, 75});
        chargingSpec.add(new int[]{7, 140, 87});
        chargingSpec.add(new int[]{6, 60, 45});
        chargingSpec.add(new int[]{6, 60, 60});
        chargingSpec.add(new int[]{11, 250, 60});
        for (int i = 0; i < 5; i++) {
            EVModelsData.put(EVModels[i], chargingSpec.get(i));
        }
    }

    // return: simulate distribution of residential daily power load
    // 返回：模拟生成的居民日常用电功率分布
    public List<String[]> simulateResidentialDailyPowerLoad() {
        List<String[]> res = new ArrayList<>();

        for (double i = 0; i <= 15; i += 0.25) {
            double power = 120 * Math.cos(0.47 * i) + 640;
            String powerStr = this.utils.limitToThreeDecimal(power);
            res.add(new String[]{String.valueOf(i), String.valueOf(powerStr)});
        }

        for (double i = 15.25; i < 24; i += 0.25) {
            double power = 80 * Math.sin(0.385 * i) + 766;
            String powerStr = this.utils.limitToThreeDecimal(power);
            res.add(new String[]{String.valueOf(i), String.valueOf(powerStr)});
        }
        return res;
    }

    /* Section to build probability models
     * 此处预定建立各概率模型 */

    // private vehicles daily travel distance
    // 私家车日里程概率分布
    // return: the approximated gamma distribution for daily travel distances
    // 返回：近似的日行驶里程的伽马分布

    public List<String[]> simulateDailyTravelDistance() {
        double alphaWork = 1.20; // 工作日尺度参数
        double lambdaWork = 0.0383; // 工作日形状参数

        double constantWork = (Math.pow(lambdaWork, alphaWork) / Gamma.gamma(alphaWork)) *
                Math.pow(lambdaWork, alphaWork - 1);

        // generate n samples
        List<String[]> res = new ArrayList<>();

        for (int i = 1; i <= 120; i++) {
            double prob = constantWork * Math.exp(-1 * lambdaWork * i);
//            logger.info("prob: " + prob);
            res.add(new String[]{String.valueOf(i), String.valueOf(prob)});
        }
        return res;
    }

    // SOC distribution
    // SOC分布情况
    // 返回： SOC 及其分布概率
    // return: SOC and distribution
    public List<String[]> simulateSOCDistribution(List<String[]> data) {
        int maxTravelDistance = 200;
        List<String[]> SOCDistribution = new ArrayList<>();

        for (String[] datum : data) {
            double SOC = (0.7 - Double.parseDouble(datum[0]) / maxTravelDistance) * 100; // 百分数 in %
            SOCDistribution.add(new String[]{String.valueOf(SOC), datum[1]});
        }
        return SOCDistribution;
    }

    // Returning time distribution
    // 返回时刻分布
    public List<String[]> simulateReturningTime() {
        double alphaWork = 104.489; // 工作日尺度参数
        double lambdaWork = 5.1947; // 工作日形状参数

        double constantWork = Math.pow(lambdaWork, alphaWork) / Gamma.gamma(alphaWork);

        List<String[]> res = new ArrayList<>();

        for (double t = 3; t <= 26; t += 0.25) {
            double prob = constantWork * Math.pow(t, alphaWork - 1) * Math.exp(-1 * lambdaWork * t);
            if (t >= 24) {
                double temp = t % 24;
                res.add(new String[]{String.valueOf(temp), String.valueOf(prob)});
            } else {
                res.add(new String[]{String.valueOf(t), String.valueOf(prob)});
            }
        }
        return res;
    }

    // Leaving time distribution
    // 首次始发时刻分布
    public List<String[]> simulateLeavingTime() {
        double mu = 8.95;
        double sigma = 2.24;

        List<String[]> res = new ArrayList<>();
        double constantWork = 1 / (Math.sqrt(2 * Math.PI) * sigma);

        for (double i = 0; i < 21; i += 0.25) {
            double prob = constantWork * Math.exp(-1 * Math.pow((i - mu), 2) / (2 * Math.pow(sigma, 2)));
            res.add(new String[]{String.valueOf(i), String.valueOf(prob)});
        }

        for (double i = 21; i < 24; i += 0.25) {
            double prob = constantWork * Math.exp(-1 * Math.pow((i - 24 - mu), 2) / (2 * Math.pow(sigma, 2)));
            res.add(new String[]{String.valueOf(i), String.valueOf(prob)});
        }
        return res;
    }

    // Vehicle type distribution
    // 车辆型号分布
    // return: n EV types
    // 返回： n 个电动汽车型号
    public String[] simulateVehicleTypeDistribution(int n) {
        String[] EVTypes = new String[n];
        for (int i = 0; i < n; i++) {
            int idx = this.generator.nextInt(5);
            EVTypes[i] = this.EVModels[idx];
        }
        return EVTypes;
    }

    public double findMaxCumulativeProb(List<String[]> mapping) {
        double cumulativeProb = 0;
        for (String[] data : mapping) {
            double currProb = Double.parseDouble(data[1]);
            cumulativeProb += currProb;
        }
        return cumulativeProb;
    }

    // return: the item corresponding to its probability density
    // 返回： 对应其概率密度的值
    public String findItemFromProb(List<String[]> mapping, double prob) {
        Map<Double, String> map = new HashMap<>();
        List<double[]> intervals = new ArrayList<>();

        int size = mapping.size();
        double cdf = Double.parseDouble(mapping.get(0)[1]);
        map.put(cdf, mapping.get(0)[1]);
        intervals.add(new double[]{0, cdf});

        for (int i = 1; i < size; i++) {
            double curr = Double.parseDouble(mapping.get(i)[1]);
            intervals.add(new double[]{cdf, cdf + curr});
            cdf += curr;
            map.put(cdf, mapping.get(i)[0]);
        }

        for (double[] interval : intervals) {
            if (prob >= interval[0] && prob <= interval[1]) {
                return map.get(interval[1]);
            }
        }
        return "";
    }

    // 模拟生成 n 辆电动车模型
    public List<EVData> simulateEVDatabase() {
        this.generator.setSeed(System.nanoTime());
        int EVNumber = (int) Math.round(this.getMarketPermeability() * this.getTotalVehicleNumber());
        List<String[]> travelDistances = this.simulateDailyTravelDistance();
        List<String[]> SOCDistribution = this.simulateSOCDistribution(travelDistances);
        List<String[]> returningTime = this.simulateReturningTime();
        List<String[]> leavingTime = this.simulateLeavingTime();
        String[] EVModels = this.simulateVehicleTypeDistribution(EVNumber);
        List<EVData> EVDatabase = new ArrayList<>();

        // generate n samples
        for (int i = 0; i < EVNumber; i++) {
            EVData ev = new EVData(EVModels[i]);
            ev.setMaxSOC(this.EVModelsData.get(EVModels[i])[2]);

            // 生成随机SOC
            double maxProb = this.findMaxCumulativeProb(SOCDistribution);
            double prob = this.generator.nextDouble() * maxProb;
            double lastSOC = Double.parseDouble(this.findItemFromProb(SOCDistribution, prob));
            ev.setRemainingSOC(lastSOC);

            int[] probBox = new int[]{0, 0, 0, 0, 1}; // 假设4/5的人会用慢充
            int ifUseFastCharging = probBox[generator.nextInt(5)];
            ev.setUseFastCharging(ifUseFastCharging);

            // 生成随机返回时间
            maxProb = this.findMaxCumulativeProb(returningTime);
            prob = this.generator.nextDouble() * maxProb;
            double retTime = Double.parseDouble(this.findItemFromProb(returningTime, prob));
            ev.setReturningTime(retTime);

            // 生成随机离开时间
            maxProb = this.findMaxCumulativeProb(leavingTime);
            prob = this.generator.nextDouble() * maxProb;
            double leavTime = Double.parseDouble(this.findItemFromProb(leavingTime, prob));
            ev.setLeavingTime(leavTime);

            int[] EVparams = this.EVModelsData.get(ev.getModelName());
            double maxCharge = EVparams[2];
            ev.setMaxSOC(maxCharge);
            ev.setChargingPower(EVparams[ifUseFastCharging]);
//            ev.setChargingPower(EVparams[0]); // 假设全部用慢充
            double chargeTime = (maxCharge * (1 - ev.getRemainingSOC() / 100)) / ev.getChargingPower();
            ev.setChargingTime(chargeTime);
            double endTime = this.utils.convertTimeToNextDay(ev.getReturningTime() + ev.getChargingTime());
            ev.setChargingEndTime(endTime);
            EVDatabase.add(ev);
        }
        return EVDatabase;
    }

    /* Section to build up Monte Carlo simulation
     * 此处预定用蒙特卡洛法模拟
     * details: use the given expressions, repeatedly sample from uniform distribution (acceptance-rejection)
     * 操作细节：重复对一个均匀分布取样, 然后用接受拒绝法
     * then get average values from samplings
     * 然后对多次采样取平均值

     * Goal: get equation that calculates the summation of Pevi, where Pevi is the charging
     * power for each vehicle
     * 目标：用求 Pevi 和的公式来计算总充电功率，Pevi 为每辆电动汽车的充电功率 */

    public List<String[]> simulateMonteCarlo() {
        // Assume each EV owner needs at least 8, at most 14 hours to be home
        // 假设每个车主至少需要8小时，至多需要14小时在家休息

        // generate n EVs
        // 模拟生成 n 辆电动车
        List<EVData> EVDatabase = this.simulateEVDatabase();

        // 计算每个时段需要的充电负荷
        List<String[]> timeToPower = new ArrayList<>();
        for (double time = 0; time < 24; time += 0.25) {
            double currPower = 0;
            for (EVData ev : EVDatabase) {
                if (this.utils.timeIsInRange(time, ev.getReturningTime(), ev.getChargingEndTime())) {
                    currPower += ev.getChargingPower();
                }
            }
            timeToPower.add(new String[]{String.valueOf(time), String.valueOf(currPower)});
        }
        return timeToPower;
    }

    public double calculatePeakValleyRate(List<String[]> chargingLoad, List<String[]> dailyLoad) {
        double maxPower = 0;
        double minPower = Integer.MAX_VALUE;

        for (int i = 0; i < 96; i++) {
            double chargePower = Double.parseDouble(chargingLoad.get(i)[1]);
            double dailyPower = Double.parseDouble(dailyLoad.get(i)[1]);
            double totalPower = chargePower + dailyPower;
            maxPower = Math.max(maxPower, totalPower);
            minPower = Math.min(minPower, totalPower);
        }
        return (maxPower - minPower) / maxPower * 100;
    }

    // simulate Monte Carlo for loop times and then take average values
    // 多次模拟Monte Carlo法， 最后取平均值
    public List<String[]> multipleSimulateMCM(int loop) {
        double[] powerAvg = new double[96];

        for (int i = 0; i < loop; i++) {
            List<String[]> timeToPower = this.simulateMonteCarlo();
            for (int j = 0; j < powerAvg.length; j++) {
                powerAvg[j] += Double.parseDouble(timeToPower.get(j)[1]);
            }
        }

        for (int i = 0; i < 96; i++) {
            powerAvg[i] /= loop;
        }

        List<String[]> avgTimeToPower = new ArrayList<>();
        for (double t = 0; t < 24; t += 0.25) {
            double power = powerAvg[(int) (t * 4)];
            avgTimeToPower.add(new String[]{String.valueOf(t), String.valueOf(power)});
        }
        return avgTimeToPower;
    }

    public double getMarketPermeability() {
        return marketPermeability;
    }

    public void setMarketPermeability(double marketPermeability) {
        this.marketPermeability = marketPermeability;
    }

    public int getTotalVehicleNumber() {
        return totalVehicleNumber;
    }

    public void setTotalVehicleNumber(int totalVehicleNumber) {
        this.totalVehicleNumber = totalVehicleNumber;
    }

//    public static void main(String[] args) throws IOException {
//        long startTime = System.nanoTime();
//        NormalCharging nc = new NormalCharging();
//
//        long endTime = System.nanoTime();
//        long totalTime = (endTime - startTime) / 1000000;
//        logger.info("Total execution time: " + totalTime + " ms");
//    }
}

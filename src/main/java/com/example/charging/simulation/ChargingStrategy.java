package com.example.charging.simulation;/* This class aims to use the Multi-Objective Particle Swarm Optimization to optimize the
   charging strategy
   此类旨在用多粒子群优化法来优化充电策略
* */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.moeaframework.core.*;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;
import com.opencsv.*;

import java.util.*;
import java.io.FileReader;
import java.io.IOException;
import com.example.charging.utils.Utils;

public class ChargingStrategy extends AbstractProblem {
    private List<double[]> timeToParams;
    private List<double[]> EVDataBase;

    private int nTimeSlots;
    private int nEVs;
    private double maxLoad = 1012.5; // 来自论文数据
    private static final Logger logger = LogManager.getLogger(ChargingStrategy.class);
    private Utils util;

    // decision variables 决策变量
    // 测试使用每辆车的起始充电时间和结束充电时间
    public ChargingStrategy(List<double[]> timeToParams, List<double[]> EVDataBase, int NumEVs,
                            int NumTimeSlots) throws IOException {
        super(NumEVs, 2, NumEVs + NumTimeSlots);
        this.setNEVs(NumEVs);
        this.setnTimeSlots(NumTimeSlots);
        this.setTimeToParams(timeToParams);
        this.setEVDataBase(EVDataBase);
        this.util = new Utils();
    }

    @Override
    public void evaluate(Solution solution) {
        List<double[]> timeToAllParams = this.getTimeToParams();
        List<double[]> EVData = this.getEVDataBase();
        int timeNum = this.getNTimeSlots();
        double[] constraints = new double[this.getNEVs() + this.getNTimeSlots()];

        double price = 0.0;
        double dailyLoadVariance = 0.0;
        double[] chargingStartingTime = EncodingUtils.getReal(solution);
        Map<Double, Double> timeToPower = new HashMap<>();
        double[] loadArray = new double[timeNum];

        for (int i = 0; i < timeNum; i++) {
            double[] rowData = timeToAllParams.get(i); // "Time","Daily_Load"
            double dailyLoad = rowData[1];
            loadArray[i] = dailyLoad;
        }

        // 将随机生成的时间转换为0.25区间的标准时间
        for (int i = 0; i < this.getNEVs(); i++) {
            double converted = this.util.convertToProperTime(chargingStartingTime[i]);
            if (converted == 24.0) {
                converted = 0.0;
            }
            chargingStartingTime[i] = converted;
        }


        // 计算新的Pev分布
        // double chargeTime = (maxCharge * (1 - ev.getRemainingSOC() / 100)) / ev.getChargingPower();
        for (double t = 0; t < 24; t += 0.25) {
            double currPower = 0;
            for (int i = 0; i < this.getNEVs(); i++) { // "Max_SOC, Remaining_SOC, Charging_Power","Returning_Time","Leaving_Time","Charging_Time"
                double[] row = EVData.get(i);
                double startTime = chargingStartingTime[(int) (t * 4)];
                double leavingTime = row[4];
                leavingTime += 24; // 表示第二天的时刻

                if (t >= startTime && t <= leavingTime) {
                    currPower += row[2];
                }

                // 检查充电时间约束条件
                double newEndTime = chargingStartingTime[(int) (t * 4)] + row[5];
                double returningTime = row[3];

                if (newEndTime >= returningTime && newEndTime <= leavingTime) {
                    constraints[i] = 0.0;
                } else {
                    constraints[i] = leavingTime - newEndTime;
                }
            }

//            // 检查最大负载的约束条件
            if (loadArray[(int) (t * 4)] + currPower > this.maxLoad) {
                constraints[(int) (t * 4) + this.getNEVs()] = this.maxLoad - loadArray[(int) (t * 4)] + currPower;
            } else {
                constraints[(int) (t * 4) + this.getNEVs()] = 0.0;
            }
            timeToPower.put(t, currPower);
        }

        // 计算用户电价
        for (double t : timeToPower.keySet()) {
            price += timeToPower.get(t) * this.findPrice(t);
        }

        // calculate daily power load variance
        // 计算每日负荷方差
        double avgLoad = 0;
        for (int i = 0; i < timeNum; i++) {
            avgLoad += loadArray[i];
        }
        avgLoad /= timeNum;

        for (double t = 0; t < 24; t += 0.25) {
            dailyLoadVariance += Math.pow((loadArray[(int) (t * 4)] + timeToPower.get(t) - avgLoad), 2);
        }
        dailyLoadVariance /= (timeNum - 1);
//        logger.info("price: " + price);
//        logger.info("variance: " + dailyLoadVariance);
        solution.setObjective(0, price);
        solution.setObjective(1, dailyLoadVariance);
        solution.setConstraints(constraints);
    }

    @Override
    public Solution newSolution() {
        Solution solution = new Solution(this.getNumberOfVariables(), this.getNumberOfObjectives(),
                this.getNEVs() + this.getNTimeSlots());
        for (int i = 0; i < this.getNEVs(); i++) {
            solution.setVariable(i, EncodingUtils.newReal(0.0, 23.5));
        }

        return solution;
    }

    @Override
    public String getName() {
        return "com.example.charging.simulation.ChargingStrategy";
    }

    @Override
    public int getNumberOfConstraints() {
        return this.getNEVs();
    }

    @Override
    public int getNumberOfObjectives() {
        return 2;
    }

    @Override
    public int getNumberOfVariables() {
        return this.getNEVs();
    }

    // return: the price corresponding to the given time slot
    // 返回： 与输入时段相对应的电价
    public double findPrice(double t) {
        if ((t >= 0 && t <= 7) || t > 23) {
            return 0.3518;
        } else if ((t > 7 && t <= 11) || (t > 13 && t <= 19)) {
            return 0.8495;
        } else {
            return 1.2282;
        }
    }

    public List<double[]> getTimeToParams() {
        return timeToParams;
    }

    public void setTimeToParams(List<double[]> timeToParams) {
        this.timeToParams = timeToParams;
    }

    public List<double[]> getEVDataBase() {
        return EVDataBase;
    }

    public void setEVDataBase(List<double[]> EVDataBase) {
        this.EVDataBase = EVDataBase;
    }

    public int getNTimeSlots() {
        return nTimeSlots;
    }

    public void setNTimeSlots(int nTimeSlots) {
        this.nTimeSlots = nTimeSlots;
    }

    public int getNEVs() {
        return nEVs;
    }

    public void setNEVs(int nEVs) {
        this.nEVs = nEVs;
    }

    public int getnTimeSlots() {
        return nTimeSlots;
    }

    public void setnTimeSlots(int nTimeSlots) {
        this.nTimeSlots = nTimeSlots;
    }

    public int getnEVs() {
        return nEVs;
    }

    public void setnEVs(int nEVs) {
        this.nEVs = nEVs;
    }

    public double getMaxLoad() {
        return maxLoad;
    }

    public void setMaxLoad(double maxLoad) {
        this.maxLoad = maxLoad;
    }

//    public static void main(String[] args) {
//        long startTime = System.nanoTime();
//        NondominatedPopulation result = new Executor()
//                .withProblemClass(com.example.charging.simulation.ChargingStrategy.class, "./src/data/timeToAllParams.csv",
//                        "./src/data/EVDatabase.csv", 120, 96)
//                .withAlgorithm("OMOPSO")
//                .withMaxEvaluations(100000)
//                .distributeOnAllCores()
//                .run();
//
////        for (int i = 0; i < result.size(); i++) {
//        Solution solution = result.get(0);
//        double[] objectives = solution.getObjectives();
//        double[] vars = new double[solution.getNumberOfVariables()];
//        double[] solutions = EncodingUtils.getReal(solution);
//        for (int i = 0; i < solution.getNumberOfVariables(); i++) {
//            vars[i] = solutions[i];
//        }
//        List<String[]> solutionList = new ArrayList<>();
//        for (int i = 0; i < solution.getNumberOfVariables(); i++) {
//            solutionList.add(new String[]{String.valueOf(i), String.valueOf(vars[i])});
//        }
//        com.example.charging.utils.Utils utils = new com.example.charging.utils.Utils();
//        utils.writeToNewCSV("./src/data/SolutionList.csv", solutionList, new String[]{"Index",
//                "Charging_Starting_Time"});

//        logger.info("Solution " + 0 + ": ");
//        logger.info("Min user charging fee: " + objectives[0]);
//        logger.info("Min daily load variance: " + objectives[1]);
//        long endTime = System.nanoTime();
//        long elapsedTime = (endTime - startTime) / 1000000;
//        logger.info("elapsed time: " + elapsedTime + "ms");
//        }
//    }
}

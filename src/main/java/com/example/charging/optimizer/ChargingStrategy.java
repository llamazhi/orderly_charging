package com.example.charging.optimizer;
/* This class aims to use the Multi-Objective Particle Swarm Optimization to optimize the
   charging strategy
   此类旨在用多粒子群优化法来优化充电策略
* */

import com.example.charging.entity.EVData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.moeaframework.core.*;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;

import java.util.*;

import com.example.charging.utils.Utils;

public class ChargingStrategy extends AbstractProblem {
    private List<double[]> timeToDailyLoad;
    private List<EVData> EVDataBase;

    private int nTimeSlots;
    private int nEVs;
    private double maxLoad = 1012.5; // 来自论文数据
    private static final Logger logger = LogManager.getLogger(ChargingStrategy.class);
    private Utils util;

    // decision variables 决策变量
    // 测试使用每辆车的起始充电时间和结束充电时间
    public ChargingStrategy(List<double[]> timeToDailyLoad, List<EVData> EVDataBase, int NumEVs,
                            int NumTimeSlots) {
        super(NumEVs, 2, NumEVs + NumTimeSlots);
        this.setNEVs(NumEVs);
        this.setnTimeSlots(NumTimeSlots);
        this.setTimeToDailyLoad(timeToDailyLoad);
        this.setEVDataBase(EVDataBase);
        this.util = new Utils();
//        logger.info("entered optimizer constructor");
    }

    @Override
    public void evaluate(Solution solution) {
        int timeNum = this.getNTimeSlots();
        double[] constraints = new double[this.getNEVs() + this.getNTimeSlots()];

        double price = 0.0;
        double dailyLoadVariance = 0.0;
        double[] chargingStartingTime = EncodingUtils.getReal(solution);
        Map<Double, Double> timeToPower = new HashMap<>();
        double[] loadArray = new double[timeNum];

        for (int i = 0; i < timeNum; i++) {
            // "Time","Daily_Load"
            loadArray[i] = this.getTimeToDailyLoad().get(i)[1];
        }

        // 计算新的Pev分布
        for (double t = 0; t < 24; t += 0.25) {
            double currPower = 0;
            for (int i = 0; i < this.getNEVs(); i++) {
                // params: maxSOC, remainingSOC, chargingPower, returningTime, leavingTime, chargingTime, endTime
                EVData ev = this.getEVDataBase().get(i);
                double oldStartTime = ev.getReturningTime();
                double newStartTime = chargingStartingTime[i];
                double leavingTime = ev.getLeavingTime();
                double newEndTime = this.util.convertTimeToNextDay(newStartTime + ev.getChargingTime());

                if (this.util.timeIsInRange(t, newStartTime, newEndTime)) {
                    currPower += ev.getChargingPower();
                }

                // 检查充电时间约束条件
                if ((newStartTime > 15 && newStartTime < 24 || newStartTime > 0 && newStartTime < 5)) {
                    constraints[i] = 0.0;
                } else {
                    constraints[i] = oldStartTime - newStartTime;
                }

                if ((newEndTime >= 0 && newEndTime <= 8) || (newEndTime >= 18 && newEndTime < 24)) {
                    constraints[i] = 0.0;
                } else {
                    constraints[i] = leavingTime - newEndTime;
                }

                if (newStartTime - oldStartTime <= 6 && newStartTime - oldStartTime > 0) {
                    constraints[i] = 0.0;
                } else {
                    constraints[i] = newStartTime - oldStartTime;
                }

                if (newEndTime < leavingTime) {
                    constraints[i] = 0.0;
                } else {
                    constraints[i] = leavingTime - newEndTime;
                }
            }

//            // 检查最大负载约束条件
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
//        logger.info("curr NEV: " + this.getNEVs());
        for (int i = 0; i < this.getNEVs(); i++) {
            solution.setVariable(i, EncodingUtils.newReal(0.0, 23.9));
        }

        return solution;
    }

    @Override
    public String getName() {
        return "com.example.charging.optimizer.ChargingStrategy";
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

    public List<double[]> getTimeToDailyLoad() {
        return timeToDailyLoad;
    }

    public void setTimeToDailyLoad(List<double[]> timeToDailyLoad) {
        this.timeToDailyLoad = timeToDailyLoad;
    }

    public List<EVData> getEVDataBase() {
        return EVDataBase;
    }

    public void setEVDataBase(List<EVData> EVDataBase) {
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
//                .withProblemClass(com.example.charging.optimizer.ChargingStrategy.class, "./src/data/timeToAllParams.csv",
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

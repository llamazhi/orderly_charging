/* This class aims to use the Multi-Objective Particle Swarm Optimization to optimize the
   charging strategy
   此类旨在用多粒子群优化法来优化充电策略
* */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.moeaframework.Executor;
import org.moeaframework.core.*;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;
import com.opencsv.*;

import java.util.*;
import java.io.FileReader;
import java.io.IOException;

public class ChargingStrategy extends AbstractProblem {
    private List<String[]> timeToParams;
    private List<String[]> EVDataBase;

    private int nTimeSlots;
    private int nEVs;
    private double maxLoad = 1012.5; // 来自论文数据
    private static final Logger logger = LogManager.getLogger(ChargingStrategy.class);
    private Utils util;

    // decision variables 决策变量
    // 测试使用每辆车的起始充电时间和结束充电时间
    public ChargingStrategy(String timeDataFileName, String EVDataFileName) throws IOException {
        super(1, 2);
        CSVReader timeReader = new CSVReaderBuilder(new FileReader(timeDataFileName))
                .withSkipLines(1)
                .build();
        CSVReader EVReader = new CSVReaderBuilder(new FileReader(EVDataFileName))
                .withSkipLines(1)
                .build();
        load(timeReader, EVReader);
        this.util = new Utils();
    }

    public void load(CSVReader timeReader, CSVReader EVReader) throws IOException {
        List<String[]> timeToParamsData = timeReader.readAll();
        List<String[]> EVData = EVReader.readAll();
        this.setTimeToParams(timeToParamsData);
        this.setEVDataBase(EVData);
        this.setNTimeSlots(timeToParamsData.size());
        this.setNEVs(EVData.size());
    }

    @Override
    public void evaluate(Solution solution) {
        List<String[]> timeToAllParams = this.getTimeToParams();
        List<String[]> EVData = this.getEVDataBase();
        int timeNum = this.getNTimeSlots();
        double constraint = 0;

        double price = 0.0;
        double dailyLoadVariance = 0.0;
        double[] chargingStartingTime = EncodingUtils.getReal(solution);
        double[] EVChargingPower = new double[this.getNEVs()];

        // 将随机生成的时间转换为0.25区间的标准时间
        for (int i = 0; i < this.getNEVs(); i++) {
            double converted = this.util.convertToProperTime(chargingStartingTime[i]);
            if (converted == 24.0) {
                converted = 0.0;
            }
            chargingStartingTime[i] = converted;
        }


//        // calculate charging price for each ev
//        // 计算第i辆电动汽车的充电价格
//        String[] row = EVData.get(EVidx); // idx chargingTime chargingPower returningTime
//        double chargingTime = Double.parseDouble(row[1]);
//        double chargingPower = Double.parseDouble(row[2]);
//        double returningTime = Double.parseDouble(row[3]);
//        double endTime = returningTime + chargingTime;
//        if (endTime >= 24) endTime -= 24;
//        for (double t = returningTime; t <= endTime; t += 0.25) {
//            double currPrice = this.findPrice(t);
//            price += currPrice * chargingPower * chargingTime;
//        }
//
//        // calculate daily power load variance
//        // 计算每日负荷方差
//        double avgLoad = 0;
//        for (int i = 0; i < timeNum; i++) {
//            String[] rowData = timeToAllParams.get(i); // "Time","Daily_Load","Delta_Power","Charging_Power"
//            double dailyLoad = Double.parseDouble(rowData[1]);
//            avgLoad += dailyLoad;
//        }
//        avgLoad /= 96;
//
//        for (int i = 0; i < timeNum; i++) {
//            String[] rowData = timeToAllParams.get(i); // "Time","Daily_Load","Delta_Power","Charging_Power"
//            double dailyLoad = Double.parseDouble(rowData[1]);
//            double EVchargingPower = Double.parseDouble(rowData[2]);
//            double dailyTotalLoad = dailyLoad + EVchargingPower;
//            if (dailyTotalLoad >= constraint) {
//                dailyTotalLoad = constraint;
//            }
//            dailyLoadVariance += Math.pow((dailyTotalLoad - avgLoad), 2);
//        }
//        dailyLoadVariance /= (timeNum - 1);
//
//        solution.setObjective(0, price);
//        solution.setObjective(1, dailyLoadVariance);
//        solution.setObjective(0, constraint);
    }

    @Override
    public Solution newSolution() {
        Solution solution = new Solution(numberOfVariables, numberOfObjectives);
        for (int i = 0; i < this.getNEVs(); i++) {
            solution.setVariable(i, EncodingUtils.newReal(0.0, 23.5));
        }

        return solution;
    }

    @Override
    public String getName() {
        return "ChargingStrategy";
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

    public List<String[]> getTimeToParams() {
        return timeToParams;
    }

    public void setTimeToParams(List<String[]> timeToParams) {
        this.timeToParams = timeToParams;
    }

    public List<String[]> getEVDataBase() {
        return EVDataBase;
    }

    public void setEVDataBase(List<String[]> EVDataBase) {
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

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        NondominatedPopulation result = new Executor()
                .withProblemClass(ChargingStrategy.class, "./src/data/timeToAllParams.csv",
                        "./src/data/EVDatabase.csv")
                .withAlgorithm("OMOPSO")
                .withMaxEvaluations(10000)
                .distributeOnAllCores()
                .run();

        for (int i = 0; i < result.size(); i++) {
            Solution solution = result.get(i);
            double[] objectives = solution.getObjectives();
            logger.info("Solution " + i + ": ");
            logger.info("Min user charging fee: " + objectives[0]);
            logger.info("Min daily load variance: " + objectives[1]);
            logger.info("Selected time slot: " + solution.getVariable(0));
            long endTime = System.nanoTime();
            long elapsedTime = (endTime - startTime) / 1000000;
            logger.info("elapsed time: " + elapsedTime + "ms");
        }
    }
}

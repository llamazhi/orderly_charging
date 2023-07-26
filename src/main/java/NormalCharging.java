/* This class aims to simulate how a normal charging (non-orderly) would affect a residential community's
 * power load capacity
 * 此 Class 致力于模拟无序充电如何影响一个居民小区的电力负荷 */

import org.apache.commons.math3.special.Gamma;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.knowm.xchart.*;

import java.io.*;
import java.io.IOException;

import com.opencsv.*;

import java.util.*;

public class NormalCharging {
    private ResidentialAreaData data;
    private static final Logger logger = LogManager.getLogger(ResidentialAreaData.class);

    // Initialize
    public NormalCharging() {
    }


    public NormalCharging(ResidentialAreaData data) {
        this.data = data;
    }
    /* Section to build up Monte Carlo simulation
     * 此处预定用蒙特卡洛法模拟
     * details: use the given expressions, repeatedly sample from uniform distribution (acceptance-rejection)
     * 操作细节：运用论文中的公式，重复对一个均匀分布取样（接受拒绝法）
     * then get average values from samplings
     * 然后对多次采样取平均值

     * Goal: get equation that calculates the summation of Pevi, where Pevi is the charging
     * power for each vehicle
     * 目标：用求 Pevi 和的公式来计算总充电功率，Pevi 为每辆电动汽车的充电功率 */

    public void simulateMonteCarlo() {
        // Final step to calculate the total power usage of the residential community
        // 最后一步要计算小区的总使用功率
    }

    /* Section to build probability models
     * 此处预定建立各概率模型 */

    // private vehicles daily travel distance
    // 私家车日里程概率分布
    // return: the approximated gamma distribution for daily travel distances
    // 返回：近似的日行驶里程的伽马分布

    public List<String[]> simulateDailyTravelDistance() {
        double alphaWork = 1.20; // 工作日尺度参数
        double lambdaWork = 3.83e-2; // 工作日形状参数
        double alphaWeekend = 1.25; // 非工作日尺度参数
        double lambdaWeekend = 3.12e-2; // 非工作日形状参数

        double constantWork = Math.pow(lambdaWork, alphaWork) / Gamma.gamma(alphaWork) *
                Math.pow(lambdaWork, alphaWork - 1);

        // generate n samples
//        int n = this.data.getVehicleNumber();
        int n = 200; // test number
        String[] travelDistDistribution = new String[n];
        String[] travelDist = new String[n];
        Random generator = new Random();

        for (int i = 0; i < n; i++) {
            int dist = generator.nextInt(121); // max distance set to be 120km for now
//            double dist = generator.nextDouble();
            double res = constantWork * Math.exp(-1 * lambdaWork * dist);
            travelDistDistribution[i] = String.valueOf(res);
            travelDist[i] = String.valueOf(dist);
        }

        List<String[]> res = new ArrayList<>();
        res.add(travelDist);
        res.add(travelDistDistribution);
        return res;
    }

    // SOC distribution *
    // SOC分布情况 *
    public void simulateSOCDistribution() {

    }

    // Returning time distribution
    // 返回时刻分布
    public void simulateReturningTime() {

    }

    // Leaving time distribution
    // 首次始发时刻分布
    public void simulateLeavingTime() {

    }

    // Vehicle type distribution
    // 车辆型号分布
    public void simulateVehicleTypeDistribution() {

    }

    public static void main(String[] args) throws IOException {


        NormalCharging nc = new NormalCharging();
        List<String[]> travelDistanceData = nc.simulateDailyTravelDistance();
        CSVWriter writer = new CSVWriter(new FileWriter("./src/data/dailyTravelDistances.csv"));
        writer.writeNext(travelDistanceData.get(0));
        writer.writeNext(travelDistanceData.get(1));
        writer.flush();
        logger.info("Daily travel distance data has been written");
        logger.info("日里程数据已被导出");
    }

}

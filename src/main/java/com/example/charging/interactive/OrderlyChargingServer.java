package com.example.charging.interactive;

import com.example.charging.database.ExportData;
import com.example.charging.entity.EVTimeComparison;
import com.example.charging.entity.LoadComparison;
import com.example.charging.optimizer.ChargingStrategy;
import com.example.charging.entity.EVData;
import com.example.charging.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class OrderlyChargingServer {
    private List<EVData> EVList;
    private List<EVData> newEVs;
    private int maxEvaluation;
    private final Utils utils;
    private List<double[]> timeToDailyLoad;
    private List<double[]> timeToTotalLoad;
    private List<double[]> timeToChargingLoad;
    private Socket socket;

    private static final int TIME_NUMS = 96; // 24h/day, in every 15 min
    private static final Logger logger = LogManager.getLogger(OrderlyChargingServer.class);


    public OrderlyChargingServer() {
        this.utils = new Utils();
        this.EVList = new ArrayList<>();
        this.newEVs = new ArrayList<>();
        this.initializeTimeLists();
    }

    public void initializeTimeLists() {
        this.timeToChargingLoad = new ArrayList<>();
        this.timeToTotalLoad = new ArrayList<>();
        this.timeToDailyLoad = this.utils.simulateResidentialDailyPowerLoad();
        for (double i = 0; i < 24; i+=0.25) {
            this.timeToChargingLoad.add(new double[]{i, 0});
            this.timeToTotalLoad.add(new double[]{i, 0});
        }
    }

    public void addNewEV(EVData ev) {
        this.EVList.add(ev);
    }

    public void removeEV(int idx) {
        this.EVList.remove(idx);
    }

    public double[] getNewOptimalSolution() {
        pickEffectiveNewEVs();
        List<EVData> newEVs = this.getNewEVs();
        NondominatedPopulation result = new Executor()
                .withProblemClass(ChargingStrategy.class, this.getTimeToDailyLoad(),
                        newEVs, newEVs.size(), TIME_NUMS)
                .withAlgorithm("OMOPSO")
                .withMaxEvaluations(this.getMaxEvaluation())
                .distributeOnAllCores()
                .run();
        // 所得solutions是优化过的电动汽车开始充电时间分布
        Solution solution = result.get(0);
        return EncodingUtils.getReal(solution);
    }

    // 选出本次新加入的EV
    public void pickEffectiveNewEVs() {
        List<EVData> res = new ArrayList<>();
        for (EVData ev : this.getEVList()) {
            if (!ev.isAlreadyOptimized()) {
                res.add(ev);
            }
            ev.setAlreadyOptimized(true);
        }
        this.setNewEVs(res);
    }

    // 计算新的每个时刻对应的充电负荷
    public void updateTimeToChargingLoad(double[] newStartTime) {
        for (double time = 0; time < 24; time += 0.25) {
            double currPower = 0;
            for (int j = 0; j < this.getNewEVs().size(); j++) {
                EVData ev = this.getNewEVs().get(j);
                double newEndTime = newStartTime[j] + ev.getChargingTime();
                newEndTime = this.utils.convertTimeToNextDay(newEndTime);
                if (this.utils.timeIsInRange(time, newStartTime[j], newEndTime)) {
                    currPower += ev.getChargingPower();
                }
            }
            this.timeToChargingLoad.set((int)(time * 4), new double[]{time, currPower});
        }
    }

    // 更新每时段对应总用电负荷
    public void updateTimeToTotalLoad() {
        for (int i = 0; i < TIME_NUMS; i++) {
            double time = this.timeToDailyLoad.get(i)[0];
            double daily = this.timeToDailyLoad.get(i)[1];
            double charging = this.timeToChargingLoad.get(i)[1];
            double total = daily + charging;
            this.timeToTotalLoad.set(i, new double[]{time, total});
        }
    }

    public void updateDatabase() throws IOException {
        // 记录总负荷
        List<double[]> oldTimeToTotalLoad = this.getTimeToTotalLoad();

        // 优化算法更新汽车充电时间
        this.setMaxEvaluation(100000);
        double[] newStartingTime = this.getNewOptimalSolution();
        this.updateTimeToChargingLoad(newStartingTime);
        this.updateTimeToTotalLoad();
        List<double[]> newTimeToTotalLoad = this.getTimeToTotalLoad();

        // 将本次循环所得数据传入数据库
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        ExportData ed = new ExportData();
        List<LoadComparison> lcList = ed.createLCList(oldTimeToTotalLoad, newTimeToTotalLoad,
                "3", timeStamp);
        ed.exportLoadComparison(lcList);

//        logger.info("EV List size: " + this.getEVList().size());
        List<EVTimeComparison> etcList = ed.createETCList(newStartingTime, this.getNewEVs(),
                "3", timeStamp);
        ed.exportEVTimeComparison(etcList);
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            logger.info("Server started ...");
            while (true) {
                this.socket = serverSocket.accept();
                ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
                EVData ev = (EVData) inStream.readObject();
                this.addNewEV(ev);
                this.updateDatabase();
                logger.info("new EV added!");
            }

        } catch (IOException e) {e.printStackTrace();}
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeServer() throws IOException {
        this.socket.close();
    }

    public List<EVData> getEVList() {
        return EVList;
    }

    public void setEVList(List<EVData> EVList) {
        this.EVList = EVList;
    }

    public List<double[]> getTimeToDailyLoad() {
        return timeToDailyLoad;
    }

    public void setTimeToDailyLoad(List<double[]> timeToDailyLoad) {
        this.timeToDailyLoad = timeToDailyLoad;
    }

    public int getMaxEvaluation() {
        return maxEvaluation;
    }

    public void setMaxEvaluation(int maxEvaluation) {
        this.maxEvaluation = maxEvaluation;
    }

    public List<double[]> getTimeToTotalLoad() {
        return timeToTotalLoad;
    }

    public void setTimeToTotalLoad(List<double[]> timeToTotalLoad) {
        this.timeToTotalLoad = timeToTotalLoad;
    }

    public List<double[]> getTimeToChargingLoad() {
        return timeToChargingLoad;
    }

    public void setTimeToChargingLoad(List<double[]> timeToChargingLoad) {
        this.timeToChargingLoad = timeToChargingLoad;
    }

    public List<EVData> getNewEVs() {
        return newEVs;
    }

    public void setNewEVs(List<EVData> newEVs) {
        this.newEVs = newEVs;
    }

    public static void main(String[] args) throws IOException {
        OrderlyChargingServer ocs = new OrderlyChargingServer();
        ocs.startServer();
        // add feature to accept manual ending command
//        if (args[0].equals("end")) {
//            ocs.closeServer();
//        }
    }
}

package com.example.charging.database;

import com.example.charging.entity.EVTimeComparison;
import com.example.charging.entity.LoadComparison;
import com.example.charging.mapper.EVTimeComparisonMapper;
import com.example.charging.mapper.LoadComparisonMapper;
import com.example.charging.entity.EVData;
import com.example.charging.utils.Utils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExportData {
    private static final Logger logger = LogManager.getLogger(ExportData.class);
    static InputStream stream = null;
    static SqlSessionFactoryBuilder builder = null;
    static SqlSessionFactory factory= null;
    static SqlSession sqlSession= null;

    private final Utils utils = new Utils();
    public ExportData() throws IOException {
    }

    public void startSession() throws IOException {
        logger.info("Start loading data to database ... ");
        //1.加载核心配置文件的字节输入流
        stream = Resources.getResourceAsStream("mybatis.xml");
        //2.创建SqlSessionFactory的构建对象--框架使用的是构建者模式
        builder=new SqlSessionFactoryBuilder();
        //3.通过构建对象加载配置文件的输入流获取SqlSessionFactory
        factory=builder.build(stream);
        //4.通过工厂对象获取SqlSession对象----执行JDBC操作的
        sqlSession=factory.openSession();
    }

    // 固定语句，为了释放资源
    public void closeSession() {
        sqlSession.commit();
        sqlSession.close();
    }

    public void exportLoadComparison(List<LoadComparison> lcList) throws IOException {
        this.startSession();
        LoadComparisonMapper lcMapper = sqlSession.getMapper(LoadComparisonMapper.class);
        for (LoadComparison loadComparison : lcList) {
            lcMapper.add(loadComparison);
        }
        this.closeSession();
    }

    public void exportEVTimeComparison(List<EVTimeComparison> etcList) throws IOException {
        this.startSession();
        EVTimeComparisonMapper etcMapper = sqlSession.getMapper(EVTimeComparisonMapper.class);
        for (EVTimeComparison evTimeComparison : etcList) {
            etcMapper.add(evTimeComparison);
        }
        this.closeSession();
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

    public List<EVTimeComparison> createETCList(double[] solutions, List<EVData> EVList , String count,
                                                String timeStamp) {
        List<EVTimeComparison> etcList = new ArrayList<>();
        String uid = "Simulation_" + count + "_" + timeStamp;

        // params: maxSOC, remainingSOC, chargingPower, returningTime, leavingTime, chargingTime, endTime
        for (int i = 0; i < EVList.size(); i++) {
            EVData ev = EVList.get(i);
            EVTimeComparison etc = new EVTimeComparison();
            etc.setUid(uid);
            etc.setEvid("EV_" + ev.getUuid());
//            Time oldStartTime = this.utils.convertHoursToExactTime(ev.getReturningTime()); // for local simulation only
//            logger.info(this.utils);
            Time oldStartTime = this.utils.convertHoursToExactTime(ev.getChargingStartTime()); // for interactive app
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
}

package com.example.charging.simulation;

import com.example.charging.entity.EVTimeComparison;
import com.example.charging.entity.LoadComparison;
import com.example.charging.mapper.EVTimeComparisonMapper;
import com.example.charging.mapper.LoadComparisonMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExportData {
    private static final Logger logger = LogManager.getLogger(ExportData.class);
    static InputStream stream = null;
    static SqlSessionFactoryBuilder builder = null;
    static SqlSessionFactory factory= null;
    static SqlSession sqlSession= null;

    public void ExportData() throws IOException {
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
}

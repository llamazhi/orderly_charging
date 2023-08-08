import com.example.charging.entity.DailyTravelDistance;
import com.example.charging.entity.UserEntity;
import com.example.charging.mapper.DailyTravelDistanceMapper;
import com.example.charging.mapper.UserTestMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * TODO  PACKAGE_NAME
 *
 * @author WeiTang
 * @date 2023/8/7
 */
public class UserTest {
    static InputStream stream = null;
    static SqlSessionFactoryBuilder builder = null;
    static SqlSessionFactory factory= null;
    static SqlSession sqlSession= null;

    public static void main(String[] args) throws IOException {
        System.out.println("Before");
        //1.加载核心配置文件的字节输入流
        stream = Resources.getResourceAsStream("mybatis.xml");
        //2.创建SqlSessionFactory的构建对象--框架使用的是构建者模式
        builder=new SqlSessionFactoryBuilder();
        //3.通过构建对象加载配置文件的输入流获取SqlSessionFactory
        factory=builder.build(stream);
        //4.通过工厂对象获取SqlSession对象----执行JDBC操作的
        sqlSession=factory.openSession();

        // 前面是固定语句


        //5.通过SqlSession对象获取接口对应的代理对象
//        UserTestMapper mapper = sqlSession.getMapper(UserTestMapper.class);
//        mapper.add(new UserEntity(4, "李666"));
//        List<UserEntity> userEntities = mapper.selectAll();
//        System.out.println(userEntities);
        DailyTravelDistanceMapper mapper = sqlSession.getMapper(DailyTravelDistanceMapper.class);
        UserTestMapper mapper2 = sqlSession.getMapper(UserTestMapper.class);
        mapper.add(new DailyTravelDistance("id1", 100));
        List<DailyTravelDistance> id1 = mapper.selectById("id1");
        List<UserEntity> userEntities = mapper2.selectAll();
        System.out.println(id1);


        // 这也是固定语句，为了释放资源
        sqlSession.commit();
        sqlSession.close();
    }
}

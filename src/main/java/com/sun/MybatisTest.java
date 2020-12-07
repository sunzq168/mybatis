package com.sun;

import com.sun.mapper.StudentMapper;
import com.sun.model.Student;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class MybatisTest {
    public static void main(String[] args) throws IOException {
        InputStream is = Resources.getResourceAsStream("com/sun/mybatis-config.xml");
        //加载 mybatis-config.xml 配置文件 ，并创建 SqlSessionFactory 对象
        SqlSessionFactory sqlSessionfactory = new SqlSessionFactoryBuilder().build(is);
        //创建 SqlSession 对象
        SqlSession sqlSession = sqlSessionfactory.openSession();

        StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);

        Student student = studentMapper.selectById(1);
        System.out.println(student);

        studentMapper.updateById(1);

        student = studentMapper.selectById(1);
        System.out.println(student);



        sqlSession.commit();
        sqlSession.close();


        //Reflector reflector = new Reflector(Student.class);

    }
}

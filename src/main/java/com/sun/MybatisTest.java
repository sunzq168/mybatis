package com.sun;

import com.sun.mapper.StudentMapper;
import com.sun.model.Student;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.reflection.Reflector;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class MybatisTest {
    public static void main(String[] args) throws IOException {
//        InputStream is = Resources.getResourceAsStream("com/sun/mybatis-config.xml");
//        SqlSessionFactory sqlSessionfactory = new SqlSessionFactoryBuilder().build(is);
//        SqlSession sqlSession = sqlSessionfactory.openSession();
//        StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);
//
//        Student student = studentMapper.selectById(1);
//        System.out.println(student);
//
//        sqlSession.commit();
//        sqlSession.close();


        Reflector reflector = new Reflector(Student.class);

    }
}

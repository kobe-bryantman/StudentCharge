package com.example.exam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 在线考试系统启动类
 *
 * @author example
 */
@SpringBootApplication
@MapperScan("com.example.exam.mapper")
public class ExamApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamApplication.class, args);
        System.out.println("============================================");
        System.out.println("  在线考试系统启动成功！");
        System.out.println("  访问地址：http://localhost:3721");
        System.out.println("============================================");
    }

}

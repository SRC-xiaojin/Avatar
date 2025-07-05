package com.operatorchoreography;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 算子编排系统主启动类
 */
@SpringBootApplication
@MapperScan("com.operatorchoreography.generator.mapper")
public class OperatorChoreographyApplication {

    public static void main(String[] args) {
        SpringApplication.run(OperatorChoreographyApplication.class, args);
        System.out.println("算子编排系统后端服务启动成功!");
        System.out.println("Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println("API文档: http://localhost:8080/v3/api-docs");
        System.out.println("健康检查: http://localhost:8080/test/health");
        System.out.println("数据库: MySQL (operator_choreography)");
    }
} 
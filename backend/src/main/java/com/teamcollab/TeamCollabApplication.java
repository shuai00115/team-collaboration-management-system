package com.teamcollab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 团队协作管理系统 - Spring Boot 启动类
 *
 * @author TeamCollab
 */
@SpringBootApplication
@MapperScan("com.teamcollab.mapper") // 扫描 MyBatis Mapper 接口
@EnableScheduling // 启用定时任务支持
public class TeamCollabApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeamCollabApplication.class, args);
        System.out.println("========================================");
        System.out.println("  团队协作管理系统启动成功！");
        System.out.println("  Knife4j文档: http://localhost:8080/doc.html");
        System.out.println("========================================");
    }
}

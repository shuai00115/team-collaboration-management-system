package com.teamcollab.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j (Swagger) API 文档配置类
 * 配置 OpenAPI 3.0 规范文档，提供可视化接口调试界面
 * 访问地址：http://localhost:8080/doc.html
 *
 * @author TeamCollab
 */
@Configuration
public class Knife4jConfig {

    /**
     * 创建 OpenAPI 文档配置
     * 包含文档标题、描述、版本、联系方式等基本信息
     *
     * @return OpenAPI 实例
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("团队协作管理系统") // API 文档标题
                        .description("团队协作管理系统接口文档，提供团队成员管理、任务分配、项目协作等功能。" +
                                "\n访问地址：http://localhost:8080/doc.html") // API 详细描述
                        .version("1.0.0") // 版本号
                        .contact(new Contact()
                                .name("TeamCollab 开发团队") // 联系人/团队名称
                                .email("support@teamcollab.com") // 联系邮箱
                                .url("https://www.teamcollab.com") // 团队主页
                        )
                );
    }

    /**
     * 创建 API 分组配置
     * 扫描 com.teamcollab.controller 包下所有接口，
     * 生成"全部接口"分组，供 Knife4j 文档页面展示。
     *
     * @return GroupedOpenApi 实例
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("全部接口")
                .pathsToMatch("/**")
                .packagesToScan("com.teamcollab.controller")
                .build();
    }
}

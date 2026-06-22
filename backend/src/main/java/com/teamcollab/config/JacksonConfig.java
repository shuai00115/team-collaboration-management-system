package com.teamcollab.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * Jackson JSON 序列化配置类
 * 统一日期格式、时区以及序列化策略
 *
 * @author TeamCollab
 */
@Configuration
public class JacksonConfig {

    /**
     * 自定义 Jackson ObjectMapper 配置
     * - 日期格式：yyyy-MM-dd HH:mm:ss
     * - 时区：GMT+8（北京时间）
     * - 序列化策略：不包含 null 值字段
     *
     * @return Jackson2ObjectMapperBuilderCustomizer 定制器
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            builder.simpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 统一日期时间格式
            builder.timeZone(TimeZone.getTimeZone("GMT+8")); // 设置中国时区
            builder.serializationInclusion(JsonInclude.Include.NON_NULL); // null 值字段不参与序列化
        };
    }
}

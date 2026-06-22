package com.teamcollab.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 配置分页插件等 MyBatis-Plus 核心组件
 *
 * @author TeamCollab
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * MyBatis-Plus 拦截器配置
     * 注册分页拦截器，设置数据库类型为 MySQL，单页最大查询条数为 500
     *
     * @return MybatisPlusInterceptor 实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页拦截器：指定数据库类型为MySQL，限制单页最大条数为500
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(500L); // 防止恶意查询导致内存溢出
        interceptor.addInnerInterceptor(paginationInterceptor);

        return interceptor;
    }
}

package com.example.mdtoword.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus配置类
 * 配置分页插件，解决分页查询不生效的问题
 * 
 * @author 坤坤
 * @since 2025-08-09
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 配置MyBatis Plus分页插件
     * 
     * 重要说明：
     * 没有这个配置，Page<T> pageParam = new Page<>(page, size) 不会生效
     * blogMapper.selectPage(pageParam, queryWrapper) 会返回所有数据而不是分页数据
     * 
     * @return MyBatis Plus拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 创建分页内部拦截器，指定数据库类型为MySQL
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        
        // 设置合理化参数：页码<=0时查询第1页，页码>总页数时查询最后一页
        paginationInnerInterceptor.setOverflow(true);
        
        // 设置单页分页条数限制，防止恶意请求大量数据
        paginationInnerInterceptor.setMaxLimit(500L);
        
        // 将分页拦截器添加到主拦截器中
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        
        return interceptor;
    }
}
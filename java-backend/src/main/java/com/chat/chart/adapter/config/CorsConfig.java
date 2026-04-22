package com.chat.chart.adapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * CORS 跨域配置
 * <p>
 * 配置全局跨域策略，允许所有来源的请求访问API接口，
 * 方便前端开发环境进行跨域调用。生产环境应收紧允许的源。
 * </p>
 *
 * @author Chat Chart System
 * @version 1.0.0
 */
@Configuration
public class CorsConfig {

    /**
     * 创建 CORS 跨域过滤器
     * <p>
     * 配置项包括：允许所有来源、所有请求头、所有HTTP方法、
     * 允许携带凭证、暴露所有响应头。
     * </p>
     *
     * @return {@link CorsWebFilter} 跨域过滤器实例
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许的源
        config.addAllowedOriginPattern("*");

        // 允许的请求头
        config.addAllowedHeader("*");

        // 允许的请求方法
        config.addAllowedMethod("*");

        // 允许携带凭证
        config.setAllowCredentials(true);

        // 暴露的响应头
        config.setExposedHeaders(Arrays.asList("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}

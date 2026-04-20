package com.chat.chart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * CORS跨域配置
 * 支持所有localhost端口，方便前端开发
 *
 * @author Chat Chart System
 * @version 1.0.0
 */
@Configuration
public class CorsConfig {

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

package com.chat.chart.adapter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 配置CORS跨域映射
     * <p>
     * 允许所有来源、所有请求头、所有HTTP方法，
     * 允许携带凭证。
     * </p>
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

package com.chat.chart.common.config;

import com.chat.chart.common.interceptor.JwtAuthInterceptor;
import com.chat.chart.common.util.JwtTokenUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置
 * <p>
 * 注册 JWT 认证拦截器，拦截 /chatbot/** 下所有业务接口。
 * 健康检查路径（/、/health）不参与认证。
 * </p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 构造器注入 JwtTokenUtil
     *
     * @param jwtTokenUtil JWT 工具类，用于传递给拦截器
     */
    public WebMvcConfig(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * 注册 JWT 认证拦截器
     * <p>
     * 拦截路径: /chatbot/**（所有业务接口）
     * 排除路径: /（首页）、/health（健康检查）
     * </p>
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtAuthInterceptor(jwtTokenUtil))
                .addPathPatterns("/chatbot/**")
                .excludePathPatterns("/", "/health");
    }
}

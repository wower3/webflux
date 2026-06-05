package com.chat.chart.common.interceptor;

import com.chat.chart.common.util.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 认证拦截器
 * <p>
 * 拦截 /chatbot/** 路径的请求，从 Authorization 请求头中提取 token 并校验。
 * OPTIONS 预检请求直接放行（支持跨域）。
 * 校验失败时返回 401 状态码和 JSON 错误信息。
 * </p>
 *
 * @see com.chat.chart.common.config.WebMvcConfig
 */
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthInterceptor.class);

    private final JwtTokenUtil jwtTokenUtil;

    public JwtAuthInterceptor(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * 请求前置处理：校验 JWT token
     * <p>
     * 1. OPTIONS 请求直接放行（CORS 预检）
     * 2. 从 Authorization 请求头提取 token，支持 "Bearer xxx" 格式
     * 3. 调用 JwtTokenUtil.validateToken() 校验签名和过期时间
     * 4. 校验不通过时返回 401 + JSON 错误响应
     * </p>
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行跨域预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 提取 Authorization 请求头
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            log.debug("缺少Authorization请求头: uri={}", request.getRequestURI());
            writeUnauthorized(response, "缺少认证信息");
            return false;
        }

        // 去掉 "Bearer " 前缀，提取纯 token
        String token = authHeader;
        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // 校验 token 有效性
        if (!jwtTokenUtil.validateToken(token)) {
            log.debug("token无效或已过期: uri={}", request.getRequestURI());
            writeUnauthorized(response, "token无效或已过期");
            return false;
        }

        return true;
    }

    /**
     * 写入 401 未授权响应
     * <p>
     * 统一返回 JSON 格式: {"success": false, "message": "xxx"}
     * </p>
     *
     * @param response HTTP 响应对象
     * @param message  错误提示信息
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}

package com.chat.chart.adapter.config;

import com.chat.chart.app.service.AuthAppService;
import com.chat.chart.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 认证过滤器
 * <p>
 * 拦截所有HTTP请求，校验请求头中的 Authorization Bearer Token。
 * 对于无需认证的路径（如根路径、健康检查、登录注册接口）直接放行。
 * 认证成功后将用户ID存入请求属性，供下游处理器使用。
 * </p>
 *
 * @see AuthAppService
 */
@Component
@Order(1)
public class AuthFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);

    /** 认证应用服务，用于token验证和用户信息提取 */
    private final AuthAppService authAppService;

    /**
     * 构造方法，注入认证服务
     *
     * @param authAppService 认证应用服务
     */
    public AuthFilter(AuthAppService authAppService) {
        this.authAppService = authAppService;
    }

    /**
     * 过滤请求，校验token有效性
     * <p>
     * 处理流程：
     * 1. 从请求头提取 Bearer Token
     * 2. 调用认证服务验证token，验证通过后放行并将userId存入请求属性
     * 3. token缺失或无效时不拦截，使用默认userId放行
     * </p>
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 提取token
        String authHeader = httpRequest.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token != null && !token.trim().isEmpty()) {
            // 有token则尝试获取userId，获取失败也不拦截
            try {
                User user = authAppService.validateToken(token);
                if (user != null) {
                    httpRequest.setAttribute("userId", user.getId());
                } else {
                    httpRequest.setAttribute("userId", 1L);
                }
            } catch (Exception e) {
                httpRequest.setAttribute("userId", 1L);
            }
        } else {
            // 无token，使用默认userId放行
            httpRequest.setAttribute("userId", 1L);
        }

        chain.doFilter(request, response);
    }
}

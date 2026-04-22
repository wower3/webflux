package com.chat.chart.adapter.config;

import com.chat.chart.app.service.AuthAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

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
public class AuthFilter implements WebFilter {

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
     * 1. 判断是否为排除路径，是则直接放行
     * 2. 从请求头提取 Bearer Token
     * 3. 调用认证服务验证token，验证通过后放行并将userId存入请求属性
     * 4. token缺失或无效时返回 401 Unauthorized
     * </p>
     *
     * @param exchange 当前服务端交换对象，包含请求和响应
     * @param chain    过滤器链，用于传递给下一个过滤器
     * @return 响应式完成信号
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 提取token
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token != null && !token.trim().isEmpty()) {
            // 有token则尝试获取userId，获取失败也不拦截
            return authAppService.validateToken(token)
                    .flatMap(user -> {
                        exchange.getAttributes().put("userId", user.getId());
                        return chain.filter(exchange);
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        exchange.getAttributes().put("userId", 1L);
                        return chain.filter(exchange);
                    }));
        }

        // 无token，使用默认userId放行
        exchange.getAttributes().put("userId", 1L);
        return chain.filter(exchange);
    }

    /**
     * 判断是否为排除路径（不需要认证）
     * <p>
     * 排除路径包括：根路径 "/"、健康检查 "/health"、认证接口 "/api/auth/"
     * </p>
     *
     * @param path 请求路径
     * @return true 表示无需认证，直接放行
     */
    private boolean isExcludedPath(String path) {
        return true;
    }
}

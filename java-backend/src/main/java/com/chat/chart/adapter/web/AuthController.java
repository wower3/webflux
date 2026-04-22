package com.chat.chart.adapter.web;

import com.chat.chart.app.dto.LoginRequest;
import com.chat.chart.app.dto.LoginResponse;
import com.chat.chart.app.service.AuthAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 认证控制器
 * <p>
 * 提供用户注册和登录接口，返回JWT token用于后续API调用的身份认证。
 * 该控制器的所有接口路径均在 {@link com.chat.chart.adapter.config.AuthFilter} 的排除列表中，
 * 无需携带token即可访问。
 * </p>
 *
 * @see AuthAppService
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    /** 认证应用服务 */
    private final AuthAppService authAppService;

    /**
     * 构造方法，注入认证服务
     *
     * @param authAppService 认证应用服务
     */
    public AuthController(AuthAppService authAppService) {
        this.authAppService = authAppService;
    }

    /**
     * 用户注册接口
     * <p>
     * 接收用户名和密码，创建新用户并返回登录凭证。
     * 注册失败时返回 400 Bad Request。
     * </p>
     *
     * @param request 注册请求体，包含用户名和密码
     * @return 响应式包装的登录响应，包含token和用户信息
     */
    @PostMapping("/register")
    public Mono<ResponseEntity<LoginResponse>> register(@RequestBody LoginRequest request) {
        log.info("[Auth] 注册请求: username={}", request.getUsername());
        return authAppService.register(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.warn("[Auth] 注册失败: {}", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body(new LoginResponse(null, null)));
                });
    }

    /**
     * 用户登录接口
     * <p>
     * 校验用户名和密码，验证通过后返回登录凭证。
     * 登录失败时返回 401 Unauthorized。
     * </p>
     *
     * @param request 登录请求体，包含用户名和密码
     * @return 响应式包装的登录响应，包含token和用户信息
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@RequestBody LoginRequest request) {
        log.info("[Auth] 登录请求: username={}", request.getUsername());
        return authAppService.login(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.warn("[Auth] 登录失败: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(401).body(new LoginResponse(null, null)));
                });
    }
}

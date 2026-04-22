package com.chat.chart.app.service;

import com.chat.chart.app.dto.LoginRequest;
import com.chat.chart.app.dto.LoginResponse;
import com.chat.chart.domain.gateway.UserGateway;
import com.chat.chart.domain.model.User;
import com.chat.chart.infrastructure.gateway.UserGatewayImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

/**
 * 认证应用服务
 * <p>
 * 提供用户注册、登录和令牌验证功能。
 * 使用UUID作为简单令牌机制，密码采用SHA-256哈希存储。
 * 所有数据库操作通过 {@link UserGateway} 网关接口完成。
 * </p>
 */
@Service
public class AuthAppService {

    private static final Logger log = LoggerFactory.getLogger(AuthAppService.class);

    /**
     * 用户数据网关
     */
    private final UserGateway userGateway;

    /**
     * 构造函数注入
     *
     * @param userGateway 用户数据网关
     */
    public AuthAppService(UserGateway userGateway) {
        this.userGateway = userGateway;
    }

    /**
     * 用户注册
     * <p>
     * 检查用户名是否已存在，不存在则创建新用户并生成认证令牌。
     * </p>
     *
     * @param request 注册请求，包含用户名和密码
     * @return 登录响应（包含令牌和用户名）
     * @throws RuntimeException 用户名已存在时抛出
     */
    public Mono<LoginResponse> register(LoginRequest request) {
        return Mono.fromCallable(() -> {
            // 检查用户名是否已被注册
            User existing = userGateway.findByUsername(request.getUsername());
            if (existing != null) {
                throw new RuntimeException("用户名已存在");
            }

            // 保存新用户（密码由网关层做SHA-256哈希）
            userGateway.saveUser(request.getUsername(), request.getPassword());

            // 查询刚创建的用户获取ID，用于绑定令牌
            User newUser = userGateway.findByUsername(request.getUsername());
            String token = UUID.randomUUID().toString().replace("-", "");
            userGateway.updateToken(newUser.getId(), token);

            log.info("[Auth] 用户注册成功: {}", request.getUsername());
            return new LoginResponse(token, request.getUsername());
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 用户登录
     * <p>
     * 验证用户名和密码，成功后生成新的认证令牌并更新到数据库。
     * </p>
     *
     * @param request 登录请求，包含用户名和密码
     * @return 登录响应（包含新令牌和用户名）
     * @throws RuntimeException 用户不存在或密码错误时抛出
     */
    public Mono<LoginResponse> login(LoginRequest request) {
        return Mono.fromCallable(() -> {
            // 根据用户名查找用户
            User user = userGateway.findByUsername(request.getUsername());
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 对输入密码做SHA-256哈希后与数据库中的哈希值比对
            String hashedInput = UserGatewayImpl.sha256(request.getPassword());
            if (!hashedInput.equals(user.getPassword())) {
                throw new RuntimeException("密码错误");
            }

            // 登录成功，生成新令牌并更新
            String token = UUID.randomUUID().toString().replace("-", "");
            userGateway.updateToken(user.getId(), token);

            log.info("[Auth] 用户登录成功: {}", request.getUsername());
            return new LoginResponse(token, request.getUsername());
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 验证令牌有效性
     * <p>
     * 根据令牌查询对应用户，令牌为空时直接返回null。
     * </p>
     *
     * @param token 认证令牌
     * @return 令牌对应的用户信息，无效令牌返回null
     */
    public Mono<User> validateToken(String token) {
        return Mono.fromCallable(() -> {
            if (token == null || token.trim().isEmpty()) {
                return null;
            }
            return userGateway.findByToken(token);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}

package com.chat.chart.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * JWT Token 工具类
 * <p>
 * 负责 JWT Token 的解析与校验，不负责生成。
 * 使用 HS512 算法，密钥和过期时间通过 application.yml 配置。
 * </p>
 *
 * @see com.chat.chart.common.interceptor.JwtAuthInterceptor
 */
@Component
public class JwtTokenUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);

    /** Claim 中用户名的 key，对应 JWT 标准的 sub 字段 */
    private static final String CLAIM_KEY_USERNAME = "sub";

    /** Claim 中 token 创建时间的 key */
    private static final String CLAIM_KEY_CREATED = "created";

    /** JWT 签名密钥，与生成端保持一致 */
    @Value("${jwt.secret}")
    private String secret;

    /** token 过期时间（秒），默认 7 天 */
    @Value("${jwt.expiration:604800}")
    private Long expiration;

    /** Authorization 请求头中 token 的前缀，默认 "Bearer " */
    @Value("${jwt.tokenHead:Bearer }")
    private String tokenHead;

    /**
     * 从 token 中解析 JWT 负载（Claims）
     * <p>
     * 使用配置的密钥验证签名，签名不匹配或格式错误时返回 null。
     * </p>
     *
     * @param token JWT token 字符串（不含 "Bearer " 前缀）
     * @return Claims 负载对象，解析失败返回 null
     */
    private Claims getClaimsFromToken(String token) {
        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.debug("JWT 格式验证失败: {}", token);
        }
        return claims;
    }

    /**
     * 判断 token 是否已过期
     * <p>
     * 无法获取过期时间（token 格式异常）时视为已过期。
     * </p>
     *
     * @param token JWT token 字符串
     * @return true 表示已过期，false 表示未过期
     */
    private boolean isTokenExpired(String token) {
        Date expiredDate = getExpiredDateFromToken(token);
        if (expiredDate == null) {
            return true;
        }
        return expiredDate.before(new Date());
    }

    /**
     * 从 token 中提取过期时间
     *
     * @param token JWT token 字符串
     * @return 过期时间，解析失败返回 null
     */
    private Date getExpiredDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        return claims.getExpiration();
    }

    /**
     * 校验 token 是否有效（签名合法且未过期）
     * <p>
     * 供拦截器调用，作为请求鉴权的核心判断方法。
     * </p>
     *
     * @param token JWT token 字符串（不含 "Bearer " 前缀）
     * @return true 表示 token 有效，false 表示无效或已过期
     */
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return false;
        }
        return !isTokenExpired(token);
    }

    /**
     * 从 token 中获取用户名（即 sub claim 的值）
     * <p>
     * 在拦截器中调用，解析出用户标识后设置到 request attribute，
     * 供后续 Controller 通过 @RequestAttribute 获取。
     * </p>
     *
     * @param token JWT token 字符串（不含 "Bearer " 前缀）
     * @return 用户名（用户ID），解析失败返回 null
     */
    public String getUsernameFromToken(String token) {
        String username = "";
        try {
            Claims claims = getClaimsFromToken(token);
            username = claims.getSubject();
        } catch (Exception e) {
            username = null;
        }
        return username;
    }
}

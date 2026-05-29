package com.chat.chart.infrastructure.config;

import com.chat.chart.infrastructure.mapper.LlmParameterMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * AI接口固定窗口限流器（基于数据库乐观锁）
 * <p>
 * 基于系统时间分钟粒度，每分钟最多允许调用指定次数。
 * 通过 llm_parameter 表的 CAS 机制保证多 pod 间计数一致。
 * </p>
 */
@Component
public class AiRateLimiter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiRateLimiter.class);

    private static final String RATE_LIMIT_KEY = "rate_limit_minute";
    private static final int MAX_RETRIES = 3;

    private final int maxRequestsPerMinute;
    private final LlmParameterMapper llmParameterMapper;
    private final ObjectMapper objectMapper;

    @Autowired
    public AiRateLimiter(LlmParameterMapper llmParameterMapper, ObjectMapper objectMapper) {
        this(15, llmParameterMapper, objectMapper);
    }

    public AiRateLimiter(int maxRequestsPerMinute, LlmParameterMapper llmParameterMapper, ObjectMapper objectMapper) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.llmParameterMapper = llmParameterMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 尝试获取调用许可
     *
     * @return true-允许调用，false-已被限流
     */
    public boolean tryAcquire() {
        long minute = System.currentTimeMillis() / 60000;

        for (int i = 0; i < MAX_RETRIES; i++) {
            String oldValue = llmParameterMapper.selectValueByKey(RATE_LIMIT_KEY);
            // 初次投产时表中无数据，先幂等插入初始行，再重新读取参与CAS
            if (oldValue == null) {
                initRateLimitRow(minute);
                oldValue = llmParameterMapper.selectValueByKey(RATE_LIMIT_KEY);
                // 插入失败（如DB异常）则放行，避免因缺数据导致服务不可用
                if (oldValue == null) {
                    return true;
                }
            }

            try {
                JsonNode node = objectMapper.readTree(oldValue);
                long dbMinute = node.path("minute").asLong();
                int count = node.path("count").asInt();
                boolean blocked = node.path("blocked").asBoolean();

                if (dbMinute == minute && blocked) {
                    LOGGER.warn("[RateLimit] 当前分钟已被外部服务限流: minute={}", minute);
                    return false;
                }
                if (dbMinute == minute && count >= maxRequestsPerMinute) {
                    LOGGER.warn("[RateLimit] 限流: minute={}, count={}, max={}", minute, count, maxRequestsPerMinute);
                    return false;
                }

                int newCount = (dbMinute != minute) ? 1 : count + 1;

                ObjectNode newNode = objectMapper.createObjectNode();
                newNode.put("minute", minute);
                newNode.put("count", newCount);
                newNode.put("blocked", false);
                String newValue = objectMapper.writeValueAsString(newNode);

                int affected = llmParameterMapper.casUpdate(RATE_LIMIT_KEY, oldValue, newValue);
                if (affected > 0) {
                    LOGGER.debug("[RateLimit] 放行: minute={}, count={}", minute, newCount);
                    return true;
                }
                LOGGER.debug("[RateLimit] CAS冲突，重试: {}/{}", i + 1, MAX_RETRIES);
            } catch (Exception e) {
                LOGGER.error("[RateLimit] 解析限流数据失败", e);
                return false;
            }
        }

        LOGGER.warn("[RateLimit] CAS重试耗尽，拒绝请求");
        return false;
    }

    /**
     * 初始化限流行（仅当key不存在时插入）
     * <p>
     * 使用 INSERT ... WHERE NOT EXISTS 保证多pod并发时只有一条插入成功，
     * 其余pod的插入被忽略后重新读取已存在的行参与CAS。
     * </p>
     */
    private void initRateLimitRow(long minute) {
        try {
            ObjectNode initNode = objectMapper.createObjectNode();
            initNode.put("minute", minute);
            initNode.put("count", 0);
            initNode.put("blocked", false);
            String initValue = objectMapper.writeValueAsString(initNode);
            int affected = llmParameterMapper.insertIfAbsent(RATE_LIMIT_KEY, initValue);
            if (affected > 0) {
                LOGGER.info("[RateLimit] 初始化限流数据: minute={}", minute);
            }
        } catch (Exception e) {
            LOGGER.error("[RateLimit] 初始化限流数据失败", e);
        }
    }

    /**

    /**
     * 标记当前分钟已被外部服务限流
     * <p>
     * 当外部AI服务返回限流错误时调用，将当前分钟标记为 blocked，
     * 阻止所有 pod 本分钟后续请求。
     * </p>
     */
    public void markBlocked() {
        long minute = System.currentTimeMillis() / 60000;

        for (int i = 0; i < MAX_RETRIES; i++) {
            String oldValue = llmParameterMapper.selectValueByKey(RATE_LIMIT_KEY);
            if (oldValue == null) {
                return;
            }

            try {
                ObjectNode newNode = objectMapper.createObjectNode();
                newNode.put("minute", minute);
                newNode.put("count", maxRequestsPerMinute);
                newNode.put("blocked", true);
                String newValue = objectMapper.writeValueAsString(newNode);

                int affected = llmParameterMapper.casUpdate(RATE_LIMIT_KEY, oldValue, newValue);
                if (affected > 0) {
                    LOGGER.warn("[RateLimit] 外部服务限流，已标记当前分钟不可用: minute={}", minute);
                    return;
                }
                LOGGER.debug("[RateLimit] markBlocked CAS冲突，重试: {}/{}", i + 1, MAX_RETRIES);
            } catch (Exception e) {
                LOGGER.error("[RateLimit] markBlocked失败", e);
                return;
            }
        }
    }
}

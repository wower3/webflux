package com.chat.chart.app.service;

import com.chat.chart.domain.model.AdoptionStatus;
import com.chat.chart.infrastructure.mapper.StatMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 统计服务
 * <p>
 * 提供 daily 维度的统计指标，时间范围为 baseDate前两天23:00 ~ baseDate前一天23:00。
 * </p>
 */
@Service
public class StatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatService.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final StatMapper statMapper;

    public StatService(StatMapper statMapper) {
        this.statMapper = statMapper;
    }

    /**
     * 统计用户量
     *
     * @param baseDate 基准日期，格式 yyyyMMdd
     * @return 用户量
     */
    public String getUserCount(String baseDate) {
        LocalDateTime startTime = parseStartTime(baseDate);
        LocalDateTime endTime = parseEndTime(baseDate);
        LOGGER.info("[Stat] 用户量统计: baseDate={}, range=[{}, {})", baseDate, startTime, endTime);
        int count = statMapper.selectUserCount(startTime, endTime);
        return String.valueOf(count);
    }

    /**
     * 统计交易量（模型回复数）
     *
     * @param baseDate 基准日期，格式 yyyyMMdd
     * @return 交易量
     */
    public String getTransactionCount(String baseDate) {
        LocalDateTime startTime = parseStartTime(baseDate);
        LocalDateTime endTime = parseEndTime(baseDate);
        LOGGER.info("[Stat] 交易量统计: baseDate={}, range=[{}, {})", baseDate, startTime, endTime);
        int count = statMapper.selectTransactionCount(startTime, endTime);
        return String.valueOf(count);
    }

    /**
     * 统计采纳率（1 - 不采纳率）
     * <p>
     * 无评价数据时返回 "1"。
     * </p>
     *
     * @param baseDate 基准日期，格式 yyyyMMdd
     * @return 采纳率
     */
    public String getAdoptionRate(String baseDate) {
        LocalDateTime startTime = parseStartTime(baseDate);
        LocalDateTime endTime = parseEndTime(baseDate);
        LOGGER.info("[Stat] 采纳率统计: baseDate={}, range=[{}, {})", baseDate, startTime, endTime);

        int transactionCount = statMapper.selectTransactionCount(startTime, endTime);
        if (transactionCount == 0) {
            return "1";
        }

        int notAdoptedCount = statMapper.selectNotAdoptedCount(startTime, endTime,
                AdoptionStatus.NOT_ADOPTED.getValue());
        BigDecimal rate = BigDecimal.ONE.subtract(
                new BigDecimal(notAdoptedCount).divide(new BigDecimal(transactionCount), 4, RoundingMode.HALF_UP));
        return rate.toString();
    }

    private LocalDateTime parseStartTime(String baseDate) {
        LocalDate date = LocalDate.parse(baseDate, DATE_FORMATTER);
        return date.minusDays(2).atTime(LocalTime.of(23, 0));
    }

    private LocalDateTime parseEndTime(String baseDate) {
        LocalDate date = LocalDate.parse(baseDate, DATE_FORMATTER);
        return date.minusDays(1).atTime(LocalTime.of(23, 0));
    }
}

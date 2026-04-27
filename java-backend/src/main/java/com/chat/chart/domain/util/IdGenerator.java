package com.chat.chart.domain.util;

import java.util.Random;

/**
 * ID生成工具类
 * <p>
 * 生成格式：13位毫秒时间戳 + "-" + 6位随机后缀，例如 {@code 1745347200000-a3f2k9}。
 * 同一毫秒内36^6（约21.7亿）种组合，冲突概率极低。
 * </p>
 */
public final class IdGenerator {

    private static final Random RANDOM = new Random();

    private static final int SUFFIX_LENGTH = 6;

    private static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private IdGenerator() {
    }

    public static String newConversationId() {
        long ts = System.currentTimeMillis();
        StringBuilder suffix = new StringBuilder(SUFFIX_LENGTH);
        for (int i = 0; i < SUFFIX_LENGTH; i++) {
            suffix.append(CHARS[RANDOM.nextInt(CHARS.length)]);
        }
        return ts + "-" + suffix;
    }
}

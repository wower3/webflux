package com.chat.chart.infrastructure.util;

import java.util.Random;

/**
 * ID生成工具类
 * <p>
 * 生成格式：13位毫秒时间戳 + "-" + 6位随机后缀，例如 {@code 1745347200000-a3f2k9}。
 * 同一毫秒内36^6（约21.7亿）种组合，冲突概率极低。
 * </p>
 *
 * @author Chat Chart System
 */
public final class IdGenerator {

    private static final Random RANDOM = new Random();

    /** 随机后缀长度 */
    private static final int SUFFIX_LENGTH = 6;

    /** 后缀可用字符：小写字母 + 数字 */
    private static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private IdGenerator() {
    }

    /**
     * 生成时间戳前缀的会话ID
     *
     * @return 格式为 {@code 13位时间戳-6位随机后缀} 的唯一ID
     */
    public static String newConversationId() {
        long ts = System.currentTimeMillis();
        StringBuilder suffix = new StringBuilder(SUFFIX_LENGTH);
        for (int i = 0; i < SUFFIX_LENGTH; i++) {
            suffix.append(CHARS[RANDOM.nextInt(CHARS.length)]);
        }
        return ts + "-" + suffix;
    }
}

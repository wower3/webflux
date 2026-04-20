package com.chat.chart.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 中文符号转换工具
 * 将中文标点符号转换为英文标点符号，并标准化JSON格式
 *
 * @author Chat Chart System
 * @version 1.0.0
 */
public class SymbolConverter {

    /**
     * 中文符号 → 英文符号映射
     */
    private static final Map<String, String> SYMBOL_MAP = new HashMap<>();

    static {
        // 中文标点 → 英文标点
        SYMBOL_MAP.put("：", ":");
        SYMBOL_MAP.put("，", ",");
        SYMBOL_MAP.put("【", "[");
        SYMBOL_MAP.put("】", "]");
        SYMBOL_MAP.put("｛", "{");
        SYMBOL_MAP.put("｝", "}");
        SYMBOL_MAP.put("《", "<");
        SYMBOL_MAP.put("》", ">");
        SYMBOL_MAP.put("；", ";");
        SYMBOL_MAP.put("（", "(");
        SYMBOL_MAP.put("）", ")");
        SYMBOL_MAP.put("！", "!");
        SYMBOL_MAP.put("？", "?");
        // 中文引号 → 英文引号
        SYMBOL_MAP.put("\u201c", "\"");  // 左双引号 "
        SYMBOL_MAP.put("\u201d", "\"");  // 右双引号 "
        SYMBOL_MAP.put("\u2018", "'");   // 左单引号 '
        SYMBOL_MAP.put("\u2019", "'");   // 右单引号 '
    }

    /**
     * 正则表达式：匹配JSON中的key
     */
    private static final Pattern KEY_PATTERN = Pattern.compile("(\"[^\"]+\")\\s*:\\s*");

    /**
     * 标准化输入文本
     * 1. 中文符号转英文
     * 2. 标准化 JSON 格式（去除结构性空格）
     *
     * @param text 原始文本
     * @return 标准化后的文本
     */
    public static String normalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // 步骤1：中文符号转英文
        String result = text;
        for (Map.Entry<String, String> entry : SYMBOL_MAP.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        // 步骤2：标准化 JSON 格式
        // 将 "key" : value 或 "key": value 统一为 "key":value（冒号两侧无空格）
        Matcher matcher = KEY_PATTERN.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1) + ":");
        }
        matcher.appendTail(sb);
        result = sb.toString();

        // 将对象或数组前后的空格去除 {  content  } → {content}
        result = result.replaceAll("\\{\\s*", "{");
        result = result.replaceAll("\\s*\\}", "}");
        result = result.replaceAll("\\[\\s*", "[");
        result = result.replaceAll("\\s*\\]", "]");

        // 数组元素之间的逗号后空格去除 [a, b] → [a,b]
        result = result.replaceAll(",\\s+", ",");

        return result;
    }
}

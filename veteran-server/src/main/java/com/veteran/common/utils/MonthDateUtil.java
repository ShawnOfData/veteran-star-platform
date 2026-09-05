package com.veteran.common.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 年月精度日期工具：前端产出 YYYY-MM，后端归一为当月 1 日；
 * 兼容 YYYY-MM-DD 完整日期输入。
 */
public final class MonthDateUtil {

    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyy-MM");

    private MonthDateUtil() {
    }

    /**
     * 归一化年月字符串为当月 1 日的 LocalDate。
     * 支持 "yyyy-MM" / "yyyy-MM-dd" / null(返回null) / 空串(返回null)
     */
    public static LocalDate toFirstDay(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String v = value.trim();
        try {
            if (v.length() == 7) {
                return LocalDate.parse(v + "-01");
            }
            return LocalDate.parse(v);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("日期格式不正确(应为yyyy-MM或yyyy-MM-dd): " + value);
        }
    }
}
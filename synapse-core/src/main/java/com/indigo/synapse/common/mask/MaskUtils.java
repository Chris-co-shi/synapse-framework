package com.indigo.synapse.common.mask;

import java.util.Objects;

/**
 * 常见敏感信息脱敏工具。
 *
 * <p>该工具只处理通用字符串脱敏场景，不校验业务证件、手机号或邮箱归属规则。
 * 入参为空或格式明显不可用时会直接抛出异常，避免静默返回误导性结果。</p>
 */
public final class MaskUtils {

    private MaskUtils() {
    }

    /**
     * 脱敏姓名，保留首尾字符，中间字符替换为星号。
     *
     * @param name 姓名，不能为空白
     * @return 脱敏后的姓名
     */
    public static String maskName(String name) {
        String value = requireText(name, "name");
        if (value.length() == 1) {
            return "*";
        }
        if (value.length() == 2) {
            return value.charAt(0) + "*";
        }
        return value.charAt(0) + repeat(value.length() - 2) + value.charAt(value.length() - 1);
    }

    /**
     * 脱敏手机号，默认保留前 3 位和后 4 位。
     *
     * @param mobile 手机号，不能为空白
     * @return 脱敏后的手机号
     */
    public static String maskMobile(String mobile) {
        return mask(mobile, 3);
    }

    /**
     * 脱敏身份证号，默认保留前 4 位和后 4 位。
     *
     * @param idCard 身份证号，不能为空白
     * @return 脱敏后的身份证号
     */
    public static String maskIdCard(String idCard) {
        return mask(idCard, 4);
    }

    /**
     * 脱敏邮箱地址，保留本地部分首尾字符和完整域名。
     *
     * @param email 邮箱地址，不能为空白，且必须包含本地部分和域名
     * @return 脱敏后的邮箱地址
     */
    public static String maskEmail(String email) {
        String value = requireText(email, "email");
        int atIndex = value.indexOf('@');
        if (atIndex <= 0 || atIndex == value.length() - 1) {
            throw new IllegalArgumentException("email must contain a valid domain");
        }
        String localPart = value.substring(0, atIndex);
        String domainPart = value.substring(atIndex);
        if (localPart.length() == 1) {
            return "*" + domainPart;
        }
        if (localPart.length() == 2) {
            return localPart.charAt(0) + "*" + domainPart;
        }
        return localPart.charAt(0) + repeat(localPart.length() - 2) + localPart.charAt(localPart.length() - 1) + domainPart;
    }

    private static String mask(String value, int prefixVisibleCount) {
        String text = requireText(value, "value");
        int length = text.length();
        if (length <= prefixVisibleCount + 4) {
            return repeat(length);
        }
        return text.substring(0, prefixVisibleCount)
                + repeat(length - prefixVisibleCount - 4)
                + text.substring(length - 4);
    }

    private static String requireText(String value, String fieldName) {
        String text = Objects.requireNonNull(value, fieldName + " must not be null");
        if (text.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return text;
    }

    private static String repeat(int count) {
        return String.valueOf('*').repeat(Math.max(0, count));
    }
}

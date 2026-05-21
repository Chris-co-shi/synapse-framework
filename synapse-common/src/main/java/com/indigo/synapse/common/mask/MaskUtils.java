package com.indigo.synapse.common.mask;

import java.util.Objects;

public final class MaskUtils {

    private MaskUtils() {
    }

    public static String maskName(String name) {
        String value = requireText(name, "name");
        if (value.length() == 1) {
            return "*";
        }
        if (value.length() == 2) {
            return value.charAt(0) + "*";
        }
        return value.charAt(0) + repeat('*', value.length() - 2) + value.charAt(value.length() - 1);
    }

    public static String maskMobile(String mobile) {
        return mask(mobile, 3, 4);
    }

    public static String maskIdCard(String idCard) {
        return mask(idCard, 4, 4);
    }

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
        return localPart.charAt(0) + repeat('*', localPart.length() - 2) + localPart.charAt(localPart.length() - 1) + domainPart;
    }

    private static String mask(String value, int prefixVisibleCount, int suffixVisibleCount) {
        String text = requireText(value, "value");
        int length = text.length();
        if (length <= prefixVisibleCount + suffixVisibleCount) {
            return repeat('*', length);
        }
        return text.substring(0, prefixVisibleCount)
                + repeat('*', length - prefixVisibleCount - suffixVisibleCount)
                + text.substring(length - suffixVisibleCount);
    }

    private static String requireText(String value, String fieldName) {
        String text = Objects.requireNonNull(value, fieldName + " must not be null");
        if (text.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return text;
    }

    private static String repeat(char ch, int count) {
        return String.valueOf(ch).repeat(Math.max(0, count));
    }
}

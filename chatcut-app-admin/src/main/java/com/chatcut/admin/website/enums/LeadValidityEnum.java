package com.chatcut.admin.website.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 线索有效性判断，独立于流程状态。
 */
@Getter
@AllArgsConstructor
public enum LeadValidityEnum {

    UNCONFIRMED("UNCONFIRMED", "待确认"),
    VALID("VALID", "有效"),
    INVALID("INVALID", "无效");

    private final String code;
    private final String label;

    public static LeadValidityEnum of(String code) {
        return Arrays.stream(values()).filter(e -> e.code.equals(code)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("非法有效性: " + code));
    }

    public static boolean isValid(String code) {
        return Arrays.stream(values()).anyMatch(e -> e.code.equals(code));
    }
}

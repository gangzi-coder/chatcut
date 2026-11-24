package com.chatcut.admin.website.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 反馈类别。
 */
@Getter
@AllArgsConstructor
public enum FeedbackCategoryEnum {

    PAGE_ISSUE("PAGE_ISSUE", "页面问题"),
    USAGE_ISSUE("USAGE_ISSUE", "使用问题"),
    CONTENT_SUGGESTION("CONTENT_SUGGESTION", "内容建议"),
    PARTNERSHIP("PARTNERSHIP", "合作咨询"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String label;

    public static FeedbackCategoryEnum of(String code) {
        return Arrays.stream(values()).filter(e -> e.code.equals(code)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("非法反馈类别: " + code));
    }

    public static boolean isValid(String code) {
        return Arrays.stream(values()).anyMatch(e -> e.code.equals(code));
    }
}

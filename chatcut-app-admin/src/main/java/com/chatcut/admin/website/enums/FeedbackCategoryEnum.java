package com.chatcut.admin.website.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 反馈类别。
 */
@Getter
@AllArgsConstructor
public enum FeedbackCategoryEnum implements EnumCodeLabel {

    PAGE_ISSUE("PAGE_ISSUE", "页面问题", "Page Issue"),
    USAGE_ISSUE("USAGE_ISSUE", "使用问题", "Usage Issue"),
    CONTENT_SUGGESTION("CONTENT_SUGGESTION", "内容建议", "Content Suggestion"),
    PARTNERSHIP("PARTNERSHIP", "合作咨询", "Partnership"),
    OTHER("OTHER", "其他", "Other");

    private final String code;
    private final String label;
    private final String labelEn;

    public static FeedbackCategoryEnum of(String code) {
        return Arrays.stream(values()).filter(e -> e.code.equals(code)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("非法反馈类别: " + code));
    }

    public static boolean isValid(String code) {
        return Arrays.stream(values()).anyMatch(e -> e.code.equals(code));
    }
}

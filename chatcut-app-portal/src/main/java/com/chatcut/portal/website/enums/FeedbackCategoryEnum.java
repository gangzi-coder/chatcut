package com.chatcut.portal.website.enums;

import com.chatcut.common.core.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 反馈类别（portal 只读副本，与 admin 端枚举保持同构）。
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
            .orElseThrow(() -> new ServiceException("非法反馈类别: " + code));
    }
}

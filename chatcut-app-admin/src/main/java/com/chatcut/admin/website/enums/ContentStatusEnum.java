package com.chatcut.admin.website.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 内容主表当前状态。
 */
@Getter
@AllArgsConstructor
public enum ContentStatusEnum implements EnumCodeLabel {

    DRAFT("DRAFT", "草稿", "Draft"),
    PUBLISHED("PUBLISHED", "已发布", "Published"),
    OFFLINE("OFFLINE", "已下线", "Offline");

    private final String code;
    private final String label;
    private final String labelEn;

    public static ContentStatusEnum of(String code) {
        return Arrays.stream(values()).filter(e -> e.code.equals(code)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("非法内容状态: " + code));
    }
}

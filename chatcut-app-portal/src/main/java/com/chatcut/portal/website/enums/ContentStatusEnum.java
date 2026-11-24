package com.chatcut.portal.website.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 内容主表当前状态（portal 只读副本，与 admin 端枚举保持同构）。
 */
@Getter
@AllArgsConstructor
public enum ContentStatusEnum {

    DRAFT("DRAFT", "草稿"),
    PUBLISHED("PUBLISHED", "已发布"),
    OFFLINE("OFFLINE", "已下线");

    private final String code;
    private final String label;

    public static ContentStatusEnum of(String code) {
        return Arrays.stream(values()).filter(e -> e.code.equals(code)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("非法内容状态: " + code));
    }
}

package com.chatcut.admin.website.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 素材状态。删除为逻辑删除，不删除 OSS 原文件。
 */
@Getter
@AllArgsConstructor
public enum AssetStatusEnum implements EnumCodeLabel {

    ACTIVE("ACTIVE", "可用", "Available"),
    DELETED("DELETED", "已删除", "Deleted");

    private final String code;
    private final String label;
    private final String labelEn;

    public static boolean isValid(String code) {
        return Arrays.stream(values()).anyMatch(e -> e.code.equals(code));
    }
}

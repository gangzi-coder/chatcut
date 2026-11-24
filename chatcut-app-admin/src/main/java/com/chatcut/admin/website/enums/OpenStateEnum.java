package com.chatcut.admin.website.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 开放状态：预告可收集预约；产品标"可用"时必须配置正式入口。
 */
@Getter
@AllArgsConstructor
public enum OpenStateEnum implements EnumCodeLabel {

    PREVIEW("PREVIEW", "预告", "Preview"),
    AVAILABLE("AVAILABLE", "可用", "Available"),
    PAUSED("PAUSED", "暂停", "Paused");

    private final String code;
    private final String label;
    private final String labelEn;

    public static OpenStateEnum of(String code) {
        return Arrays.stream(values()).filter(e -> e.code.equals(code)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("非法开放状态: " + code));
    }

    public static boolean isValid(String code) {
        return Arrays.stream(values()).anyMatch(e -> e.code.equals(code));
    }
}

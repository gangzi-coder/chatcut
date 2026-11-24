package com.chatcut.admin.website.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 联系方式类型。联系方式明文存储，不加密。
 */
@Getter
@AllArgsConstructor
public enum ContactTypeEnum implements EnumCodeLabel {

    PHONE("PHONE", "手机号", "Phone"),
    EMAIL("EMAIL", "邮箱", "Email");

    private final String code;
    private final String label;
    private final String labelEn;

    public static ContactTypeEnum of(String code) {
        return Arrays.stream(values()).filter(e -> e.code.equals(code)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("非法联系方式类型: " + code));
    }

    public static boolean isValid(String code) {
        return Arrays.stream(values()).anyMatch(e -> e.code.equals(code));
    }
}

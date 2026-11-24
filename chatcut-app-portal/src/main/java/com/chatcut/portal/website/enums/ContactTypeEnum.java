package com.chatcut.portal.website.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 联系方式类型（portal 只读副本）。联系方式明文存储，不加密。
 */
@Getter
@AllArgsConstructor
public enum ContactTypeEnum {

    PHONE("PHONE", "手机号"),
    EMAIL("EMAIL", "邮箱");

    private final String code;
    private final String label;

    public static ContactTypeEnum of(String code) {
        return Arrays.stream(values()).filter(e -> e.code.equals(code)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("非法联系方式类型: " + code));
    }
}

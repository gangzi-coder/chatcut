package com.chatcut.admin.website.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 内容类型。已有内容的类型固定不可修改，调整需新建内容。
 */
@Getter
@AllArgsConstructor
public enum ContentTypeEnum {

    PAGE("PAGE", "页面区块"),
    NOTICE("NOTICE", "公告"),
    CASE("CASE", "展示案例"),
    RESOURCE("RESOURCE", "资源文档"),
    POLICY("POLICY", "法律条款"),
    PRODUCT("PRODUCT", "产品方案");

    private final String code;
    private final String label;

    public static ContentTypeEnum of(String code) {
        return Arrays.stream(values()).filter(e -> e.code.equals(code)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("非法内容类型: " + code));
    }

    public static boolean isValid(String code) {
        return Arrays.stream(values()).anyMatch(e -> e.code.equals(code));
    }
}

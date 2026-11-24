package com.chatcut.admin.website.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 内容类型。已有内容的类型固定不可修改，调整需新建内容。
 */
@Getter
@AllArgsConstructor
public enum ContentTypeEnum implements EnumCodeLabel {

    PAGE("PAGE", "页面区块", "Page Section"),
    NOTICE("NOTICE", "公告", "Notice"),
    CASE("CASE", "展示案例", "Case Study"),
    RESOURCE("RESOURCE", "资源文档", "Resource"),
    POLICY("POLICY", "法律条款", "Legal & Policy"),
    PRODUCT("PRODUCT", "产品方案", "Product Plan");

    private final String code;
    private final String label;
    private final String labelEn;

    public static ContentTypeEnum of(String code) {
        return Arrays.stream(values()).filter(e -> e.code.equals(code)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("非法内容类型: " + code));
    }

    public static boolean isValid(String code) {
        return Arrays.stream(values()).anyMatch(e -> e.code.equals(code));
    }
}

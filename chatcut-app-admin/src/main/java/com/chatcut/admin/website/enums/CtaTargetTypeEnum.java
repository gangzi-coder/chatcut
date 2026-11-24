package com.chatcut.admin.website.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 按钮目标类型，对应 lead: / content: / https:// 三种前缀。
 */
@Getter
@AllArgsConstructor
public enum CtaTargetTypeEnum {

    LEAD("LEAD", "留资咨询"),
    CONTENT("CONTENT", "站内跳转"),
    EXTERNAL("EXTERNAL", "外链");

    private final String code;
    private final String label;
}

package com.chatcut.admin.website.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 按钮目标类型，对应 lead: / content: / https:// 三种前缀。
 */
@Getter
@AllArgsConstructor
public enum CtaTargetTypeEnum implements EnumCodeLabel {

    LEAD("LEAD", "留资咨询", "Lead Form"),
    CONTENT("CONTENT", "站内跳转", "In-site Link"),
    EXTERNAL("EXTERNAL", "外链", "External Link");

    private final String code;
    private final String label;
    private final String labelEn;
}

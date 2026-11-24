package com.chatcut.admin.website.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 发布版本生命周期。PUBLISHED 表示当前或曾发布；ARCHIVED 表示被新版本替代。
 * 下线不改变版本状态（历史保留），归档只发生在新版本发布时。
 */
@Getter
@AllArgsConstructor
public enum VersionStatusEnum {

    PUBLISHED("PUBLISHED", "已发布"),
    ARCHIVED("ARCHIVED", "已归档");

    private final String code;
    private final String label;
}

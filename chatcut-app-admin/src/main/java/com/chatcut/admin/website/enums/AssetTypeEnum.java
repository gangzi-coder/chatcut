package com.chatcut.admin.website.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 素材类型：文本 / 视频 / 声音。单文件大小由前端控制，后端不校验。
 */
@Getter
@AllArgsConstructor
public enum AssetTypeEnum implements EnumCodeLabel {

    TEXT("TEXT", "文本", "Text"),
    VIDEO("VIDEO", "视频", "Video"),
    AUDIO("AUDIO", "声音", "Audio");

    private final String code;
    private final String label;
    private final String labelEn;

    public static AssetTypeEnum of(String code) {
        return Arrays.stream(values()).filter(e -> e.code.equals(code)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("非法素材类型: " + code));
    }

    public static boolean isValid(String code) {
        return Arrays.stream(values()).anyMatch(e -> e.code.equals(code));
    }
}

package com.chatcut.admin.website.support;

import com.chatcut.admin.website.enums.EnumCodeLabel;

/**
 * 枚举展示标签转换工具：把数据库存储的枚举编码转换为中/英文展示标签。
 *
 * <p>用于 Resp 视图对象的派生展示字段（如 status + statusZh + statusEn），
 * 未知编码或空值安全返回，不抛异常。</p>
 */
public final class EnumLabelUtils {

    private EnumLabelUtils() {
    }

    /**
     * 按编码查找枚举的中文展示标签。
     *
     * @param values 枚举全量值（调用方传 XxxEnum.values()）
     * @param code   数据库存储的枚举编码，可为 null
     * @return 匹配枚举的中文标签；编码为空返回 null；未匹配返回原编码兜底
     */
    public static String labelZh(EnumCodeLabel[] values, String code) {
        return resolve(values, code, EnumCodeLabel::getLabel);
    }

    /**
     * 按编码查找枚举的英文展示标签。
     *
     * @param values 枚举全量值（调用方传 XxxEnum.values()）
     * @param code   数据库存储的枚举编码，可为 null
     * @return 匹配枚举的英文标签；编码为空返回 null；未匹配返回原编码兜底
     */
    public static String labelEn(EnumCodeLabel[] values, String code) {
        return resolve(values, code, EnumCodeLabel::getLabelEn);
    }

    /** 统一查找：编码为空返回 null，未匹配返回原编码兜底（保证前端始终有内容且便于排查脏数据）。 */
    private static String resolve(EnumCodeLabel[] values, String code,
                                  java.util.function.Function<EnumCodeLabel, String> labelGetter) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (EnumCodeLabel item : values) {
            if (item.getCode().equals(code)) {
                return labelGetter.apply(item);
            }
        }
        return code;
    }
}

package com.chatcut.admin.website.enums;

/**
 * 官网域枚举统一契约：所有枚举均携带 code（入库值）、label（中文展示）、labelEn（英文展示）。
 * 供 {@link com.chatcut.admin.website.support.EnumLabelUtils} 统一转换展示字段使用。
 */
public interface EnumCodeLabel {

    /** 入库存储的枚举编码。 */
    String getCode();

    /** 面向前端展示的中文标签。 */
    String getLabel();

    /** 面向前端展示的英文标签。 */
    String getLabelEn();
}

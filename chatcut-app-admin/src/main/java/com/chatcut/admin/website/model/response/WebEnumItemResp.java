package com.chatcut.admin.website.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 枚举选项视图对象，供前端下拉选择。
 */
@Data
@AllArgsConstructor
public class WebEnumItemResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 枚举值（存储 code）。 */
    private String value;

    /** 展示文案。 */
    private String label;
}

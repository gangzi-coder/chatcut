package com.chatcut.admin.website.model.response;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import com.chatcut.admin.website.repository.entity.WebAssetEntity;

import java.io.Serial;
import java.io.Serializable;
import com.chatcut.admin.website.support.EnumLabelUtils;
import com.chatcut.admin.website.enums.AssetTypeEnum;
import com.chatcut.admin.website.enums.AssetStatusEnum;
import java.time.LocalDateTime;

/**
 * 官网展示素材视图对象 web_asset
 */
@Data
@AutoMapper(target = WebAssetEntity.class)
public class WebAssetResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    /** 素材名称。 */
    private String name;

    /** 素材类型：TEXT/VIDEO/AUDIO。 */
    private String assetType;

    /** 预览展示色。 */
    private String color;

    /** OSS 对象键。 */
    private String objectKey;

    /** 公开访问地址。 */
    private String publicUrl;

    /** MIME 类型。 */
    private String mimeType;

    /** 素材说明。 */
    private String description;

    /** 素材状态：ACTIVE/DELETED。 */
    private String status;

    /** 创建管理员 ID。 */
    private String createdBy;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;

    // ---------------- 枚举中英文展示标签（派生字段，仅用于前端展示，不落库） ----------------

    /** 素材类型中文标签。 */
    public String getAssetTypeZh() {
        return EnumLabelUtils.labelZh(AssetTypeEnum.values(), assetType);
    }

    /** 素材类型英文标签。 */
    public String getAssetTypeEn() {
        return EnumLabelUtils.labelEn(AssetTypeEnum.values(), assetType);
    }

    /** 素材状态中文标签。 */
    public String getStatusZh() {
        return EnumLabelUtils.labelZh(AssetStatusEnum.values(), status);
    }

    /** 素材状态英文标签。 */
    public String getStatusEn() {
        return EnumLabelUtils.labelEn(AssetStatusEnum.values(), status);
    }
}

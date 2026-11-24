package com.chatcut.admin.website.model.response;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import com.chatcut.admin.website.repository.entity.WebAssetEntity;

import java.io.Serial;
import java.io.Serializable;
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
}

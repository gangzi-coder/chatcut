package com.chatcut.portal.website.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 官网展示素材表 web_asset（portal 侧只读副本）。用于公开内容详情解析媒体信息。
 */
@Data
@TableName("web_asset")
public class WebAssetEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 素材名称。 */
    private String name;

    /** 素材类型：TEXT/VIDEO/AUDIO。 */
    private String assetType;

    /** 预览展示色，六位十六进制。 */
    private String color;

    /** OSS 对象键。 */
    private String objectKey;

    /** 公开访问地址。 */
    private String publicUrl;

    /** 媒体 MIME 类型。 */
    private String mimeType;

    /** 素材说明。 */
    private String description;

    /** 素材状态：ACTIVE/DELETED。 */
    private String status;

    /** 创建管理员 ID。 */
    private Long createdBy;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;

    /** 删除标志：0正常，1已删除（全局逻辑删除字段）。 */
    private String delFlag;

    /** 乐观锁版本号。 */
    @Version
    private Integer version;
}

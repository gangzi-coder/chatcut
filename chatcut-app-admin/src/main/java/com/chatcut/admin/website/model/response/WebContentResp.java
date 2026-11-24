package com.chatcut.admin.website.model.response;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import com.chatcut.admin.website.repository.entity.WebContentEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 官网内容视图对象 web_content
 */
@Data
@AutoMapper(target = WebContentEntity.class)
public class WebContentResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    /** 内容类型：PAGE/NOTICE/CASE/RESOURCE/POLICY/PRODUCT。 */
    private String contentType;

    /** 内容分组。 */
    private String groupKey;

    /** 当前展示标题。 */
    private String title;

    /** 内容状态：DRAFT/PUBLISHED/OFFLINE。 */
    private String status;

    /** 当前正式发布版本号。 */
    private Integer publishedVersion;

    /** 创建管理员 ID。 */
    private String createdBy;

    /** 最后更新管理员 ID。 */
    private String updatedBy;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;

    /** 乐观锁版本号（详情/编辑时回传）。 */
    private Integer version;
}

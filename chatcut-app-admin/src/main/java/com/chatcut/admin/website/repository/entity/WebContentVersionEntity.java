package com.chatcut.admin.website.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 官网内容发布版本表 web_content_version。
 * 发布快照摊平为列；历史版本不可原地修改，恢复时读取本表写回草稿表。
 */
@Data
@TableName("web_content_version")
public class WebContentVersionEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 内容 ID。 */
    private Long contentId;

    /** 版本号，从 1 递增，内容内唯一。 */
    private Integer versionNo;

    /** 发布标题。 */
    private String title;

    /** 发布摘要。 */
    private String summary;

    /** 发布正文。 */
    private String body;

    /** 按钮文案。 */
    private String ctaLabel;

    /** 按钮目标。 */
    private String ctaTarget;

    /** 关联素材 ID。 */
    private Long mediaId;

    /** 开放状态。 */
    private String openState;

    /** 版本状态：PUBLISHED 当前或曾发布，ARCHIVED 已归档。 */
    private String publishStatus;

    /** 发布时间。 */
    private LocalDateTime publishedAt;

    /** 发布管理员 ID。 */
    private Long createdBy;

    /** 记录创建时间。 */
    private LocalDateTime createdAt;
}

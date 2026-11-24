package com.chatcut.admin.website.repository.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 官网内容草稿表 web_content_draft。一内容一条草稿，发布或恢复后覆盖。
 * 业务字段 updateStrategy=ALWAYS：保存草稿是全量覆盖语义，用户删掉摘要/CTA 后
 * 保存必须能把库里旧值清空（MyBatis-Plus 默认 NOT_NULL 策略会跳过 null 字段导致残留）。
 */
@Data
@TableName("web_content_draft")
public class WebContentDraftEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 内容 ID，对应 web_content.id，唯一。 */
    private Long contentId;

    /** 草稿标题。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String title;

    /** 摘要，发布前必填。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String summary;

    /** 正文，纯文本，换行保留。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String body;

    /** 按钮文案，与 ctaTarget 成对。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String ctaLabel;

    /** 按钮目标：lead:意向 / content:内容ID / https://地址。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String ctaTarget;

    /** 关联素材 ID。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long mediaId;

    /** 开放状态：PREVIEW/AVAILABLE/PAUSED。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String openState;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}

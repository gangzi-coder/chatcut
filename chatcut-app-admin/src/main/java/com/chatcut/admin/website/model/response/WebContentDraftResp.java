package com.chatcut.admin.website.model.response;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import com.chatcut.admin.website.repository.entity.WebContentDraftEntity;

import java.io.Serial;
import java.io.Serializable;
import com.chatcut.admin.website.support.EnumLabelUtils;
import com.chatcut.admin.website.enums.OpenStateEnum;
import java.time.LocalDateTime;

/**
 * 官网内容草稿视图对象 web_content_draft
 */
@Data
@AutoMapper(target = WebContentDraftEntity.class)
public class WebContentDraftResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    private String contentId;

    /** 草稿标题。 */
    private String title;

    /** 摘要。 */
    private String summary;

    /** 正文，纯文本。 */
    private String body;

    /** 按钮文案。 */
    private String ctaLabel;

    /** 按钮目标。 */
    private String ctaTarget;

    /** 关联素材 ID。 */
    private String mediaId;

    /** 开放状态：PREVIEW/AVAILABLE/PAUSED。 */
    private String openState;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;

    // ---------------- 枚举中英文展示标签（派生字段，仅用于前端展示，不落库） ----------------

    /** 开放状态中文标签。 */
    public String getOpenStateZh() {
        return EnumLabelUtils.labelZh(OpenStateEnum.values(), openState);
    }

    /** 开放状态英文标签。 */
    public String getOpenStateEn() {
        return EnumLabelUtils.labelEn(OpenStateEnum.values(), openState);
    }
}

package com.chatcut.admin.website.model.response;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import com.chatcut.admin.website.repository.entity.WebContentVersionEntity;

import java.io.Serial;
import java.io.Serializable;
import com.chatcut.admin.website.support.EnumLabelUtils;
import com.chatcut.admin.website.enums.OpenStateEnum;
import com.chatcut.admin.website.enums.VersionStatusEnum;
import java.time.LocalDateTime;

/**
 * 官网内容发布版本视图对象 web_content_version
 */
@Data
@AutoMapper(target = WebContentVersionEntity.class)
public class WebContentVersionResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    private String contentId;

    /** 版本号。 */
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
    private String mediaId;

    /** 开放状态。 */
    private String openState;

    /** 版本状态：PUBLISHED/ARCHIVED。 */
    private String publishStatus;

    /** 发布时间。 */
    private LocalDateTime publishedAt;

    /** 发布管理员 ID。 */
    private String createdBy;

    // ---------------- 枚举中英文展示标签（派生字段，仅用于前端展示，不落库） ----------------

    /** 开放状态中文标签。 */
    public String getOpenStateZh() {
        return EnumLabelUtils.labelZh(OpenStateEnum.values(), openState);
    }

    /** 开放状态英文标签。 */
    public String getOpenStateEn() {
        return EnumLabelUtils.labelEn(OpenStateEnum.values(), openState);
    }

    /** 版本状态中文标签。 */
    public String getPublishStatusZh() {
        return EnumLabelUtils.labelZh(VersionStatusEnum.values(), publishStatus);
    }

    /** 版本状态英文标签。 */
    public String getPublishStatusEn() {
        return EnumLabelUtils.labelEn(VersionStatusEnum.values(), publishStatus);
    }
}

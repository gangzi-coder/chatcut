package com.chatcut.admin.website.model.response;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import com.chatcut.admin.website.repository.entity.CrmFeedbackEntity;

import java.io.Serial;
import java.io.Serializable;
import com.chatcut.admin.website.support.EnumLabelUtils;
import com.chatcut.admin.website.enums.FeedbackCategoryEnum;
import com.chatcut.admin.website.enums.ContactTypeEnum;
import com.chatcut.admin.website.enums.FeedbackStatusEnum;
import java.time.LocalDateTime;

/**
 * 官网访客反馈视图对象 crm_feedback
 */
@Data
@AutoMapper(target = CrmFeedbackEntity.class)
public class CrmFeedbackResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    /** 访客提交幂等标识，与 crm_lead_submission.request_id 同源。 */
    private String requestId;

    /** 反馈类别 code。 */
    private String category;

    /** 反馈内容。 */
    private String body;

    /** 联系方式明文，可空。 */
    private String contact;

    /** 联系方式类型，可空。 */
    private String contactType;

    /** 反馈来源。 */
    private String source;

    /** 处理状态。 */
    private String status;

    /** 处理人管理员 ID。 */
    private String ownerAdminId;

    /** 处理结果/不予处理原因。 */
    private String result;

    /** 转化后关联线索 ID。 */
    private String leadId;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;

    /** 乐观锁版本号。 */
    private Integer version;

    // ---------------- 枚举中英文展示标签（派生字段，仅用于前端展示，不落库） ----------------

    /** 分类中文标签。 */
    public String getCategoryZh() {
        return EnumLabelUtils.labelZh(FeedbackCategoryEnum.values(), category);
    }

    /** 分类英文标签。 */
    public String getCategoryEn() {
        return EnumLabelUtils.labelEn(FeedbackCategoryEnum.values(), category);
    }

    /** 联系方式类型中文标签。 */
    public String getContactTypeZh() {
        return EnumLabelUtils.labelZh(ContactTypeEnum.values(), contactType);
    }

    /** 联系方式类型英文标签。 */
    public String getContactTypeEn() {
        return EnumLabelUtils.labelEn(ContactTypeEnum.values(), contactType);
    }

    /** 处理状态中文标签。 */
    public String getStatusZh() {
        return EnumLabelUtils.labelZh(FeedbackStatusEnum.values(), status);
    }

    /** 处理状态英文标签。 */
    public String getStatusEn() {
        return EnumLabelUtils.labelEn(FeedbackStatusEnum.values(), status);
    }
}

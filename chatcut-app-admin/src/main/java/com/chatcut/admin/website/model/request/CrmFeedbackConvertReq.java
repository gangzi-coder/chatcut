package com.chatcut.admin.website.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 反馈转线索请求体。转化与关联在同一事务完成，反馈状态保持原状态。
 * 企业类意向（ENTERPRISE/API_TRIAL/TEAM_PLAN/CUSTOM）必须填写联系人姓名与公司名称。
 */
@Data
public class CrmFeedbackConvertReq {

    /** 反馈主键，前端传 String。 */
    @NotBlank(message = "主键不能为空")
    private String id;

    /** 联系人姓名。 */
    @NotBlank(message = "联系人姓名不能为空")
    @Size(max = 80, message = "联系人姓名长度不能超过{max}个字符")
    private String contactName;

    /** 公司名称。 */
    @NotBlank(message = "公司名称不能为空")
    @Size(max = 120, message = "公司名称长度不能超过{max}个字符")
    private String companyName;

    /** 联系方式明文：手机号或邮箱。 */
    @NotBlank(message = "联系方式不能为空")
    @Size(max = 200, message = "联系方式长度不能超过{max}个字符")
    private String contact;

    /** 联系方式类型：PHONE/EMAIL。 */
    @NotBlank(message = "联系方式类型不能为空")
    private String contactType;

    /** 咨询意向 code。 */
    @NotBlank(message = "咨询意向不能为空")
    private String intent;

    /** 线索来源，可空，默认取反馈来源。 */
    @Size(max = 200, message = "来源长度不能超过{max}个字符")
    private String source;
}

package com.chatcut.portal.website.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 留资提交记录表 crm_lead_submission（portal 侧写入副本）。
 * 令牌由后端生成并一次性核销；request_id 与 captcha_token 双唯一键兜底防重复。
 */
@Data
@TableName("crm_lead_submission")
public class CrmLeadSubmissionEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 防重复提交标识，前端生成，唯一。 */
    private String requestId;

    /** 后端生成的一次性验证码令牌，校验后作废，唯一。 */
    private String captchaToken;

    /** 线索 ID。 */
    private Long leadId;

    /** 本次提交来源，前端传入。 */
    private String source;

    /** 本次补充需求原文。 */
    private String content;

    /** 提交时间。 */
    private LocalDateTime submittedAt;

    /** 客户端 IP HMAC 摘要，仅用于限流。 */
    private byte[] ipHash;
}

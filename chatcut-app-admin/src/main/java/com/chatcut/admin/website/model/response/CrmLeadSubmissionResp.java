package com.chatcut.admin.website.model.response;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import com.chatcut.admin.website.repository.entity.CrmLeadSubmissionEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 留资提交记录视图对象 crm_lead_submission。不含 IP 摘要。
 */
@Data
@AutoMapper(target = CrmLeadSubmissionEntity.class)
public class CrmLeadSubmissionResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    /** 本次提交来源。 */
    private String source;

    /** 本次补充需求原文。 */
    private String content;

    /** 提交时间。 */
    private LocalDateTime submittedAt;
}

package com.chatcut.portal.website.repository.vo;

import com.chatcut.portal.website.repository.entity.CrmLeadSubmissionEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 留资提交记录 Vo（仅供 Mapper 泛型使用）。
 */
@Data
@AutoMapper(target = CrmLeadSubmissionEntity.class)
public class CrmLeadSubmissionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String requestId;

    private String captchaToken;

    private Long leadId;
}

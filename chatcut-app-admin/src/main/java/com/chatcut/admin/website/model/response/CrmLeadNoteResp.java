package com.chatcut.admin.website.model.response;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import com.chatcut.admin.website.repository.entity.CrmLeadNoteEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 线索内部跟进记录视图对象 crm_lead_note
 */
@Data
@AutoMapper(target = CrmLeadNoteEntity.class)
public class CrmLeadNoteResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    private String leadId;

    /** 记录作者管理员 ID。 */
    private String authorAdminId;

    /** 内部跟进内容。 */
    private String note;

    /** 记录时间。 */
    private LocalDateTime createdAt;
}

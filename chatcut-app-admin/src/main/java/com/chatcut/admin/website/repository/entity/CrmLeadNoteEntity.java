package com.chatcut.admin.website.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 线索内部跟进记录表 crm_lead_note。追加不可编辑/删除。
 */
@Data
@TableName("crm_lead_note")
public class CrmLeadNoteEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 线索 ID。 */
    private Long leadId;

    /** 记录作者管理员 ID。 */
    private Long authorAdminId;

    /** 内部跟进内容，不向访客发送。 */
    private String note;

    /** 记录时间。 */
    private LocalDateTime createdAt;
}

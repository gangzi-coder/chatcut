package com.chatcut.admin.website.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 添加线索跟进记录请求体。追加后不可编辑/删除。
 */
@Data
public class CrmLeadNoteCreateReq {

    /** 线索主键，前端传 String。 */
    @NotBlank(message = "主键不能为空")
    private String leadId;

    /** 内部跟进内容，追加后不可编辑/删除。 */
    @NotBlank(message = "跟进内容不能为空")
    @Size(max = 4000, message = "跟进内容长度不能超过{max}个字符")
    private String note;
}

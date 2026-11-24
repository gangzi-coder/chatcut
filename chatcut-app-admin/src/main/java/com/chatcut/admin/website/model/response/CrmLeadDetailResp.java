package com.chatcut.admin.website.model.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 线索详情：线索 + 提交历史 + 跟进记录。
 */
@Data
public class CrmLeadDetailResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private CrmLeadResp lead;

    /** 留资提交历史，按提交时间倒序。 */
    private List<CrmLeadSubmissionResp> submissions;

    /** 内部跟进记录，按记录时间正序。 */
    private List<CrmLeadNoteResp> notes;
}

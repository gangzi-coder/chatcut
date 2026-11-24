package com.chatcut.portal.website.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 访客留资提交结果。只返回业务线索编号（对外展示编号），不返回内部主键与内部状态。
 */
@Data
@Schema(description = "访客留资提交结果（不返回内部主键与内部状态）")
public class PortalCrmLeadSubmitResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 线索编号（对外展示编号），重复提交时返回原线索编号。 */
    @Schema(description = "线索编号（对外展示编号），重复提交时返回原线索编号", example = "LD202611241425305446")
    private String leadNo;

    /** 提交时间。 */
    @Schema(description = "提交时间（yyyy-MM-dd HH:mm:ss）", example = "2026-11-24 14:25:30")
    private LocalDateTime submittedAt;

    /** 是否命中重复提交（幂等返回原线索语义）。 */
    @Schema(description = "是否命中重复提交（true=同 requestId 重放，幂等返回原线索）", example = "false")
    private Boolean duplicate;
}

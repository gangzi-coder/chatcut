package com.chatcut.portal.website.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 访客反馈提交结果。不返回内部主键与处理状态。
 */
@Data
@Schema(description = "访客反馈提交结果（不返回内部主键与处理状态）")
public class PortalCrmFeedbackSubmitResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 是否命中重复提交（幂等返回）。 */
    @Schema(description = "是否命中重复提交（true=同 requestId 重放，幂等返回）", example = "false")
    private Boolean duplicate;
}

package com.chatcut.admin.website.model.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 工作概览计数。实时聚合，不做缓存。
 */
@Data
public class WebOverviewResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 待处理线索数（PENDING）。 */
    private int pendingLeads;

    /** 有效线索数（validity = VALID）。 */
    private int validLeads;

    /** 待处理反馈数（PENDING）。 */
    private int pendingFeedbacks;

    /** 已发布内容数（PUBLISHED）。 */
    private int publishedContents;

    /** 待发布草稿数（有草稿且主表状态为 DRAFT）。 */
    private int draftContents;
}

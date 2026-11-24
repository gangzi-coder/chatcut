package com.chatcut.admin.website.model.request;

import lombok.Data;

/**
 * 内容筛选请求参数。
 */
@Data
public class WebContentListReq {

    /** 内容类型，可空。 */
    private String contentType;

    /** 内容分组，可空。 */
    private String groupKey;

    /** 内容状态：DRAFT/PUBLISHED/OFFLINE，可空。 */
    private String status;

    /** 标题关键词，模糊匹配，可空。 */
    private String keyword;
}

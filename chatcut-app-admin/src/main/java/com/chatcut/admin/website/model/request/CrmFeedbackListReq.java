package com.chatcut.admin.website.model.request;

import lombok.Data;

/**
 * 反馈筛选请求参数。
 */
@Data
public class CrmFeedbackListReq {

    /** 处理状态，可空。 */
    private String status;

    /** 反馈类别，可空。 */
    private String category;

    /** 关键词：匹配反馈内容/联系方式，可空。 */
    private String keyword;
}

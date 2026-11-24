package com.chatcut.admin.website.model.request;

import lombok.Data;

/**
 * 线索筛选请求参数。
 */
@Data
public class CrmLeadListReq {

    /** 线索状态，可空。 */
    private String status;

    /** 咨询意向，可空。 */
    private String intent;

    /** 有效性，可空。 */
    private String validity;

    /** 负责人管理员 ID，可空。 */
    private String ownerAdminId;

    /** 关键词：匹配线索编号/公司/联系人/联系方式，可空。 */
    private String keyword;
}

package com.chatcut.admin.website.model.request;

import lombok.Data;

/**
 * 素材筛选请求参数。
 */
@Data
public class WebAssetListReq {

    /** 素材类型，可空。 */
    private String assetType;

    /** 素材状态：ACTIVE/DELETED，可空。 */
    private String status;

    /** 名称关键词，模糊匹配，可空。 */
    private String keyword;
}

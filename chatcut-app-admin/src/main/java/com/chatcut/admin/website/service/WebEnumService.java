package com.chatcut.admin.website.service;

import com.chatcut.admin.website.model.response.WebEnumItemResp;

import java.util.List;
import java.util.Map;

/**
 * 官网公共枚举查询，供前端下拉选择。
 */
public interface WebEnumService {

    /**
     * 返回官网域全部枚举选项，key 为枚举组名（如 ContentType、LeadStatus）。
     */
    Map<String, List<WebEnumItemResp>> getWebEnums();
}

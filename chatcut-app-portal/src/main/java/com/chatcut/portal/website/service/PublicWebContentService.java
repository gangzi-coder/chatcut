package com.chatcut.portal.website.service;

import com.chatcut.portal.website.model.response.PublicWebContentDetailResp;
import com.chatcut.portal.website.model.response.PublicWebContentItemResp;

import java.util.List;

/**
 * 官网公开内容服务：免登录读取已发布内容。
 */
public interface PublicWebContentService {

    /**
     * 公开内容列表：只返回 PUBLISHED 且非删除的内容，按创建时间倒序（新内容在前）。
     *
     * @param contentType 内容类型筛选，可空
     * @param groupKey    内容分组筛选，可空
     * @return 公开列表项（不分页，官网展示列表一次返回全部可用条目）
     */
    List<PublicWebContentItemResp> listPublicWebContents(String contentType, String groupKey);

    /**
     * 公开内容详情：读取 web_content 当前发布版本；草稿/已下线/不存在一律按不存在处理。
     *
     * @param id 内容 ID（前端传 String）
     */
    PublicWebContentDetailResp getPublicWebContentDetail(String id);
}

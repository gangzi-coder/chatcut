package com.chatcut.admin.website.service;

import com.chatcut.admin.website.model.request.WebContentCreateReq;
import com.chatcut.admin.website.model.request.WebContentDraftReq;
import com.chatcut.admin.website.model.request.WebContentListReq;
import com.chatcut.admin.website.model.response.WebContentDetailResp;
import com.chatcut.admin.website.model.response.WebContentResp;
import com.chatcut.admin.website.model.response.WebContentVersionResp;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;

import java.util.List;

/**
 * 官网内容服务：草稿与正式发布版本分离，发布切版本，下线保护引用。
 */
public interface WebContentService {

    /** 内容筛选分页。 */
    TableDataInfo<WebContentResp> pageWebContents(WebContentListReq request, PageQuery pageQuery);

    /** 详情：主表 + 当前草稿 + 当前发布版本。 */
    WebContentDetailResp getWebContentInfo(Long id);

    /** 新建内容并写入初始草稿，返回内容 ID。 */
    Long createWebContent(WebContentCreateReq request);

    /** 更新草稿（全量覆盖），不影响主表发布状态与展示标题。 */
    void saveWebContentDraft(Long id, WebContentDraftReq request);

    /** 校验并发布，事务内新增版本并切换当前发布版本，返回新版本号。 */
    Integer publishWebContent(Long id);

    /** 下线（校验其他已发布内容的站内引用），不删除历史版本。 */
    void offlineWebContent(Long id);

    /** 版本列表，按版本号倒序。 */
    List<WebContentVersionResp> listWebContentVersions(Long id);

    /** 恢复历史版本：读取版本快照写回草稿，仍需再次发布才生效。 */
    void restoreWebContentVersion(Long id, Integer versionNo);
}

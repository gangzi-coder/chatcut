package com.chatcut.admin.website.model.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 内容详情：主表 + 当前草稿 + 当前发布版本。
 */
@Data
public class WebContentDetailResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主表信息（状态、发布版本号等）。 */
    private WebContentResp content;

    /** 当前草稿（可能为 null：从未保存过草稿）。 */
    private WebContentDraftResp draft;

    /** 当前正式发布版本（可能为 null：从未发布）。 */
    private WebContentVersionResp currentVersion;
}

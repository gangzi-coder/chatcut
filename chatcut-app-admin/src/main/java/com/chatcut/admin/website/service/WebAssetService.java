package com.chatcut.admin.website.service;

import com.chatcut.admin.website.model.request.WebAssetListReq;
import com.chatcut.admin.website.model.request.WebAssetSaveReq;
import com.chatcut.admin.website.model.response.WebAssetResp;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;

/**
 * 官网展示素材服务。删除仅逻辑删除，不删除 OSS 原文件。
 */
public interface WebAssetService {

    /** 素材筛选分页。 */
    TableDataInfo<WebAssetResp> pageWebAssets(WebAssetListReq request, PageQuery pageQuery);

    /** 素材详情及被草稿/发布版本引用的次数。 */
    WebAssetDetail getWebAssetInfo(Long id);

    /** 登记素材（仅元数据），返回素材 ID。 */
    Long createWebAsset(WebAssetSaveReq request);

    /** 删除未被引用的素材（逻辑删除，保留 OSS 原文件）。 */
    void deleteWebAsset(Long id);

    /** 素材详情返回结构：素材 + 引用计数。 */
    final class WebAssetDetail {

        private final WebAssetResp webAsset;
        private final long draftReferences;
        private final long versionReferences;

        public WebAssetDetail(WebAssetResp webAsset, long draftReferences, long versionReferences) {
            this.webAsset = webAsset;
            this.draftReferences = draftReferences;
            this.versionReferences = versionReferences;
        }

        public WebAssetResp getWebAsset() {
            return webAsset;
        }

        public long getDraftReferences() {
            return draftReferences;
        }

        public long getVersionReferences() {
            return versionReferences;
        }
    }
}

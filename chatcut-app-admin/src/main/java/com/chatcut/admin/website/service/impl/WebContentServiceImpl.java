package com.chatcut.admin.website.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chatcut.admin.website.enums.AssetStatusEnum;
import com.chatcut.admin.website.enums.CtaTargetTypeEnum;
import com.chatcut.admin.website.enums.ContentTypeEnum;
import com.chatcut.admin.website.enums.ContentStatusEnum;
import com.chatcut.admin.website.enums.OpenStateEnum;
import com.chatcut.admin.website.enums.VersionStatusEnum;
import com.chatcut.admin.website.model.request.WebContentCreateReq;
import com.chatcut.admin.website.model.request.WebContentDraftReq;
import com.chatcut.admin.website.model.request.WebContentListReq;
import com.chatcut.admin.website.model.response.WebContentDetailResp;
import com.chatcut.admin.website.model.response.WebContentDraftResp;
import com.chatcut.admin.website.model.response.WebContentResp;
import com.chatcut.admin.website.model.response.WebContentVersionResp;
import com.chatcut.admin.website.repository.entity.WebContentDraftEntity;
import com.chatcut.admin.website.repository.entity.WebContentEntity;
import com.chatcut.admin.website.repository.entity.WebContentVersionEntity;
import com.chatcut.admin.website.repository.mapper.WebAssetMapper;
import com.chatcut.admin.website.repository.mapper.WebContentDraftMapper;
import com.chatcut.admin.website.repository.mapper.WebContentMapper;
import com.chatcut.admin.website.repository.mapper.WebContentVersionMapper;
import com.chatcut.admin.website.service.WebContentService;
import com.chatcut.admin.website.support.CtaTargets;
import com.chatcut.admin.website.support.WebsiteUtils;
import com.chatcut.admin.website.support.WebChangeLogRecorder;
import com.chatcut.admin.website.enums.AssetStatusEnum;
import com.chatcut.common.core.exception.ServiceException;
import com.chatcut.common.core.utils.MapstructUtils;
import com.chatcut.common.core.utils.StringUtils;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import com.chatcut.common.satoken.utils.LoginHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 官网内容服务实现。
 *
 * <p>草稿与正式发布版本分离：保存草稿不改主表发布状态与展示标题；
 * 发布在内容级分布式锁内校验并切版本；下线校验站内引用；恢复历史只写回草稿。</p>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WebContentServiceImpl implements WebContentService {

    private final WebContentMapper webContentMapper;
    private final WebContentDraftMapper webContentDraftMapper;
    private final WebContentVersionMapper webContentVersionMapper;
    private final WebAssetMapper webAssetMapper;
    private final WebChangeLogRecorder changeLogRecorder;

    @Override
    public TableDataInfo<WebContentResp> pageWebContents(WebContentListReq request, PageQuery pageQuery) {
        LambdaQueryWrapper<WebContentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(request.getContentType()), WebContentEntity::getContentType, request.getContentType())
            .eq(StringUtils.isNotBlank(request.getGroupKey()), WebContentEntity::getGroupKey, request.getGroupKey())
            .eq(StringUtils.isNotBlank(request.getStatus()), WebContentEntity::getStatus, request.getStatus())
            .like(StringUtils.isNotBlank(request.getKeyword()), WebContentEntity::getTitle, request.getKeyword())
            .orderByDesc(WebContentEntity::getUpdatedAt);
        Page<WebContentResp> page = webContentMapper.selectVoPage(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    @Override
    public WebContentDetailResp getWebContentInfo(Long id) {
        WebContentEntity webContent = requireContent(id);
        WebContentDetailResp resp = new WebContentDetailResp();
        resp.setContent(MapstructUtils.convert(webContent, WebContentResp.class));
        WebContentDraftEntity webContentDraft = webContentDraftMapper.selectOne(new LambdaQueryWrapper<WebContentDraftEntity>()
            .eq(WebContentDraftEntity::getContentId, id));
        resp.setDraft(webContentDraft == null ? null : MapstructUtils.convert(webContentDraft, WebContentDraftResp.class));
        if (webContent.getPublishedVersion() != null) {
            WebContentVersionEntity webContentVersion = webContentVersionMapper.selectOne(new LambdaQueryWrapper<WebContentVersionEntity>()
                .eq(WebContentVersionEntity::getContentId, id)
                .eq(WebContentVersionEntity::getVersionNo, webContent.getPublishedVersion()));
            resp.setCurrentVersion(webContentVersion == null ? null : MapstructUtils.convert(webContentVersion, WebContentVersionResp.class));
        }
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWebContent(WebContentCreateReq request) {
        ContentTypeEnum.of(request.getContentType());
        OpenStateEnum openState = request.getOpenState() == null ? OpenStateEnum.PREVIEW : OpenStateEnum.of(request.getOpenState());
        // 格式级校验：CTA 成对与目标格式在保存时就拦住，目标存在性留给发布时校验
        validateCtaPair(request.getCtaLabel(), request.getCtaTarget());
        CtaTargets.parse(request.getCtaTarget());
        Long mediaId = WebsiteUtils.parseIdNullable(request.getMediaId());
        checkAssetUsable(mediaId);

        Long adminId = LoginHelper.getUserId();
        LocalDateTime now = LocalDateTime.now();

        WebContentEntity webContent = new WebContentEntity();
        webContent.setContentType(request.getContentType());
        webContent.setGroupKey(request.getGroupKey());
        webContent.setTitle(request.getTitle());
        webContent.setStatus(ContentStatusEnum.DRAFT.getCode());
        webContent.setCreatedBy(adminId);
        webContent.setUpdatedBy(adminId);
        webContent.setCreatedAt(now);
        webContent.setUpdatedAt(now);
        webContentMapper.insert(webContent);

        WebContentDraftEntity webContentDraft = new WebContentDraftEntity();
        webContentDraft.setContentId(webContent.getId());
        fillDraft(webContentDraft, request.getTitle(), request.getSummary(), request.getBody(),
            request.getCtaLabel(), request.getCtaTarget(), mediaId, openState.getCode());
        webContentDraftMapper.insert(webContentDraft);
        return webContent.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWebContentDraft(Long id, WebContentDraftReq request) {
        WebContentEntity webContent = requireContent(id);
        OpenStateEnum.of(request.getOpenState());
        validateCtaPair(request.getCtaLabel(), request.getCtaTarget());
        CtaTargets.parse(request.getCtaTarget());
        Long mediaId = WebsiteUtils.parseIdNullable(request.getMediaId());
        checkAssetUsable(mediaId);

        WebContentDraftEntity existing = webContentDraftMapper.selectOne(new LambdaQueryWrapper<WebContentDraftEntity>()
            .eq(WebContentDraftEntity::getContentId, id));
        LocalDateTime now = LocalDateTime.now();
        Long adminId = LoginHelper.getUserId();

        if (existing == null) {
            WebContentDraftEntity webContentDraft = new WebContentDraftEntity();
            webContentDraft.setContentId(id);
            fillDraft(webContentDraft, request.getTitle(), request.getSummary(), request.getBody(),
                request.getCtaLabel(), request.getCtaTarget(), mediaId, request.getOpenState());
            webContentDraft.setUpdatedAt(now);
            webContentDraftMapper.insert(webContentDraft);
        } else {
            // 记录字段级变更（body 长文本由记录器截断/脱敏）
            Map<String, String[]> changes = diffDraft(existing, request);
            fillDraft(existing, request.getTitle(), request.getSummary(), request.getBody(),
                request.getCtaLabel(), request.getCtaTarget(), mediaId, request.getOpenState());
            existing.setUpdatedAt(now);
            webContentDraftMapper.updateById(existing);
            changeLogRecorder.record("CONTENT", id, changes);
        }

        // 主表仅维护最后更新人与时间，标题与状态不动（展示标题随发布切换）
        WebContentEntity update = new WebContentEntity();
        update.setId(id);
        update.setUpdatedBy(adminId);
        update.setUpdatedAt(now);
        webContentMapper.updateById(update);
    }

    @Override
    @Lock4j(keys = {"#id"}, expire = 30000, acquireTimeout = 3000)
    @Transactional(rollbackFor = Exception.class)
    public Integer publishWebContent(Long id) {
        WebContentEntity webContent = requireContent(id);
        WebContentDraftEntity webContentDraft = webContentDraftMapper.selectOne(new LambdaQueryWrapper<WebContentDraftEntity>()
            .eq(WebContentDraftEntity::getContentId, id));
        if (webContentDraft == null) {
            throw new ServiceException("请先保存内容草稿再发布");
        }
        validateForPublish(webContent, webContentDraft);

        Long adminId = LoginHelper.getUserId();
        LocalDateTime now = LocalDateTime.now();
        int versionNo = nextVersionNo(id);

        WebContentVersionEntity webContentVersion = new WebContentVersionEntity();
        webContentVersion.setContentId(id);
        webContentVersion.setVersionNo(versionNo);
        webContentVersion.setTitle(webContentDraft.getTitle());
        webContentVersion.setSummary(webContentDraft.getSummary());
        webContentVersion.setBody(webContentDraft.getBody());
        webContentVersion.setCtaLabel(webContentDraft.getCtaLabel());
        webContentVersion.setCtaTarget(webContentDraft.getCtaTarget());
        webContentVersion.setMediaId(webContentDraft.getMediaId());
        webContentVersion.setOpenState(webContentDraft.getOpenState());
        webContentVersion.setPublishStatus(VersionStatusEnum.PUBLISHED.getCode());
        webContentVersion.setPublishedAt(now);
        webContentVersion.setCreatedBy(adminId);
        webContentVersion.setCreatedAt(now);
        webContentVersionMapper.insert(webContentVersion);

        // 旧当前版本归档（历史保留）
        webContentVersionMapper.update(null, new LambdaUpdateWrapper<WebContentVersionEntity>()
            .eq(WebContentVersionEntity::getContentId, id)
            .eq(WebContentVersionEntity::getPublishStatus, VersionStatusEnum.PUBLISHED.getCode())
            .ne(WebContentVersionEntity::getVersionNo, versionNo)
            .set(WebContentVersionEntity::getPublishStatus, VersionStatusEnum.ARCHIVED.getCode()));

        // 切换主表：乐观锁保护并发发布
        webContent.setStatus(ContentStatusEnum.PUBLISHED.getCode());
        webContent.setPublishedVersion(versionNo);
        webContent.setTitle(webContentDraft.getTitle());
        webContent.setUpdatedBy(adminId);
        webContent.setUpdatedAt(now);
        int rows = webContentMapper.updateById(webContent);
        if (rows == 0) {
            throw new ServiceException("内容已被他人修改（版本冲突），请刷新后重试");
        }

        return versionNo;
    }

    @Override
    @Lock4j(keys = {"#id"}, expire = 30000, acquireTimeout = 3000)
    @Transactional(rollbackFor = Exception.class)
    public void offlineWebContent(Long id) {
        WebContentEntity webContent = requireContent(id);
        if (!ContentStatusEnum.PUBLISHED.getCode().equals(webContent.getStatus())) {
            throw new ServiceException("仅已发布内容可下线");
        }
        // 下线保护：其他已发布内容通过 webContent:<id> 指向本内容时禁止下线
        List<WebContentVersionEntity> publishedVersions = webContentVersionMapper.selectList(
            new LambdaQueryWrapper<WebContentVersionEntity>()
                .eq(WebContentVersionEntity::getPublishStatus, VersionStatusEnum.PUBLISHED.getCode()));
        boolean referenced = publishedVersions.stream()
            .filter(v -> !Objects.equals(v.getContentId(), id))
            .map(WebContentVersionEntity::getCtaTarget)
            .anyMatch(t -> CtaTargets.refersToContent(t, id));
        if (referenced) {
            throw new ServiceException("其他已发布内容的按钮正指向本内容，请先修改相关入口再下线");
        }

        Long adminId = LoginHelper.getUserId();
        webContent.setStatus(ContentStatusEnum.OFFLINE.getCode());
        webContent.setUpdatedBy(adminId);
        webContent.setUpdatedAt(LocalDateTime.now());
        int rows = webContentMapper.updateById(webContent);
        if (rows == 0) {
            throw new ServiceException("内容已被他人修改（版本冲突），请刷新后重试");
        }

        // 下线同时把当前发布版本归档：否则版本表仍存在 PUBLISHED 记录，
        // 其他内容的引用保护永远解除不了，官网侧也无法判断"当前发布版"。
        webContentVersionMapper.update(null, new LambdaUpdateWrapper<WebContentVersionEntity>()
            .eq(WebContentVersionEntity::getContentId, id)
            .eq(WebContentVersionEntity::getPublishStatus, VersionStatusEnum.PUBLISHED.getCode())
            .set(WebContentVersionEntity::getPublishStatus, VersionStatusEnum.ARCHIVED.getCode()));
    }

    @Override
    public List<WebContentVersionResp> listWebContentVersions(Long id) {
        requireContent(id);
        return webContentVersionMapper.selectVoList(new LambdaQueryWrapper<WebContentVersionEntity>()
            .eq(WebContentVersionEntity::getContentId, id)
            .orderByDesc(WebContentVersionEntity::getVersionNo));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreWebContentVersion(Long id, Integer versionNo) {
        WebContentEntity webContent = requireContent(id);
        WebContentVersionEntity webContentVersion = webContentVersionMapper.selectOne(new LambdaQueryWrapper<WebContentVersionEntity>()
            .eq(WebContentVersionEntity::getContentId, id)
            .eq(WebContentVersionEntity::getVersionNo, versionNo));
        if (webContentVersion == null) {
            throw new ServiceException("历史版本不存在");
        }

        WebContentDraftEntity existing = webContentDraftMapper.selectOne(new LambdaQueryWrapper<WebContentDraftEntity>()
            .eq(WebContentDraftEntity::getContentId, id));
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            WebContentDraftEntity webContentDraft = new WebContentDraftEntity();
            webContentDraft.setContentId(id);
            copyVersionToDraft(webContentVersion, webContentDraft);
            webContentDraft.setUpdatedAt(now);
            webContentDraftMapper.insert(webContentDraft);
        } else {
            Map<String, String[]> changes = diffVersionToDraft(existing, webContentVersion);
            copyVersionToDraft(webContentVersion, existing);
            existing.setUpdatedAt(now);
            webContentDraftMapper.updateById(existing);
            changeLogRecorder.record("CONTENT", id, changes);
        }

        WebContentEntity update = new WebContentEntity();
        update.setId(id);
        update.setUpdatedBy(LoginHelper.getUserId());
        update.setUpdatedAt(now);
        webContentMapper.updateById(update);
    }

    // ------------------------------------------------------------------ private

    private WebContentEntity requireContent(Long id) {
        WebContentEntity webContent = webContentMapper.selectById(id);
        if (webContent == null) {
            throw new ServiceException("内容不存在");
        }
        return webContent;
    }

    private void fillDraft(WebContentDraftEntity webContentDraft, String title, String summary, String body,
                           String ctaLabel, String ctaTarget, Long mediaId, String openState) {
        webContentDraft.setTitle(title);
        webContentDraft.setSummary(summary);
        webContentDraft.setBody(body);
        webContentDraft.setCtaLabel(ctaLabel);
        webContentDraft.setCtaTarget(ctaTarget);
        webContentDraft.setMediaId(mediaId);
        webContentDraft.setOpenState(openState);
    }

    /** 文案与目标必须成对：都空（无按钮）或都非空。 */
    private void validateCtaPair(String ctaLabel, String ctaTarget) {
        if (StringUtils.isNotBlank(ctaLabel) != StringUtils.isNotBlank(ctaTarget)) {
            throw new ServiceException("按钮文案与按钮目标必须成对填写");
        }
    }

    /** 素材引用校验：素材必须存在且未删除。 */
    private void checkAssetUsable(Long mediaId) {
        if (mediaId == null) {
            return;
        }
        com.chatcut.admin.website.repository.entity.WebAssetEntity webAsset = webAssetMapper.selectById(mediaId);
        if (webAsset == null || AssetStatusEnum.DELETED.getCode().equals(webAsset.getStatus())) {
            throw new ServiceException("关联素材不存在或已删除");
        }
    }

    /**
     * 发布前校验：摘要必填、开放状态合法、CTA 成对且合法、产品可用约束、站内目标可达。
     */
    private void validateForPublish(WebContentEntity webContent, WebContentDraftEntity webContentDraft) {
        if (StringUtils.isBlank(webContentDraft.getSummary())) {
            throw new ServiceException("发布前必须填写摘要");
        }
        OpenStateEnum openState = OpenStateEnum.of(webContentDraft.getOpenState());
        validateCtaPair(webContentDraft.getCtaLabel(), webContentDraft.getCtaTarget());
        checkAssetUsable(webContentDraft.getMediaId());

        if (StringUtils.isBlank(webContentDraft.getCtaTarget())) {
            return;
        }
        CtaTargets.TargetRef targetRef = CtaTargets.parse(webContentDraft.getCtaTarget());
        ContentTypeEnum contentType = ContentTypeEnum.of(webContent.getContentType());
        boolean productAvailable = contentType == ContentTypeEnum.PRODUCT && openState == OpenStateEnum.AVAILABLE;

        if (productAvailable && targetRef.getType() == CtaTargetTypeEnum.LEAD) {
            throw new ServiceException("产品「可用」状态必须配置正式入口（外链或站内内容），不能用 lead: 咨询入口代替");
        }
        if (targetRef.getType() == CtaTargetTypeEnum.CONTENT) {
            Long targetId = Long.valueOf(targetRef.getValue());
            if (targetId.equals(webContent.getId())) {
                throw new ServiceException("站内目标不能指向自身");
            }
            WebContentEntity target = webContentMapper.selectById(targetId);
            if (target == null || !ContentStatusEnum.PUBLISHED.getCode().equals(target.getStatus())) {
                throw new ServiceException("站内目标内容不存在或未发布");
            }
            if (productAvailable) {
                WebContentVersionEntity targetVersion = webContentVersionMapper.selectOne(
                    new LambdaQueryWrapper<WebContentVersionEntity>()
                        .eq(WebContentVersionEntity::getContentId, targetId)
                        .eq(WebContentVersionEntity::getVersionNo, target.getPublishedVersion()));
                if (targetVersion == null || !OpenStateEnum.AVAILABLE.getCode().equals(targetVersion.getOpenState())) {
                    throw new ServiceException("产品「可用」状态的站内目标内容也须为「可用」状态");
                }
            }
        }
    }

    private int nextVersionNo(Long contentId) {
        WebContentVersionEntity max = webContentVersionMapper.selectOne(new LambdaQueryWrapper<WebContentVersionEntity>()
            .eq(WebContentVersionEntity::getContentId, contentId)
            .orderByDesc(WebContentVersionEntity::getVersionNo)
            .last("limit 1"));
        return max == null ? 1 : max.getVersionNo() + 1;
    }

    private void copyVersionToDraft(WebContentVersionEntity webContentVersion, WebContentDraftEntity webContentDraft) {
        webContentDraft.setTitle(webContentVersion.getTitle());
        webContentDraft.setSummary(webContentVersion.getSummary());
        webContentDraft.setBody(webContentVersion.getBody());
        webContentDraft.setCtaLabel(webContentVersion.getCtaLabel());
        webContentDraft.setCtaTarget(webContentVersion.getCtaTarget());
        webContentDraft.setMediaId(webContentVersion.getMediaId());
        webContentDraft.setOpenState(webContentVersion.getOpenState());
    }

    /** 草稿保存前后 diff，供 web_change_log。 */
    private Map<String, String[]> diffDraft(WebContentDraftEntity oldDraft, WebContentDraftReq request) {
        Map<String, String[]> changes = new LinkedHashMap<>();
        putIfChanged(changes, "title", oldDraft.getTitle(), request.getTitle());
        putIfChanged(changes, "summary", oldDraft.getSummary(), request.getSummary());
        putIfChanged(changes, "body", oldDraft.getBody(), request.getBody());
        putIfChanged(changes, "cta_label", oldDraft.getCtaLabel(), request.getCtaLabel());
        putIfChanged(changes, "cta_target", oldDraft.getCtaTarget(), request.getCtaTarget());
        putIfChanged(changes, "media_id", Objects.toString(oldDraft.getMediaId(), null), Objects.toString(request.getMediaId(), null));
        putIfChanged(changes, "open_state", oldDraft.getOpenState(), request.getOpenState());
        return changes;
    }

    /** 版本恢复覆盖草稿前 diff，供 web_change_log。 */
    private Map<String, String[]> diffVersionToDraft(WebContentDraftEntity oldDraft, WebContentVersionEntity webContentVersion) {
        Map<String, String[]> changes = new LinkedHashMap<>();
        putIfChanged(changes, "title", oldDraft.getTitle(), webContentVersion.getTitle());
        putIfChanged(changes, "summary", oldDraft.getSummary(), webContentVersion.getSummary());
        putIfChanged(changes, "body", oldDraft.getBody(), webContentVersion.getBody());
        putIfChanged(changes, "cta_label", oldDraft.getCtaLabel(), webContentVersion.getCtaLabel());
        putIfChanged(changes, "cta_target", oldDraft.getCtaTarget(), webContentVersion.getCtaTarget());
        putIfChanged(changes, "media_id", Objects.toString(oldDraft.getMediaId(), null), Objects.toString(webContentVersion.getMediaId(), null));
        putIfChanged(changes, "open_state", oldDraft.getOpenState(), webContentVersion.getOpenState());
        return changes;
    }

    private void putIfChanged(Map<String, String[]> changes, String field, String oldValue, String newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            changes.put(field, new String[]{oldValue, newValue});
        }
    }
}

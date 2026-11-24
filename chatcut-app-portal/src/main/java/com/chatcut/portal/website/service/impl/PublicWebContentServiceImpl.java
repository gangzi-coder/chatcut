package com.chatcut.portal.website.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chatcut.common.core.exception.ServiceException;
import com.chatcut.common.core.utils.StringUtils;
import com.chatcut.portal.website.enums.ContentStatusEnum;
import com.chatcut.portal.website.repository.entity.WebAssetEntity;
import com.chatcut.portal.website.repository.entity.WebContentEntity;
import com.chatcut.portal.website.repository.entity.WebContentVersionEntity;
import com.chatcut.portal.website.repository.mapper.WebAssetMapper;
import com.chatcut.portal.website.repository.mapper.WebContentMapper;
import com.chatcut.portal.website.repository.mapper.WebContentVersionMapper;
import com.chatcut.portal.website.model.response.PublicWebContentDetailResp;
import com.chatcut.portal.website.model.response.PublicWebContentItemResp;
import com.chatcut.portal.website.service.PublicWebContentService;
import com.chatcut.portal.website.support.PortalWebsiteUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 官网公开内容服务实现。
 *
 * <p>只读已发布内容：列表按创建时间倒序（web_content 无 sortOrder 列，一期用 createdAt
 * 近似运营期望的“最新在前”）；详情读取主表当前发布版本。管理端录入的标题/正文为可信内容，
 * 此处不做 HTML 转义；访客输入的转义在提交侧完成。</p>
 */
@RequiredArgsConstructor
@Service
public class PublicWebContentServiceImpl implements PublicWebContentService {

    private final WebContentMapper webContentMapper;
    private final WebContentVersionMapper webContentVersionMapper;
    private final WebAssetMapper webAssetMapper;

    @Override
    public List<PublicWebContentItemResp> listPublicWebContents(String contentType, String groupKey) {
        LambdaQueryWrapper<WebContentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WebContentEntity::getStatus, ContentStatusEnum.PUBLISHED.getCode())
            .eq(WebContentEntity::getDelFlag, "0")
            .eq(StringUtils.isNotBlank(contentType), WebContentEntity::getContentType, contentType)
            .eq(StringUtils.isNotBlank(groupKey), WebContentEntity::getGroupKey, groupKey)
            // 显式排序：新内容在前，同秒创建按 ID 倒序保证稳定
            .orderByDesc(WebContentEntity::getCreatedAt)
            .orderByDesc(WebContentEntity::getId);
        List<WebContentEntity> webContents = webContentMapper.selectList(wrapper);
        return webContents.stream().map(this::toItemResp).toList();
    }

    @Override
    public PublicWebContentDetailResp getPublicWebContentDetail(String id) {
        Long contentId = PortalWebsiteUtils.parseId(id);
        WebContentEntity webContent = webContentMapper.selectById(contentId);
        if (webContent == null
            || !ContentStatusEnum.PUBLISHED.getCode().equals(webContent.getStatus())
            || !"0".equals(webContent.getDelFlag())
            || webContent.getPublishedVersion() == null) {
            throw new ServiceException("内容不存在或未公开");
        }
        WebContentVersionEntity webContentVersion = webContentVersionMapper.selectOne(
            new LambdaQueryWrapper<WebContentVersionEntity>()
                .eq(WebContentVersionEntity::getContentId, contentId)
                .eq(WebContentVersionEntity::getVersionNo, webContent.getPublishedVersion()));
        if (webContentVersion == null) {
            throw new ServiceException("内容不存在或未公开");
        }
        return toDetailResp(webContent, webContentVersion);
    }

    // ------------------------------------------------------------------ private

    /** 列表项映射：附带当前发布版本的摘要/开放状态/CTA（发布版本缺失则这些字段留空）。 */
    private PublicWebContentItemResp toItemResp(WebContentEntity webContent) {
        PublicWebContentItemResp item = new PublicWebContentItemResp();
        item.setId(String.valueOf(webContent.getId()));
        item.setContentType(webContent.getContentType());
        item.setGroupKey(webContent.getGroupKey());
        item.setTitle(webContent.getTitle());
        item.setPublishedAt(webContent.getCreatedAt());
        if (webContent.getPublishedVersion() != null) {
            WebContentVersionEntity webContentVersion = requireCurrentVersion(webContent);
            if (webContentVersion != null) {
                fillPublicVersionFields(item, webContentVersion);
                item.setPublishedAt(webContentVersion.getPublishedAt());
            }
        }
        return item;
    }

    /** 详情映射：正文 + 媒体信息（素材须 ACTIVE 且未删除）。 */
    private PublicWebContentDetailResp toDetailResp(WebContentEntity webContent, WebContentVersionEntity webContentVersion) {
        PublicWebContentDetailResp detail = new PublicWebContentDetailResp();
        detail.setId(String.valueOf(webContent.getId()));
        detail.setContentType(webContent.getContentType());
        detail.setGroupKey(webContent.getGroupKey());
        detail.setTitle(webContent.getTitle());
        detail.setBody(webContentVersion.getBody());
        detail.setPublishedAt(webContentVersion.getPublishedAt());
        fillPublicVersionFields(detail, webContentVersion);

        if (webContentVersion.getMediaId() != null) {
            WebAssetEntity webAsset = webAssetMapper.selectById(webContentVersion.getMediaId());
            if (webAsset != null && "ACTIVE".equals(webAsset.getStatus()) && "0".equals(webAsset.getDelFlag())) {
                detail.setMediaName(webAsset.getName());
                detail.setMediaUrl(webAsset.getPublicUrl());
                detail.setMediaMimeType(webAsset.getMimeType());
                detail.setMediaAssetType(webAsset.getAssetType());
            }
        }
        return detail;
    }

    /** 版本公开字段（摘要/开放状态/CTA）填充，CTA 目标类型按前缀推导。 */
    private void fillPublicVersionFields(PublicWebContentItemResp target, WebContentVersionEntity webContentVersion) {
        target.setSummary(webContentVersion.getSummary());
        target.setOpenState(webContentVersion.getOpenState());
        target.setCtaLabel(webContentVersion.getCtaLabel());
        target.setCtaTarget(webContentVersion.getCtaTarget());
        target.setCtaTargetType(resolveCtaTargetType(webContentVersion.getCtaTarget()));
    }

    /** 版本公开字段填充（详情响应重载）。 */
    private void fillPublicVersionFields(PublicWebContentDetailResp target, WebContentVersionEntity webContentVersion) {
        target.setSummary(webContentVersion.getSummary());
        target.setOpenState(webContentVersion.getOpenState());
        target.setCtaLabel(webContentVersion.getCtaLabel());
        target.setCtaTarget(webContentVersion.getCtaTarget());
        target.setCtaTargetType(resolveCtaTargetType(webContentVersion.getCtaTarget()));
    }

    /** 读取内容的当前发布版本；读取失败按空处理（列表场景容忍，详情场景已前置校验）。 */
    private WebContentVersionEntity requireCurrentVersion(WebContentEntity webContent) {
        return webContentVersionMapper.selectOne(new LambdaQueryWrapper<WebContentVersionEntity>()
            .eq(WebContentVersionEntity::getContentId, webContent.getId())
            .eq(WebContentVersionEntity::getVersionNo, webContent.getPublishedVersion()));
    }

    /** CTA 目标类型推导：lead:→LEAD，content:→CONTENT，https://→EXTERNAL，其余/空为 null。 */
    private String resolveCtaTargetType(String ctaTarget) {
        if (StringUtils.isBlank(ctaTarget)) {
            return null;
        }
        String trimmed = ctaTarget.trim();
        if (trimmed.startsWith("lead:")) {
            return "LEAD";
        }
        if (trimmed.startsWith("content:")) {
            return "CONTENT";
        }
        if (trimmed.startsWith("https://")) {
            return "EXTERNAL";
        }
        return null;
    }
}

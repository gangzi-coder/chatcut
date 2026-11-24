package com.chatcut.admin.website.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chatcut.admin.website.enums.AssetStatusEnum;
import com.chatcut.admin.website.enums.AssetTypeEnum;
import com.chatcut.admin.website.model.request.WebAssetListReq;
import com.chatcut.admin.website.model.request.WebAssetSaveReq;
import com.chatcut.admin.website.model.response.WebAssetResp;
import com.chatcut.admin.website.repository.entity.WebAssetEntity;
import com.chatcut.admin.website.repository.entity.WebContentDraftEntity;
import com.chatcut.admin.website.repository.entity.WebContentVersionEntity;
import com.chatcut.admin.website.repository.mapper.WebAssetMapper;
import com.chatcut.admin.website.repository.mapper.WebContentDraftMapper;
import com.chatcut.admin.website.repository.mapper.WebContentVersionMapper;
import com.chatcut.admin.website.service.WebAssetService;
import com.chatcut.common.core.exception.ServiceException;
import com.chatcut.common.core.utils.MapstructUtils;
import com.chatcut.common.core.utils.StringUtils;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import com.chatcut.common.satoken.utils.LoginHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 官网展示素材服务实现。一期仅登记元数据，单文件大小由前端控制。
 */
@RequiredArgsConstructor
@Service
public class WebAssetServiceImpl implements WebAssetService {

    private final WebAssetMapper webAssetMapper;
    private final WebContentDraftMapper webContentDraftMapper;
    private final WebContentVersionMapper webContentVersionMapper;

    @Override
    public TableDataInfo<WebAssetResp> pageWebAssets(WebAssetListReq request, PageQuery pageQuery) {
        LambdaQueryWrapper<WebAssetEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(request.getAssetType()), WebAssetEntity::getAssetType, request.getAssetType())
                .eq(StringUtils.isNotBlank(request.getStatus()), WebAssetEntity::getStatus, request.getStatus())
                .like(StringUtils.isNotBlank(request.getKeyword()), WebAssetEntity::getName, request.getKeyword())
                .orderByDesc(WebAssetEntity::getCreatedAt);
        Page<WebAssetResp> page = webAssetMapper.selectVoPage(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    @Override
    public WebAssetDetail getWebAssetInfo(Long id) {
        WebAssetEntity webAsset = requireAsset(id);
        long draftRefs = webContentDraftMapper.selectCount(new LambdaQueryWrapper<WebContentDraftEntity>()
                .eq(WebContentDraftEntity::getMediaId, id));
        long versionRefs = webContentVersionMapper.selectCount(new LambdaQueryWrapper<WebContentVersionEntity>()
                .eq(WebContentVersionEntity::getMediaId, id));
        return new WebAssetDetail(MapstructUtils.convert(webAsset, WebAssetResp.class), draftRefs, versionRefs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWebAsset(WebAssetSaveReq request) {
        AssetTypeEnum.of(request.getAssetType());
        Long adminId = LoginHelper.getUserId();
        LocalDateTime now = LocalDateTime.now();

        WebAssetEntity webAsset = new WebAssetEntity();
        webAsset.setName(request.getName());
        webAsset.setAssetType(request.getAssetType());
        webAsset.setColor(request.getColor());
        webAsset.setObjectKey(request.getObjectKey());
        webAsset.setPublicUrl(request.getPublicUrl());
        webAsset.setMimeType(request.getMimeType());
        webAsset.setDescription(request.getDescription());
        webAsset.setStatus(AssetStatusEnum.ACTIVE.getCode());
        webAsset.setCreatedBy(adminId);
        webAsset.setCreatedAt(now);
        webAsset.setUpdatedAt(now);
        webAssetMapper.insert(webAsset);
        return webAsset.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWebAsset(Long id) {
        WebAssetEntity webAsset = requireAsset(id);
        long draftRefs = webContentDraftMapper.selectCount(new LambdaQueryWrapper<WebContentDraftEntity>()
                .eq(WebContentDraftEntity::getMediaId, id));
        long versionRefs = webContentVersionMapper.selectCount(new LambdaQueryWrapper<WebContentVersionEntity>()
                .eq(WebContentVersionEntity::getMediaId, id));
        if (draftRefs + versionRefs > 0) {
            throw new ServiceException("素材正被内容草稿或发布版本引用，不能删除");
        }
        // 仅逻辑删除（del_flag），不删除 OSS 原文件
        webAssetMapper.deleteById(id);
    }

    private WebAssetEntity requireAsset(Long id) {
        WebAssetEntity webAsset = webAssetMapper.selectById(id);
        if (webAsset == null) {
            throw new ServiceException("素材不存在");
        }
        return webAsset;
    }
}

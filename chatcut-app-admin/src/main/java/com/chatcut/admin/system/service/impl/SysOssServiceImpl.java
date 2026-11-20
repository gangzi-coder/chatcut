package com.chatcut.admin.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import com.chatcut.common.core.constant.CacheNames;
import com.chatcut.common.core.domain.dto.OssDTO;
import com.chatcut.common.core.exception.ServiceException;
import com.chatcut.common.core.service.OssService;
import com.chatcut.common.core.utils.MapstructUtils;
import com.chatcut.common.core.utils.SpringUtils;
import com.chatcut.common.core.utils.StreamUtils;
import com.chatcut.common.core.utils.StringUtils;
import com.chatcut.common.core.utils.file.FileUtils;
import com.chatcut.common.json.utils.JsonUtils;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import com.chatcut.common.oss.core.OssClient;
import com.chatcut.common.oss.entity.UploadResult;
import com.chatcut.common.oss.enums.AccessPolicyType;
import com.chatcut.common.oss.factory.OssFactory;
import com.chatcut.admin.system.repository.entity.SysOssEntity;
import com.chatcut.admin.system.repository.entity.SysOssExtEntity;
import com.chatcut.admin.system.model.req.SysOssReq;
import com.chatcut.admin.system.model.resp.SysOssResp;
import com.chatcut.admin.system.repository.mapper.SysOssMapper;
import com.chatcut.admin.system.service.ISysOssService;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 文件上传 服务层实现
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysOssServiceImpl implements ISysOssService, OssService {

    private final SysOssMapper baseMapper;

    /**
     * 查询OSS对象存储列表
     *
     * @param bo        OSS对象存储分页查询对象
     * @param pageQuery 分页查询实体类
     * @return 结果
     */
    @Override
    public TableDataInfo<SysOssResp> queryPageList(SysOssReq bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysOssEntity> lqw = buildQueryWrapper(bo);
        Page<SysOssResp> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        List<SysOssResp> filterResult = StreamUtils.toList(result.getRecords(), this::matchingUrl);
        result.setRecords(filterResult);
        return TableDataInfo.build(result);
    }

    /**
     * 根据一组 ossIds 获取对应的 SysOssResp 列表
     *
     * @param ossIds 一组文件在数据库中的唯一标识集合
     * @return 包含 SysOssResp 对象的列表
     */
    @Override
    public List<SysOssResp> listByIds(Collection<Long> ossIds) {
        List<SysOssResp> list = new ArrayList<>();
        SysOssServiceImpl ossService = SpringUtils.getAopProxy(this);
        for (Long id : ossIds) {
            SysOssResp vo = ossService.getById(id);
            if (ObjectUtil.isNotNull(vo)) {
                try {
                    list.add(this.matchingUrl(vo));
                } catch (Exception ignored) {
                    // 如果oss异常无法连接则将数据直接返回
                    list.add(vo);
                }
            }
        }
        return list;
    }

    /**
     * 根据一组 ossIds 获取对应文件的 URL 列表
     *
     * @param ossIds 以逗号分隔的 ossId 字符串
     * @return 以逗号分隔的文件 URL 字符串
     */
    @Override
    public String selectUrlByIds(String ossIds) {
        List<String> list = new ArrayList<>();
        SysOssServiceImpl ossService = SpringUtils.getAopProxy(this);
        for (Long id : StringUtils.splitTo(ossIds, Convert::toLong)) {
            SysOssResp vo = ossService.getById(id);
            if (ObjectUtil.isNotNull(vo)) {
                try {
                    list.add(this.matchingUrl(vo).getUrl());
                } catch (Exception ignored) {
                    // 如果oss异常无法连接则将数据直接返回
                    list.add(vo.getUrl());
                }
            }
        }
        return StringUtils.joinComma(list);
    }

    @Override
    public List<OssDTO> selectByIds(String ossIds) {
        List<OssDTO> list = new ArrayList<>();
        for (Long id : StringUtils.splitTo(ossIds, Convert::toLong)) {
            SysOssResp vo = SpringUtils.getAopProxy(this).getById(id);
            if (ObjectUtil.isNotNull(vo)) {
                try {
                    vo.setUrl(this.matchingUrl(vo).getUrl());
                    list.add(BeanUtil.toBean(vo, OssDTO.class));
                } catch (Exception ignored) {
                    // 如果oss异常无法连接则将数据直接返回
                    list.add(BeanUtil.toBean(vo, OssDTO.class));
                }
            }
        }
        return list;
    }

    private LambdaQueryWrapper<SysOssEntity> buildQueryWrapper(SysOssReq bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SysOssEntity> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getFileName()), SysOssEntity::getFileName, bo.getFileName());
        lqw.like(StringUtils.isNotBlank(bo.getOriginalName()), SysOssEntity::getOriginalName, bo.getOriginalName());
        lqw.eq(StringUtils.isNotBlank(bo.getFileSuffix()), SysOssEntity::getFileSuffix, bo.getFileSuffix());
        lqw.eq(StringUtils.isNotBlank(bo.getUrl()), SysOssEntity::getUrl, bo.getUrl());
        lqw.between(params.get("beginCreateTime") != null && params.get("endCreateTime") != null,
            SysOssEntity::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));
        lqw.eq(ObjectUtil.isNotNull(bo.getCreateBy()), SysOssEntity::getCreateBy, bo.getCreateBy());
        lqw.eq(StringUtils.isNotBlank(bo.getService()), SysOssEntity::getService, bo.getService());
        lqw.orderByAsc(SysOssEntity::getOssId);
        return lqw;
    }

    /**
     * 根据 ossId 从缓存或数据库中获取 SysOssResp 对象
     *
     * @param ossId 文件在数据库中的唯一标识
     * @return SysOssResp 对象，包含文件信息
     */
    @Cacheable(cacheNames = CacheNames.SYS_OSS, key = "#ossId")
    @Override
    public SysOssResp getById(Long ossId) {
        return baseMapper.selectVoById(ossId);
    }


    /**
     * 文件下载方法，支持一次性下载完整文件
     *
     * @param ossId    OSS对象ID
     * @param response HttpServletResponse对象，用于设置响应头和向客户端发送文件内容
     */
    @Override
    public void download(Long ossId, HttpServletResponse response) throws IOException {
        SysOssResp sysOss = SpringUtils.getAopProxy(this).getById(ossId);
        if (ObjectUtil.isNull(sysOss)) {
            throw new ServiceException("文件数据不存在!");
        }
        FileUtils.setAttachmentResponseHeader(response, sysOss.getOriginalName());
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE + "; charset=UTF-8");
        OssClient storage = OssFactory.instance(sysOss.getService());
        storage.download(sysOss.getFileName(), response.getOutputStream(), response::setContentLengthLong);
    }

    /**
     * 上传 MultipartFile 到对象存储服务，并保存文件信息到数据库
     *
     * @param file 要上传的 MultipartFile 对象
     * @return 上传成功后的 SysOssResp 对象，包含文件信息
     * @throws ServiceException 如果上传过程中发生异常，则抛出 ServiceException 异常
     */
    @Override
    public SysOssResp upload(MultipartFile file) {
        if (ObjectUtil.isNull(file) || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }
        String originalfileName = file.getOriginalFilename();
        String suffix = StringUtils.substring(originalfileName, originalfileName.lastIndexOf("."), originalfileName.length());
        OssClient storage = OssFactory.instance();
        UploadResult uploadResult;
        try {
            uploadResult = storage.uploadSuffix(file.getBytes(), suffix, file.getContentType());
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }
        SysOssExtEntity ext1 = new SysOssExtEntity();
        ext1.setFileSize(file.getSize());
        ext1.setContentType(file.getContentType());
        // 保存文件信息
        return buildResultEntity(originalfileName, suffix, storage.getConfigKey(), uploadResult, ext1);
    }

    /**
     * 上传文件到对象存储服务，并保存文件信息到数据库
     *
     * @param file 要上传的文件对象
     * @return 上传成功后的 SysOssResp 对象，包含文件信息
     */
    @Override
    public SysOssResp upload(File file) {
        if (ObjectUtil.isNull(file) || !file.isFile() || file.length() <= 0) {
            throw new ServiceException("上传文件不能为空");
        }
        String originalfileName = file.getName();
        String suffix = StringUtils.substring(originalfileName, originalfileName.lastIndexOf("."), originalfileName.length());
        OssClient storage = OssFactory.instance();
        long length = file.length();
        UploadResult uploadResult = storage.uploadSuffix(file, suffix);
        SysOssExtEntity ext1 = new SysOssExtEntity();
        ext1.setFileSize(length);
        // 保存文件信息
        return buildResultEntity(originalfileName, suffix, storage.getConfigKey(), uploadResult, ext1);
    }

    @NotNull
    private SysOssResp buildResultEntity(String originalfileName, String suffix, String configKey, UploadResult uploadResult, SysOssExtEntity ext1) {
        SysOssEntity oss = new SysOssEntity();
        oss.setUrl(uploadResult.getUrl());
        oss.setFileSuffix(suffix);
        oss.setFileName(uploadResult.getFilename());
        oss.setOriginalName(originalfileName);
        oss.setService(configKey);
        oss.setExt1(JsonUtils.toJsonString(ext1));
        baseMapper.insert(oss);
        SysOssResp sysOssVo = MapstructUtils.convert(oss, SysOssResp.class);
        return this.matchingUrl(sysOssVo);
    }

    /**
     * 删除OSS对象存储
     *
     * @param ids     OSS对象ID串
     * @param isValid 判断是否需要校验
     * @return 结果
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            // 做一些业务上的校验,判断是否需要校验
        }
        List<SysOssEntity> list = baseMapper.selectByIds(ids);
        for (SysOssEntity sysOss : list) {
            OssClient storage = OssFactory.instance(sysOss.getService());
            storage.delete(sysOss.getUrl());
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 桶类型为 private 的URL 修改为临时URL时长为120s
     *
     * @param oss OSS对象
     * @return oss 匹配Url的OSS对象
     */
    private SysOssResp matchingUrl(SysOssResp oss) {
        OssClient storage = OssFactory.instance(oss.getService());
        // 仅修改桶类型为 private 的URL，临时URL时长为120s
        if (AccessPolicyType.PRIVATE == storage.getAccessPolicy()) {
            oss.setUrl(storage.createPresignedGetUrl(oss.getFileName(), Duration.ofSeconds(120)));
        }
        return oss;
    }
}

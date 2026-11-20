package com.chatcut.admin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.chatcut.common.core.constant.CacheNames;
import com.chatcut.common.core.exception.ServiceException;
import com.chatcut.common.core.utils.MapstructUtils;
import com.chatcut.common.core.utils.ObjectUtils;
import com.chatcut.common.core.utils.StringUtils;
import com.chatcut.common.json.utils.JsonUtils;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import com.chatcut.common.oss.constant.OssConstant;
import com.chatcut.common.redis.utils.CacheUtils;
import com.chatcut.common.redis.utils.RedisUtils;
import com.chatcut.admin.system.repository.entity.SysOssConfigEntity;
import com.chatcut.admin.system.model.req.SysOssConfigReq;
import com.chatcut.admin.system.model.resp.SysOssConfigResp;
import com.chatcut.admin.system.repository.mapper.SysOssConfigMapper;
import com.chatcut.admin.system.service.ISysOssConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * 对象存储配置Service业务层处理
 *
 * @author Lion Li
 * @author 孤舟烟雨
 * @date 2021-08-13
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysOssConfigServiceImpl implements ISysOssConfigService {

    private final SysOssConfigMapper baseMapper;

    /**
     * 项目启动时，初始化参数到缓存，加载配置类
     */
    @Override
    public void init() {
        List<SysOssConfigEntity> list = baseMapper.selectList();
        // 加载OSS初始化配置
        for (SysOssConfigEntity config : list) {
            String configKey = config.getConfigKey();
            if ("0".equals(config.getStatus())) {
                RedisUtils.setCacheObject(OssConstant.DEFAULT_CONFIG_KEY, configKey);
            }
            CacheUtils.put(CacheNames.SYS_OSS_CONFIG, config.getConfigKey(), JsonUtils.toJsonString(config));
        }
    }

    @Override
    public SysOssConfigResp queryById(Long ossConfigId) {
        return baseMapper.selectVoById(ossConfigId);
    }

    @Override
    public TableDataInfo<SysOssConfigResp> queryPageList(SysOssConfigReq bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysOssConfigEntity> lqw = buildQueryWrapper(bo);
        Page<SysOssConfigResp> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }


    private LambdaQueryWrapper<SysOssConfigEntity> buildQueryWrapper(SysOssConfigReq bo) {
        LambdaQueryWrapper<SysOssConfigEntity> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getConfigKey()), SysOssConfigEntity::getConfigKey, bo.getConfigKey());
        lqw.like(StringUtils.isNotBlank(bo.getBucketName()), SysOssConfigEntity::getBucketName, bo.getBucketName());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), SysOssConfigEntity::getStatus, bo.getStatus());
        lqw.orderByAsc(SysOssConfigEntity::getOssConfigId);
        return lqw;
    }

    @Override
    public Boolean insertByBo(SysOssConfigReq bo) {
        SysOssConfigEntity config = MapstructUtils.convert(bo, SysOssConfigEntity.class);
        validEntityBeforeSave(config);
        boolean flag = baseMapper.insert(config) > 0;
        if (flag) {
            // 从数据库查询完整的数据做缓存
            config = baseMapper.selectById(config.getOssConfigId());
            CacheUtils.put(CacheNames.SYS_OSS_CONFIG, config.getConfigKey(), JsonUtils.toJsonString(config));
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(SysOssConfigReq bo) {
        SysOssConfigEntity config = MapstructUtils.convert(bo, SysOssConfigEntity.class);
        validEntityBeforeSave(config);
        LambdaUpdateWrapper<SysOssConfigEntity> luw = new LambdaUpdateWrapper<>();
        luw.set(ObjectUtil.isNull(config.getPrefix()), SysOssConfigEntity::getPrefix, "");
        luw.set(ObjectUtil.isNull(config.getRegion()), SysOssConfigEntity::getRegion, "");
        luw.set(ObjectUtil.isNull(config.getExt1()), SysOssConfigEntity::getExt1, "");
        luw.set(ObjectUtil.isNull(config.getRemark()), SysOssConfigEntity::getRemark, "");
        luw.eq(SysOssConfigEntity::getOssConfigId, config.getOssConfigId());
        boolean flag = baseMapper.update(config, luw) > 0;
        if (flag) {
            // 从数据库查询完整的数据做缓存
            config = baseMapper.selectById(config.getOssConfigId());
            CacheUtils.put(CacheNames.SYS_OSS_CONFIG, config.getConfigKey(), JsonUtils.toJsonString(config));
        }
        return flag;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SysOssConfigEntity entity) {
        if (StringUtils.isNotEmpty(entity.getConfigKey())
            && !checkConfigKeyUnique(entity)) {
            throw new ServiceException("操作配置'{}'失败, 配置key已存在!", entity.getConfigKey());
        }
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            if (CollUtil.containsAny(ids, OssConstant.SYSTEM_DATA_IDS)) {
                throw new ServiceException("系统内置, 不可删除!");
            }
        }
        List<SysOssConfigEntity> list = CollUtil.newArrayList();
        for (Long configId : ids) {
            SysOssConfigEntity config = baseMapper.selectById(configId);
            list.add(config);
        }
        boolean flag = baseMapper.deleteByIds(ids) > 0;
        if (flag) {
            list.forEach(sysOssConfig ->
                CacheUtils.evict(CacheNames.SYS_OSS_CONFIG, sysOssConfig.getConfigKey()));
        }
        return flag;
    }

    /**
     * 判断configKey是否唯一
     */
    private boolean checkConfigKeyUnique(SysOssConfigEntity sysOssConfig) {
        long ossConfigId = ObjectUtils.notNull(sysOssConfig.getOssConfigId(), -1L);
        SysOssConfigEntity info = baseMapper.selectOne(new LambdaQueryWrapper<SysOssConfigEntity>()
            .select(SysOssConfigEntity::getOssConfigId, SysOssConfigEntity::getConfigKey)
            .eq(SysOssConfigEntity::getConfigKey, sysOssConfig.getConfigKey()));
        if (ObjectUtil.isNotNull(info) && info.getOssConfigId() != ossConfigId) {
            return false;
        }
        return true;
    }

    /**
     * 启用禁用状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOssConfigStatus(SysOssConfigReq bo) {
        SysOssConfigEntity sysOssConfig = MapstructUtils.convert(bo, SysOssConfigEntity.class);
        int row = baseMapper.update(null, new LambdaUpdateWrapper<SysOssConfigEntity>()
            .set(SysOssConfigEntity::getStatus, "1"));
        row += baseMapper.updateById(sysOssConfig);
        if (row > 0) {
            RedisUtils.setCacheObject(OssConstant.DEFAULT_CONFIG_KEY, sysOssConfig.getConfigKey());
        }
        return row;
    }

}

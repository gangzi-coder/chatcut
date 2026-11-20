package com.chatcut.admin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.chatcut.common.core.constant.CacheNames;
import com.chatcut.common.core.utils.MapstructUtils;
import com.chatcut.common.core.utils.StringUtils;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import com.chatcut.admin.system.repository.entity.SysClientEntity;
import com.chatcut.admin.system.model.req.SysClientReq;
import com.chatcut.admin.system.model.resp.SysClientResp;
import com.chatcut.admin.system.repository.mapper.SysClientMapper;
import com.chatcut.admin.system.service.ISysClientService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 客户端管理Service业务层处理
 *
 * @author Michelle.Chung
 * @date 2023-06-18
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysClientServiceImpl implements ISysClientService {

    private final SysClientMapper baseMapper;

    /**
     * 查询客户端管理
     */
    @Override
    public SysClientResp queryById(Long id) {
        SysClientResp vo = baseMapper.selectVoById(id);
        vo.setGrantTypeList(StringUtils.splitList(vo.getGrantType()));
        return vo;
    }

    /**
     * 查询客户端管理
     */
    @Cacheable(cacheNames = CacheNames.SYS_CLIENT, key = "#clientId")
    @Override
    public SysClientResp queryByClientId(String clientId) {
        return baseMapper.selectVoOne(new LambdaQueryWrapper<SysClientEntity>().eq(SysClientEntity::getClientId, clientId));
    }

    /**
     * 查询客户端管理列表
     */
    @Override
    public TableDataInfo<SysClientResp> queryPageList(SysClientReq bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysClientEntity> lqw = buildQueryWrapper(bo);
        Page<SysClientResp> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        result.getRecords().forEach(r -> r.setGrantTypeList(StringUtils.splitList(r.getGrantType())));
        return TableDataInfo.build(result);
    }

    /**
     * 查询客户端管理列表
     */
    @Override
    public List<SysClientResp> queryList(SysClientReq bo) {
        LambdaQueryWrapper<SysClientEntity> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SysClientEntity> buildQueryWrapper(SysClientReq bo) {
        LambdaQueryWrapper<SysClientEntity> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getClientId()), SysClientEntity::getClientId, bo.getClientId());
        lqw.eq(StringUtils.isNotBlank(bo.getClientKey()), SysClientEntity::getClientKey, bo.getClientKey());
        lqw.eq(StringUtils.isNotBlank(bo.getClientSecret()), SysClientEntity::getClientSecret, bo.getClientSecret());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), SysClientEntity::getStatus, bo.getStatus());
        lqw.orderByAsc(SysClientEntity::getId);
        return lqw;
    }

    /**
     * 新增客户端管理
     */
    @Override
    public Boolean insertByBo(SysClientReq bo) {
        SysClientEntity add = MapstructUtils.convert(bo, SysClientEntity.class);
        add.setGrantType(CollUtil.join(bo.getGrantTypeList(), StringUtils.SEPARATOR));
        // 生成clientid
        String clientKey = bo.getClientKey();
        String clientSecret = bo.getClientSecret();
        add.setClientId(SecureUtil.md5(clientKey + clientSecret));
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改客户端管理
     */
    @CacheEvict(cacheNames = CacheNames.SYS_CLIENT, key = "#bo.clientId")
    @Override
    public Boolean updateByBo(SysClientReq bo) {
        SysClientEntity update = MapstructUtils.convert(bo, SysClientEntity.class);
        update.setGrantType(StringUtils.joinComma(bo.getGrantTypeList()));
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 修改状态
     */
    @CacheEvict(cacheNames = CacheNames.SYS_CLIENT, key = "#clientId")
    @Override
    public int updateClientStatus(String clientId, String status) {
        return baseMapper.update(null,
            new LambdaUpdateWrapper<SysClientEntity>()
                .set(SysClientEntity::getStatus, status)
                .eq(SysClientEntity::getClientId, clientId));
    }

    /**
     * 批量删除客户端管理
     */
    @CacheEvict(cacheNames = CacheNames.SYS_CLIENT, allEntries = true)
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 校验客户端key是否唯一
     *
     * @param client 客户端信息
     * @return 结果
     */
    @Override
    public boolean checkClickKeyUnique(SysClientReq client) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysClientEntity>()
            .eq(SysClientEntity::getClientKey, client.getClientKey())
            .ne(ObjectUtil.isNotNull(client.getId()), SysClientEntity::getId, client.getId()));
        return !exist;
    }

}

package com.chatcut.admin.system.service.impl;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import com.chatcut.common.core.utils.MapstructUtils;
import com.chatcut.common.core.utils.StringUtils;
import com.chatcut.common.core.utils.ip.AddressUtils;
import com.chatcut.common.log.event.OperLogEvent;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import com.chatcut.admin.system.repository.entity.SysOperLogEntity;
import com.chatcut.admin.system.model.req.SysOperLogReq;
import com.chatcut.admin.system.model.resp.SysOperLogResp;
import com.chatcut.admin.system.repository.mapper.SysOperLogMapper;
import com.chatcut.admin.system.service.ISysOperLogService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 操作日志 服务层处理
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysOperLogServiceImpl implements ISysOperLogService {

    private final SysOperLogMapper baseMapper;

    /**
     * 操作日志记录
     *
     * @param operLogEvent 操作日志事件
     */
    @Async
    @EventListener
    public void recordOper(OperLogEvent operLogEvent) {
        SysOperLogReq operLog = MapstructUtils.convert(operLogEvent, SysOperLogReq.class);
        // 远程查询操作地点
        operLog.setOperLocation(AddressUtils.getRealAddressByIP(operLog.getOperIp()));
        insertOperlog(operLog);
    }

    /**
     * 分页查询操作日志列表
     *
     * @param operLog   查询条件
     * @param pageQuery 分页参数
     * @return 操作日志分页列表
     */
    @Override
    public TableDataInfo<SysOperLogResp> selectPageOperLogList(SysOperLogReq operLog, PageQuery pageQuery) {
        LambdaQueryWrapper<SysOperLogEntity> lqw = buildQueryWrapper(operLog);
        if (StringUtils.isBlank(pageQuery.getOrderByColumn())) {
            lqw.orderByDesc(SysOperLogEntity::getOperId);
        }
        Page<SysOperLogResp> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    private LambdaQueryWrapper<SysOperLogEntity> buildQueryWrapper(SysOperLogReq operLog) {
        Map<String, Object> params = operLog.getParams();
        return new LambdaQueryWrapper<SysOperLogEntity>()
            .like(StringUtils.isNotBlank(operLog.getOperIp()), SysOperLogEntity::getOperIp, operLog.getOperIp())
            .like(StringUtils.isNotBlank(operLog.getTitle()), SysOperLogEntity::getTitle, operLog.getTitle())
            .eq(operLog.getBusinessType() != null && operLog.getBusinessType() > 0,
                SysOperLogEntity::getBusinessType, operLog.getBusinessType())
            .func(f -> {
                if (ArrayUtil.isNotEmpty(operLog.getBusinessTypes())) {
                    f.in(SysOperLogEntity::getBusinessType, Arrays.asList(operLog.getBusinessTypes()));
                }
            })
            .eq(operLog.getStatus() != null,
                SysOperLogEntity::getStatus, operLog.getStatus())
            .like(StringUtils.isNotBlank(operLog.getOperName()), SysOperLogEntity::getOperName, operLog.getOperName())
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                SysOperLogEntity::getOperTime, params.get("beginTime"), params.get("endTime"));
    }

    /**
     * 新增操作日志
     *
     * @param bo 操作日志对象
     */
    @Override
    public void insertOperlog(SysOperLogReq bo) {
        SysOperLogEntity operLog = MapstructUtils.convert(bo, SysOperLogEntity.class);
        operLog.setOperTime(new Date());
        baseMapper.insert(operLog);
    }

    /**
     * 查询系统操作日志集合
     *
     * @param operLog 操作日志对象
     * @return 操作日志集合
     */
    @Override
    public List<SysOperLogResp> selectOperLogList(SysOperLogReq operLog) {
        LambdaQueryWrapper<SysOperLogEntity> lqw = buildQueryWrapper(operLog);
        return baseMapper.selectVoList(lqw.orderByDesc(SysOperLogEntity::getOperId));
    }

    /**
     * 批量删除系统操作日志
     *
     * @param operIds 需要删除的操作日志ID
     * @return 结果
     */
    @Override
    public int deleteOperLogByIds(Long[] operIds) {
        return baseMapper.deleteByIds(Arrays.asList(operIds));
    }

    /**
     * 查询操作日志详细
     *
     * @param operId 操作ID
     * @return 操作日志对象
     */
    @Override
    public SysOperLogResp selectOperLogById(Long operId) {
        return baseMapper.selectVoById(operId);
    }

    /**
     * 清空操作日志
     */
    @Override
    public void cleanOperLog() {
        baseMapper.delete(new LambdaQueryWrapper<>());
    }
}

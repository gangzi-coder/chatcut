package com.chatcut.admin.system.service;

import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import com.chatcut.admin.system.model.req.SysOperLogReq;
import com.chatcut.admin.system.model.resp.SysOperLogResp;

import java.util.List;

/**
 * 操作日志 服务层
 *
 * @author Lion Li
 */
public interface ISysOperLogService {

    /**
     * 分页查询操作日志列表
     *
     * @param operLog   查询条件
     * @param pageQuery 分页参数
     * @return 操作日志分页列表
     */
    TableDataInfo<SysOperLogResp> selectPageOperLogList(SysOperLogReq operLog, PageQuery pageQuery);

    /**
     * 新增操作日志
     *
     * @param bo 操作日志对象
     */
    void insertOperlog(SysOperLogReq bo);

    /**
     * 查询系统操作日志集合
     *
     * @param operLog 操作日志对象
     * @return 操作日志集合
     */
    List<SysOperLogResp> selectOperLogList(SysOperLogReq operLog);

    /**
     * 批量删除系统操作日志
     *
     * @param operIds 需要删除的操作日志ID
     * @return 结果
     */
    int deleteOperLogByIds(Long[] operIds);

    /**
     * 查询操作日志详细
     *
     * @param operId 操作ID
     * @return 操作日志对象
     */
    SysOperLogResp selectOperLogById(Long operId);

    /**
     * 清空操作日志
     */
    void cleanOperLog();
}

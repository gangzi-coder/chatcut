package com.chatcut.admin.system.service;

import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import com.chatcut.admin.system.model.req.SysClientReq;
import com.chatcut.admin.system.model.resp.SysClientResp;

import java.util.Collection;
import java.util.List;

/**
 * 客户端管理Service接口
 *
 * @author Michelle.Chung
 * @date 2023-06-18
 */
public interface ISysClientService {

    /**
     * 查询客户端管理
     */
    SysClientResp queryById(Long id);

    /**
     * 查询客户端信息基于客户端id
     */
    SysClientResp queryByClientId(String clientId);

    /**
     * 查询客户端管理列表
     */
    TableDataInfo<SysClientResp> queryPageList(SysClientReq bo, PageQuery pageQuery);

    /**
     * 查询客户端管理列表
     */
    List<SysClientResp> queryList(SysClientReq bo);

    /**
     * 新增客户端管理
     */
    Boolean insertByBo(SysClientReq bo);

    /**
     * 修改客户端管理
     */
    Boolean updateByBo(SysClientReq bo);

    /**
     * 修改状态
     */
    int updateClientStatus(String clientId, String status);

    /**
     * 校验并批量删除客户端管理信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 校验客户端key是否唯一
     *
     * @param client 客户端信息
     * @return 结果
     */
    boolean checkClickKeyUnique(SysClientReq client);
}

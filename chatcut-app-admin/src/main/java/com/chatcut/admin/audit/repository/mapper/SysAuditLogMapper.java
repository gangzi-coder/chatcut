package com.chatcut.admin.audit.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chatcut.admin.audit.repository.entity.SysAuditLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计主表 Mapper。
 *
 * <p>只用 BaseMapper 的 select / insert：<b>不提供 update、delete 方法</b>，
 * 从代码层面断掉篡改审计记录的可能。</p>
 */
@Mapper
public interface SysAuditLogMapper extends BaseMapper<SysAuditLogEntity> {
}

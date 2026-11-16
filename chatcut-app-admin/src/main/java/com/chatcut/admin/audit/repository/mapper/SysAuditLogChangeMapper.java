package com.chatcut.admin.audit.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chatcut.admin.audit.repository.entity.SysAuditLogChangeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计明细 Mapper。同样只读 + 只插，不提供改删。
 */
@Mapper
public interface SysAuditLogChangeMapper extends BaseMapper<SysAuditLogChangeEntity> {
}

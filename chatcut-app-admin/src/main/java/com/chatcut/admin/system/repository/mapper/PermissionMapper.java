package com.chatcut.admin.system.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chatcut.admin.system.repository.entity.SysPermissionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper extends BaseMapper<SysPermissionEntity> {}

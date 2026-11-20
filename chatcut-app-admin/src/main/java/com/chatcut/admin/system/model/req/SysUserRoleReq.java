package com.chatcut.admin.system.model.req;

import lombok.Data;

/**
 * 用户角色关联操作请求。
 */
@Data
public class SysUserRoleReq {

    /**
     * 用户编号。
     */
    private Long userId;

    /**
     * 角色编号。
     */
    private Long roleId;
}

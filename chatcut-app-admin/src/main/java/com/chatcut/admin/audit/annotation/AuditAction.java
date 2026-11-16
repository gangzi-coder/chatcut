package com.chatcut.admin.audit.annotation;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审计动作字典。
 *
 * <p>模块 / 动作码 / 动作名 / 对象类型在这里一次定义，注解、落库、查询筛选共用同一份，
 * 避免各处手写字符串导致拼写漂移。新增审计点只在本枚举加一项。</p>
 *
 * <p>{@code code} 是落库的稳定值，前端筛选依赖它，<b>不要改</b>；{@code name} 只是展示文案，可改。</p>
 */
@Getter
@AllArgsConstructor
public enum AuditAction {

    ADMIN_CREATE("管理员", "ADMIN_CREATE", "创建管理员", "admin"),
    ADMIN_STATUS("管理员", "ADMIN_STATUS", "启停管理员", "admin"),

    ROLE_CREATE("角色", "ROLE_CREATE", "创建角色", "role"),
    ROLE_UPDATE("角色", "ROLE_UPDATE", "编辑角色", "role"),
    ROLE_STATUS("角色", "ROLE_STATUS", "启停角色", "role"),

    /** 登录 / 登出没有业务对象，targetType 为空。 */
    LOGIN("认证", "LOGIN", "登录", null),
    LOGOUT("认证", "LOGOUT", "登出", null);

    /** 所属模块，用于列表筛选与展示。 */
    private final String module;

    /** 稳定动作码，落库值。 */
    private final String code;

    /** 动作中文名。 */
    private final String name;

    /** 操作对象类型；无对象的动作（登录）为 null。 */
    private final String targetType;
}

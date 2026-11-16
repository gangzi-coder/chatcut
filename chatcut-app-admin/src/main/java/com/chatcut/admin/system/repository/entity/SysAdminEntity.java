package com.chatcut.admin.system.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.chatcut.admin.audit.annotation.AuditDisplay;
import com.chatcut.admin.audit.annotation.AuditField;
import com.chatcut.common.core.enums.EnableStatus;
import com.chatcut.common.mybatis.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 管理员账号，对应表 {@code sys_admin}。
 *
 * <p>密码只存 BCrypt 哈希（password_hash），明文不落库、不进日志。
 * 审计字段用 {@link AuditField} 白名单标注：没标注的字段（如 passwordHash）永不进审计快照。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_admin")
public class SysAdminEntity extends BaseEntity {

    /** 登录用户名，全局唯一（数据库 UNIQUE KEY）。 */
    @AuditField(label = "用户名")
    private String username;

    /** 密码哈希（BCrypt）。 */
    private String passwordHash;

    /** 显示名称，前端展示用。 */
    @AuditField(label = "显示名称")
    private String displayName;

    /** 手机号，可空。 */
    @AuditField(label = "手机号")
    private String phone;

    /**
     * 账号状态：0=停用 1=启用。
     *
     * @see EnableStatus 取值枚举
     */
    @AuditField(label = "状态", display = AuditDisplay.ENABLE_STATUS)
    private Integer status;

    /** 最近登录时间，从未登录为 null。 */
    private LocalDateTime lastLoginAt;
}

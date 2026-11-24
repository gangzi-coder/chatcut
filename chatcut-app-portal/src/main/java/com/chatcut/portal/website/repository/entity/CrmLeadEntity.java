package com.chatcut.portal.website.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 官网访客线索表 crm_lead（portal 侧写入副本）。每次留资独立成线索，不做合并。
 */
@Data
@TableName("crm_lead")
public class CrmLeadEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 线索编号，唯一。 */
    private String leadNo;

    /** 公司名称。 */
    private String companyName;

    /** 联系人姓名。 */
    private String contactName;

    /** 联系方式明文：手机号或邮箱。 */
    private String contact;

    /** 联系方式类型：PHONE/EMAIL。 */
    private String contactType;

    /** 咨询意向 code：BETA_TRIAL/ENTERPRISE/API_TRIAL/TEAM_PLAN/CUSTOM。 */
    private String intent;

    /** 留资来源页面/入口，前端传入。 */
    private String source;

    /** 线索状态：PENDING/IN_PROGRESS/COMPLETED/INVALID。 */
    private String status;

    /** 有效性：UNCONFIRMED/VALID/INVALID。 */
    private String validity;

    /** 负责人管理员 ID。 */
    private Long ownerAdminId;

    /** 下次跟进时间。 */
    private LocalDateTime nextFollowAt;

    /** 处理结果或无效原因。 */
    private String result;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;

    /** 删除标志：0正常，1已删除（全局逻辑删除字段）。 */
    private String delFlag;

    /** 乐观锁版本号。 */
    @Version
    private Integer version;
}

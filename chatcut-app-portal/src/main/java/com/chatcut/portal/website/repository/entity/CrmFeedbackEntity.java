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
 * 官网访客反馈表 crm_feedback（portal 侧写入副本）。初始状态 PENDING，转线索后状态保持原值。
 */
@Data
@TableName("crm_feedback")
public class CrmFeedbackEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 防重复提交标识，前端生成，唯一。 */
    private String requestId;

    /** 反馈类别：PAGE_ISSUE/USAGE_ISSUE/CONTENT_SUGGESTION/PARTNERSHIP/OTHER。 */
    private String category;

    /** 反馈内容。 */
    private String body;

    /** 联系方式明文，可空。 */
    private String contact;

    /** 联系方式类型：PHONE/EMAIL，可空。 */
    private String contactType;

    /** 反馈来源，前端传入。 */
    private String source;

    /** 处理状态：PENDING/PROCESSING/COMPLETED/REJECTED。 */
    private String status;

    /** 处理人管理员 ID。 */
    private Long ownerAdminId;

    /** 处理结果/不予处理原因。 */
    private String result;

    /** 转化后关联线索 ID，可空。 */
    private Long leadId;

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

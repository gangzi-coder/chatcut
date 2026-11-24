package com.chatcut.admin.website.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 官网运营字段变更明细表 web_change_log。
 * 字段级前后值放子表；联系方式明文不得写入 old_value/new_value，一律记为"已变更"。
 */
@Data
@TableName("web_change_log")
public class WebChangeLogEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联审计日志或业务事件日志 ID。
     * 管理侧审计为独立事务（REQUIRES_NEW），业务事务内拿不到审计 ID，填 0；
     * 变更明细按 entity_type + entity_id 关联查询（走 idx_web_change_entity 索引）。
     */
    private Long eventId;

    /** 实体类型：CONTENT/LEAD/FEEDBACK/ASSET/SETTING。 */
    private String entityType;

    /** 实体主键。 */
    private Long entityId;

    /** 变更字段名。 */
    private String fieldName;

    /** 变更前值，长文本截断；敏感字段记"已变更"。 */
    private String oldValue;

    /** 变更后值，长文本截断；敏感字段记"已变更"。 */
    private String newValue;

    /** 记录时间。 */
    private LocalDateTime createdAt;
}

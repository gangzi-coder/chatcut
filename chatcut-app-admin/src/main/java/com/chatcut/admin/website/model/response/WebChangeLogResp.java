package com.chatcut.admin.website.model.response;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import com.chatcut.admin.website.repository.entity.WebChangeLogEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 官网运营字段变更明细视图对象 web_change_log
 */
@Data
@AutoMapper(target = WebChangeLogEntity.class)
public class WebChangeLogResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    /** 关联事件 ID（管理侧为 0，按实体关联查询）。 */
    private String eventId;

    /** 实体类型：CONTENT/LEAD/FEEDBACK/ASSET/SETTING。 */
    private String entityType;

    /** 实体主键。 */
    private String entityId;

    /** 变更字段名。 */
    private String fieldName;

    /** 变更前值；敏感字段为"已变更"。 */
    private String oldValue;

    /** 变更后值；敏感字段为"已变更"。 */
    private String newValue;

    /** 记录时间。 */
    private LocalDateTime createdAt;
}

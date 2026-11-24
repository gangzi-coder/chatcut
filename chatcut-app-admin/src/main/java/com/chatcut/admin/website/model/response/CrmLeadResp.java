package com.chatcut.admin.website.model.response;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import com.chatcut.admin.website.repository.entity.CrmLeadEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 官网访客线索视图对象 crm_lead。联系方式明文返回。
 */
@Data
@AutoMapper(target = CrmLeadEntity.class)
public class CrmLeadResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    /** 线索编号。 */
    private String leadNo;

    /** 公司名称。 */
    private String companyName;

    /** 联系人姓名。 */
    private String contactName;

    /** 联系方式明文。 */
    private String contact;

    /** 联系方式类型：PHONE/EMAIL。 */
    private String contactType;

    /** 咨询意向 code。 */
    private String intent;

    /** 留资来源。 */
    private String source;

    /** 线索状态。 */
    private String status;

    /** 有效性。 */
    private String validity;

    /** 负责人管理员 ID。 */
    private String ownerAdminId;

    /** 下次跟进时间。 */
    private LocalDateTime nextFollowAt;

    /** 处理结果或无效原因。 */
    private String result;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;

    /** 乐观锁版本号。 */
    private Integer version;
}

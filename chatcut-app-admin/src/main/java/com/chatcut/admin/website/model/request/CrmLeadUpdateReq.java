package com.chatcut.admin.website.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 线索更新请求体：分配、状态/有效性/下次跟进/结果更新。
 * 仅更新非空字段；状态变更按状态机校验，终态不可重开。
 */
@Data
public class CrmLeadUpdateReq {

    /** 主键，前端传 String。 */
    @NotBlank(message = "主键不能为空")
    private String id;

    /** 目标状态，可空（不更新）。 */
    private String status;

    /** 有效性，可空。 */
    private String validity;

    /** 负责人管理员 ID，可空。 */
    private String ownerAdminId;

    /** 下次跟进时间，可空。 */
    private LocalDateTime nextFollowAt;

    /** 处理结果或无效原因。 */
    @Size(max = 2000, message = "处理结果长度不能超过{max}个字符")
    private String result;

    /** 乐观锁版本号，必须回传。 */
    @NotNull(message = "版本号不能为空")
    private Integer version;
}

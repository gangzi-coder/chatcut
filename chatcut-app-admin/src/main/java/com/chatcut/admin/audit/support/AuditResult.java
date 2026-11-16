package com.chatcut.admin.audit.support;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审计结果三态。
 *
 * <p>「业务拒绝」与「系统异常」分开记：前者是正常的流程分支（如用户名重复），后者需要排查。
 * 审计上「尝试了但被拒」和「做成了」同等重要。</p>
 */
@Getter
@AllArgsConstructor
public enum AuditResult {

    /** 业务执行成功。 */
    SUCCESS(1, "成功"),

    /** 业务主动拒绝（BizException），如参数校验失败、对象不存在。 */
    REJECTED(2, "业务拒绝"),

    /** 未预期异常，需要排查。 */
    ERROR(3, "系统异常");

    private final int code;
    private final String desc;
}

package com.chatcut.admin.audit.support;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 一次请求内的审计上下文载体（ThreadLocal 的内容），生命周期由切面管理。
 */
@Data
class AuditPayload {

    /** 操作者；未登录场景（登录失败）由业务显式指定。 */
    private Long operatorId;
    private String operatorName;

    /** 操作对象。 */
    private String targetType;
    private String targetId;
    private String targetName;

    /** 操作摘要；为空时切面用「动作名 + 对象名」兜底。 */
    private String content;

    /** 白名单字段快照：字段名 -> 原始值。 */
    private Map<String, Object> before;
    private Map<String, Object> after;

    /** 快照所属类型，用于取字段中文名与展示方式。 */
    private Class<?> snapshotType;

    /** 非实体类操作的补充明细，落 detail 列。 */
    private Map<String, Object> detail = new LinkedHashMap<>();

    /** 字段级变更。 */
    private List<ChangeDraft> changes = new ArrayList<>();
}

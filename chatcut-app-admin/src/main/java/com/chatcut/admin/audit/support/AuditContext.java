package com.chatcut.admin.audit.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 审计上下文：业务代码往这里补充「操作了什么」，切面在方法返回后统一落库。
 *
 * <p>典型用法（service 内，与业务同一事务）：</p>
 * <pre>
 * AuditContext.target("admin", admin.getId(), admin.getUsername());
 * AuditContext.captureBefore(admin);      // 修改前抓快照
 * admin.setStatus(status.getCode());
 * adminMapper.updateById(admin);
 * AuditContext.captureAfter(admin);       // 修改后抓快照，自动算 diff
 * AuditContext.content("停用管理员 " + admin.getUsername());
 * </pre>
 *
 * <p>所有方法在无审计上下文（例如单测直接调 service）时静默跳过，不会 NPE。</p>
 */
public final class AuditContext {

    private static final ThreadLocal<AuditPayload> HOLDER = new ThreadLocal<>();

    private AuditContext() {
    }

    static void begin() {
        HOLDER.set(new AuditPayload());
    }

    static AuditPayload current() {
        return HOLDER.get();
    }

    static void clear() {
        HOLDER.remove();
    }

    /** 显式指定操作者；不指定时切面回退到当前登录管理员。 */
    public static void operator(Long id, String name) {
        AuditPayload p = HOLDER.get();
        if (p == null) {
            return;
        }
        p.setOperatorId(id);
        p.setOperatorName(name);
    }

    /** 指定操作对象：类型 + ID + 可读名称（名称必须存，否则事后看不懂 ID）。 */
    public static void target(String type, Object id, String name) {
        AuditPayload p = HOLDER.get();
        if (p == null) {
            return;
        }
        p.setTargetType(type);
        p.setTargetId(id == null ? null : String.valueOf(id));
        p.setTargetName(name);
    }

    /** 操作摘要，列表页直接展示。不设置时切面用「动作名 + 对象名」兜底。 */
    public static void content(String content) {
        AuditPayload p = HOLDER.get();
        if (p != null) {
            p.setContent(content);
        }
    }

    /** 补充明细（如权限增删清单），落 detail 列。 */
    public static void detail(String key, Object value) {
        AuditPayload p = HOLDER.get();
        if (p != null) {
            p.getDetail().put(key, value);
        }
    }

    /** 抓取修改前快照。必须在对实体做任何修改之前调用。 */
    public static void captureBefore(Object entity) {
        AuditPayload p = HOLDER.get();
        if (p == null || entity == null) {
            return;
        }
        p.setSnapshotType(entity.getClass());
        p.setBefore(AuditDiff.snapshot(entity));
    }

    /** 抓取修改后快照，并立即计算字段级 diff。 */
    public static void captureAfter(Object entity) {
        AuditPayload p = HOLDER.get();
        if (p == null || entity == null) {
            return;
        }
        p.setSnapshotType(entity.getClass());
        p.setAfter(AuditDiff.snapshot(entity));
        List<ChangeDraft> changes = AuditDiff.diff(p.getBefore(), p.getAfter(), entity.getClass());
        p.getChanges().addAll(changes);
    }

    /**
     * 集合型字段变更（如角色的 permissionIds）：按元素展开为 ADD / REMOVE 明细。
     *
     * @param field    字段名，如 permissions
     * @param label    中文名，如 权限
     * @param before   变更前 id 集合
     * @param after    变更后 id 集合
     * @param names    id -> 可读名称；缺失时展示 id
     */
    public static void collectionChange(String field, String label, Collection<Long> before,
                                        Collection<Long> after, Map<Long, String> names) {
        AuditPayload p = HOLDER.get();
        if (p == null) {
            return;
        }
        List<Long> oldIds = before == null ? List.of() : new ArrayList<>(before);
        List<Long> newIds = after == null ? List.of() : new ArrayList<>(after);

        List<String> addedNames = new ArrayList<>();
        List<String> removedNames = new ArrayList<>();

        for (Long id : newIds) {
            if (!oldIds.contains(id)) {
                String name = nameOf(names, id);
                addedNames.add(name);
                p.getChanges().add(new ChangeDraft(field, label, null, String.valueOf(id), "-", name, "ADD"));
            }
        }
        for (Long id : oldIds) {
            if (!newIds.contains(id)) {
                String name = nameOf(names, id);
                removedNames.add(name);
                p.getChanges().add(new ChangeDraft(field, label, String.valueOf(id), null, name, "-", "REMOVE"));
            }
        }

        if (!addedNames.isEmpty()) {
            p.getDetail().put(field + "Added", addedNames);
        }
        if (!removedNames.isEmpty()) {
            p.getDetail().put(field + "Removed", removedNames);
        }
    }

    private static String nameOf(Map<Long, String> names, Long id) {
        String name = names == null ? null : names.get(id);
        return name == null ? String.valueOf(id) : name;
    }
}

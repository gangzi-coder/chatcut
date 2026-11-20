package com.chatcut.admin.audit.support;

import com.chatcut.admin.audit.annotation.AuditDisplay;
import com.chatcut.admin.audit.annotation.AuditField;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 基于 {@link AuditField} 白名单的快照与 diff。
 *
 * <p>刻意不用 MyBatis 拦截器自动 diff：通用 diff 分不清业务字段、转不了字典、
 * 也看不到关联表（角色权限不在 sys_role 里），维护成本远高于收益。</p>
 */
final class AuditDiff {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 集合展示最多列这么多项，其余折叠成「…共 N 项」，避免一行几百字。 */
    private static final int MAX_DISPLAY_ITEMS = 10;

    /** 反射元数据缓存，避免每次请求都扫注解。 */
    private static final Map<Class<?>, List<FieldMeta>> CACHE = new ConcurrentHashMap<>();

    /**
     * 缓存的审计字段元数据。
     */
    private static final class FieldMeta {

        /** 反射字段。 */
        private final Field field;

        /** 字段名。 */
        private final String name;

        /** 展示名称。 */
        private final String label;

        /** 展示格式类型。 */
        private final AuditDisplay display;

        /**
         * 构造审计字段元数据。
         *
         * @param field 反射字段
         * @param name 字段名
         * @param label 展示名称
         * @param display 展示格式类型
         */
        private FieldMeta(Field field, String name, String label, AuditDisplay display) {
            this.field = field;
            this.name = name;
            this.label = label;
            this.display = display;
        }

        private Field field() { return field; }
        private String name() { return name; }
        private String label() { return label; }
        private AuditDisplay display() { return display; }
    }

    private AuditDiff() {
    }

    /** 按白名单抓取快照：字段名 -> 原始值。 */
    static Map<String, Object> snapshot(Object entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (entity == null) {
            return map;
        }
        for (FieldMeta meta : metas(entity.getClass())) {
            try {
                map.put(meta.name(), meta.field().get(entity));
            } catch (IllegalAccessException ignored) {
                // 字段不可读时跳过，快照不完整好过整个请求失败
            }
        }
        return map;
    }

    /**
     * 计算字段级变更。新增（before 为 null）不算 UPDATE——after_data 里已有完整快照，
     * 再逐字段记一遍只是噪音。
     */
    static List<ChangeDraft> diff(Map<String, Object> before, Map<String, Object> after, Class<?> type) {
        List<ChangeDraft> changes = new ArrayList<>();
        if (before == null || after == null || type == null) {
            return changes;
        }
        for (FieldMeta meta : metas(type)) {
            Object oldValue = before.get(meta.name());
            Object newValue = after.get(meta.name());
            if (Objects.equals(oldValue, newValue)) {
                continue;
            }
            changes.add(ChangeDraft.update(meta.name(), meta.label(),
                    raw(oldValue), raw(newValue),
                    display(oldValue, meta.display()), display(newValue, meta.display())));
        }
        return changes;
    }

    /** 原始值：写 old_value / new_value，供程序反查与精确匹配。 */
    static String raw(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(String::valueOf).collect(Collectors.joining(","));
        }
        if (value instanceof LocalDateTime time) {
            return time.format(TS);
        }
        return String.valueOf(value);
    }

    /** 展示值：写 old_display / new_display，字典在这里转义，查询时不再 join 字典表。 */
    static String display(Object value, AuditDisplay type) {
        if (value == null) {
            return "空";
        }
        if (type == AuditDisplay.ENABLE_STATUS && value instanceof Number number) {
            return switch (number.intValue()) {
                case 1 -> "启用";
                case 0 -> "停用";
                default -> String.valueOf(number);
            };
        }
        if (value instanceof Collection<?> collection) {
            if (collection.isEmpty()) {
                return "空";
            }
            String joined = collection.stream().limit(MAX_DISPLAY_ITEMS)
                    .map(String::valueOf).collect(Collectors.joining("、"));
            return collection.size() > MAX_DISPLAY_ITEMS ? joined + "…共" + collection.size() + "项" : joined;
        }
        if (value instanceof LocalDateTime time) {
            return time.format(TS);
        }
        if (value instanceof LocalDate date) {
            return date.toString();
        }
        if (value instanceof Boolean bool) {
            return bool ? "是" : "否";
        }
        return String.valueOf(value);
    }

    private static List<FieldMeta> metas(Class<?> type) {
        return CACHE.computeIfAbsent(type, t -> {
            List<FieldMeta> list = new ArrayList<>();
            for (Class<?> c = t; c != null && c != Object.class; c = c.getSuperclass()) {
                for (Field field : c.getDeclaredFields()) {
                    if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    AuditField annotation = field.getAnnotation(AuditField.class);
                    if (annotation == null) {
                        continue;
                    }
                    field.setAccessible(true);
                    list.add(new FieldMeta(field, field.getName(), annotation.label(), annotation.display()));
                }
            }
            return List.copyOf(list);
        });
    }
}

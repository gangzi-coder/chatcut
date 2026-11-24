package com.chatcut.admin.website.support;

import com.chatcut.admin.website.repository.entity.WebChangeLogEntity;
import com.chatcut.admin.website.repository.mapper.WebChangeLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 字段级变更明细记录器：写入 web_change_log 子表，操作记录主表只存摘要。
 *
 * <p>联系方式等敏感字段（明文）不得写入 old_value/new_value，一律记为「已变更」；
 * 长文本（正文等）截断到 1000 字符落库。</p>
 */
@Component
@RequiredArgsConstructor
public class WebChangeLogRecorder {

    /** 明文敏感字段名（小写）：只记「已变更」，不落值。 */
    private static final java.util.Set<String> SENSITIVE_FIELDS =
        java.util.Set.of("contact", "contactemail", "contactphone", "contactname", "body");

    private static final int MAX_VALUE_LENGTH = 1000;

    private final WebChangeLogMapper webChangeLogMapper;

    /**
     * 记录一批字段变更。
     *
     * @param entityType 实体类型：CONTENT/LEAD/FEEDBACK/ASSET/SETTING
     * @param entityId   实体主键
     * @param changes    字段名 -> [旧值, 新值]；值变化为 null -> null 的字段调用方应过滤掉
     */
    public void record(String entityType, Long entityId, Map<String, String[]> changes) {
        if (changes == null || changes.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<String, String[]> entry : changes.entrySet()) {
            String[] pair = entry.getValue();
            if (pair == null || pair.length != 2) {
                continue;
            }
            WebChangeLogEntity entity = new WebChangeLogEntity();
            entity.setEventId(0L);
            entity.setEntityType(entityType);
            entity.setEntityId(entityId);
            entity.setFieldName(entry.getKey());
            entity.setOldValue(maskOrTruncate(entry.getKey(), pair[0]));
            entity.setNewValue(maskOrTruncate(entry.getKey(), pair[1]));
            entity.setCreatedAt(now);
            webChangeLogMapper.insert(entity);
        }
    }

    private String maskOrTruncate(String fieldName, String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        if (SENSITIVE_FIELDS.contains(fieldName.toLowerCase())) {
            return "已变更";
        }
        return value.length() <= MAX_VALUE_LENGTH ? value : value.substring(0, MAX_VALUE_LENGTH) + "...";
    }
}

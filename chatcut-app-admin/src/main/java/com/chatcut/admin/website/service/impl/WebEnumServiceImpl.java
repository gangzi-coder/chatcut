package com.chatcut.admin.website.service.impl;

import com.chatcut.admin.website.enums.AssetStatusEnum;
import com.chatcut.admin.website.enums.AssetTypeEnum;
import com.chatcut.admin.website.enums.ContactTypeEnum;
import com.chatcut.admin.website.enums.ContentStatusEnum;
import com.chatcut.admin.website.enums.ContentTypeEnum;
import com.chatcut.admin.website.enums.CtaTargetTypeEnum;
import com.chatcut.admin.website.enums.FeedbackCategoryEnum;
import com.chatcut.admin.website.enums.FeedbackStatusEnum;
import com.chatcut.admin.website.enums.LeadIntentEnum;
import com.chatcut.admin.website.enums.LeadStatusEnum;
import com.chatcut.admin.website.enums.LeadValidityEnum;
import com.chatcut.admin.website.enums.OpenStateEnum;
import com.chatcut.admin.website.enums.VersionStatusEnum;
import com.chatcut.admin.website.model.response.WebEnumItemResp;
import com.chatcut.admin.website.service.WebEnumService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 官网公共枚举查询实现。枚举统一为 code + label 结构，反射聚合，结果不缓存（枚举量小）。
 */
@Service
public class WebEnumServiceImpl implements WebEnumService {

    /** 官网域全部对外枚举，顺序即返回顺序。 */
    private static final List<Class<?>> ENUM_CLASSES = List.of(
        ContentTypeEnum.class, ContentStatusEnum.class, OpenStateEnum.class,
        VersionStatusEnum.class, AssetTypeEnum.class, AssetStatusEnum.class,
        ContactTypeEnum.class, LeadIntentEnum.class, LeadStatusEnum.class,
        LeadValidityEnum.class, FeedbackCategoryEnum.class, FeedbackStatusEnum.class,
        CtaTargetTypeEnum.class);

    @Override
    public Map<String, List<WebEnumItemResp>> getWebEnums() {
        Map<String, List<WebEnumItemResp>> result = new LinkedHashMap<>();
        for (Class<?> clazz : ENUM_CLASSES) {
            String group = clazz.getSimpleName().endsWith("Enum")
                ? clazz.getSimpleName().substring(0, clazz.getSimpleName().length() - 4)
                : clazz.getSimpleName();
            List<WebEnumItemResp> items = new ArrayList<>();
            for (Object constant : clazz.getEnumConstants()) {
                items.add(new WebEnumItemResp(
                    invokeStringGetter(constant, "getCode"),
                    invokeStringGetter(constant, "getLabel")));
            }
            result.put(group, items);
        }
        return result;
    }

    private String invokeStringGetter(Object constant, String getter) {
        try {
            Object value = constant.getClass().getMethod(getter).invoke(constant);
            return String.valueOf(value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("枚举缺少 " + getter + " 方法: " + constant.getClass().getName(), e);
        }
    }
}

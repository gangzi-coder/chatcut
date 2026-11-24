package com.chatcut.portal.website.enums;

import com.chatcut.common.core.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 咨询意向（portal 只读副本，与 admin 端枚举保持同构）。
 * ENTERPRISE / API_TRIAL / TEAM_PLAN / CUSTOM 为企业类意向，提交时强制填写联系人姓名与公司名称。
 */
@Getter
@AllArgsConstructor
public enum LeadIntentEnum {

    BETA_TRIAL("BETA_TRIAL", "内测预约"),
    ENTERPRISE("ENTERPRISE", "企业合作"),
    API_TRIAL("API_TRIAL", "API试用"),
    TEAM_PLAN("TEAM_PLAN", "团队版咨询"),
    CUSTOM("CUSTOM", "定制接入");

    private final String code;
    private final String label;

    /** 企业类意向集合（不含内测预约）。 */
    public static final List<LeadIntentEnum> ENTERPRISE_INTENTS = List.of(ENTERPRISE, API_TRIAL, TEAM_PLAN, CUSTOM);

    public static LeadIntentEnum of(String code) {
        return Arrays.stream(values()).filter(e -> e.code.equals(code)).findFirst()
            .orElseThrow(() -> new ServiceException("非法咨询意向: " + code));
    }

    public boolean isEnterpriseIntent() {
        return ENTERPRISE_INTENTS.contains(this);
    }
}

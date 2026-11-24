package com.chatcut.admin.website.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 线索流程状态。COMPLETED / INVALID 为终态，不允许重新打开，仅管理员可更正并审计。
 */
@Getter
@AllArgsConstructor
public enum LeadStatusEnum implements EnumCodeLabel {

    PENDING("PENDING", "待处理", "Pending"),
    IN_PROGRESS("IN_PROGRESS", "跟进中", "In Progress"),
    COMPLETED("COMPLETED", "已完成", "Completed"),
    INVALID("INVALID", "无效", "Invalid");

    private final String code;
    private final String label;
    private final String labelEn;

    /** 终态集合。 */
    public static final List<LeadStatusEnum> FINAL_STATES = List.of(COMPLETED, INVALID);

    public static LeadStatusEnum of(String code) {
        return Arrays.stream(values()).filter(e -> e.code.equals(code)).findFirst()
            .orElseThrow(() -> new com.chatcut.common.core.exception.ServiceException("非法线索状态: " + code));
    }

    public boolean isFinal() {
        return FINAL_STATES.contains(this);
    }

    /**
     * 校验状态迁移合法性。
     * 正常路径 PENDING→IN_PROGRESS→COMPLETED；允许 PENDING→COMPLETED 直接完成；
     * 任意未结束状态可转 INVALID；同状态重复设置视为无操作放行。
     */
    public static void checkTransition(LeadStatusEnum from, LeadStatusEnum to) {
        if (from == to) {
            return;
        }
        if (from.isFinal()) {
            throw new com.chatcut.common.core.exception.ServiceException("线索已处于终态[" + from.label + "]，不允许重新打开");
        }
        boolean legal = switch (to) {
            case IN_PROGRESS -> from == PENDING;
            case COMPLETED -> from == PENDING || from == IN_PROGRESS;
            case INVALID -> true;
            case PENDING -> false;
        };
        if (!legal) {
            throw new com.chatcut.common.core.exception.ServiceException("线索状态不允许从[" + from.label + "]变更为[" + to.label + "]");
        }
    }
}

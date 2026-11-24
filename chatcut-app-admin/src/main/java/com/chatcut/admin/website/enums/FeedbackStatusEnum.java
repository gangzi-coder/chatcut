package com.chatcut.admin.website.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 反馈处理状态。COMPLETED / REJECTED 为终态，不允许重新打开；
 * 转线索不改变反馈状态，只建立关联。
 */
@Getter
@AllArgsConstructor
public enum FeedbackStatusEnum {

    PENDING("PENDING", "待处理"),
    PROCESSING("PROCESSING", "处理中"),
    COMPLETED("COMPLETED", "已完成"),
    REJECTED("REJECTED", "不予处理");

    private final String code;
    private final String label;

    public static final List<FeedbackStatusEnum> FINAL_STATES = List.of(COMPLETED, REJECTED);

    public static FeedbackStatusEnum of(String code) {
        return Arrays.stream(values()).filter(e -> e.code.equals(code)).findFirst()
            .orElseThrow(() -> new com.chatcut.common.core.exception.ServiceException("非法反馈状态: " + code));
    }

    public boolean isFinal() {
        return FINAL_STATES.contains(this);
    }

    /**
     * PENDING→PROCESSING→COMPLETED/REJECTED；待处理可直接结束；终态不可重开。
     */
    public static void checkTransition(FeedbackStatusEnum from, FeedbackStatusEnum to) {
        if (from == to) {
            return;
        }
        if (from.isFinal()) {
            throw new com.chatcut.common.core.exception.ServiceException("反馈已处于终态[" + from.label + "]，不允许重新打开");
        }
        boolean legal = switch (to) {
            case PROCESSING -> from == PENDING;
            case COMPLETED, REJECTED -> true;
            case PENDING -> false;
        };
        if (!legal) {
            throw new com.chatcut.common.core.exception.ServiceException("反馈状态不允许从[" + from.label + "]变更为[" + to.label + "]");
        }
    }
}

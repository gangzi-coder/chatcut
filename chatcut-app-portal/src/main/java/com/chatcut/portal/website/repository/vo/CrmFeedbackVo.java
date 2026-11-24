package com.chatcut.portal.website.repository.vo;

import com.chatcut.portal.website.repository.entity.CrmFeedbackEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 官网访客反馈 Vo（仅供 Mapper 泛型使用）。
 */
@Data
@AutoMapper(target = CrmFeedbackEntity.class)
public class CrmFeedbackVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String requestId;

    private String category;

    private String status;
}

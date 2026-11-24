package com.chatcut.portal.website.repository.vo;

import com.chatcut.portal.website.repository.entity.CrmLeadEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 官网访客线索 Vo（仅供 Mapper 泛型使用）。
 */
@Data
@AutoMapper(target = CrmLeadEntity.class)
public class CrmLeadVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String leadNo;

    private String intent;

    private String status;
}

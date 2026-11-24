package com.chatcut.portal.website.repository.vo;

import com.chatcut.portal.website.repository.entity.WebContentEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 官网内容 Vo（仅供 Mapper 泛型使用，公开响应模型见 model.response）。
 */
@Data
@AutoMapper(target = WebContentEntity.class)
public class WebContentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String contentType;

    private String groupKey;

    private String title;

    private String status;

    private Integer publishedVersion;
}

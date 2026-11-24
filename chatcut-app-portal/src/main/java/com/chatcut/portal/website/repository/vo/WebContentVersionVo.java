package com.chatcut.portal.website.repository.vo;

import com.chatcut.portal.website.repository.entity.WebContentVersionEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 官网内容发布版本 Vo（仅供 Mapper 泛型使用）。
 */
@Data
@AutoMapper(target = WebContentVersionEntity.class)
public class WebContentVersionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long contentId;

    private Integer versionNo;

    private String title;

    private String summary;

    private String body;

    private String ctaLabel;

    private String ctaTarget;

    private Long mediaId;

    private String openState;

    private String publishStatus;

    private java.time.LocalDateTime publishedAt;
}

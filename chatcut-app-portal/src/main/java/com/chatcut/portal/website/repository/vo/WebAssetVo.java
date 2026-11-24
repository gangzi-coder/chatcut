package com.chatcut.portal.website.repository.vo;

import com.chatcut.portal.website.repository.entity.WebAssetEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 官网展示素材 Vo（仅供 Mapper 泛型使用）。
 */
@Data
@AutoMapper(target = WebAssetEntity.class)
public class WebAssetVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String assetType;

    private String publicUrl;

    private String mimeType;

    private String status;
}

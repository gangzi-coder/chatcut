package com.chatcut.portal.website.repository.vo;

import com.chatcut.portal.website.repository.entity.WebSiteSettingEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 官网站点设置 Vo（仅供 Mapper 泛型使用）。
 */
@Data
@AutoMapper(target = WebSiteSettingEntity.class)
public class WebSiteSettingVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String settingKey;

    private String brand;

    private String contactEmail;

    private String contactPhone;

    private String response;
}

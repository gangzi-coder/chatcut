package com.chatcut.admin.website.model.response;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import com.chatcut.admin.website.repository.entity.WebSiteSettingEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 官网站点设置视图对象 web_site_setting
 */
@Data
@AutoMapper(target = WebSiteSettingEntity.class)
public class WebSiteSettingResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    /** 设置分组键。 */
    private String settingKey;

    /** 对外品牌名称。 */
    private String brand;

    /** 合作邮箱。 */
    private String contactEmail;

    /** 联系电话。 */
    private String contactPhone;

    /** 响应时效说明。 */
    private String response;

    /** 乐观锁版本号（更新时必须回传）。 */
    private Integer version;

    /** 最后更新管理员 ID。 */
    private String updatedBy;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}

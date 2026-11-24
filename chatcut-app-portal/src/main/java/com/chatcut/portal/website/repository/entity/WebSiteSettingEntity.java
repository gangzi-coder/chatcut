package com.chatcut.portal.website.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 官网站点设置表 web_site_setting（portal 侧只读副本）。
 * 配置摊平为列，保存即生效，官网实时查库。一期固定一行 setting_key = site_profile。
 * portal 只暴露公开列（品牌/联系/响应说明），绝不返回 version/updated_by 等管理字段。
 */
@Data
@TableName("web_site_setting")
public class WebSiteSettingEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 设置分组键，一期固定 site_profile，唯一。 */
    private String settingKey;

    /** 对外品牌名称。 */
    private String brand;

    /** 合作邮箱。 */
    private String contactEmail;

    /** 联系电话。 */
    private String contactPhone;

    /** 响应时效说明。 */
    private String response;

    /** 乐观锁版本号。 */
    @Version
    private Integer version;

    /** 最后更新管理员 ID。 */
    private Long updatedBy;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}

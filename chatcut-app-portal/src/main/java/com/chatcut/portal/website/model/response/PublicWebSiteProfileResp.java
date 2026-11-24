package com.chatcut.portal.website.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 公开站点配置。仅暴露公开列：站点名、联系与响应说明；
 * 绝不返回 version/updatedBy/updatedAt 等管理字段。
 */
@Data
@Schema(description = "公开站点配置（站点名、联系与响应说明）")
public class PublicWebSiteProfileResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 对外品牌名称。 */
    @Schema(description = "对外品牌名称", example = "ChatCut")
    private String brand;

    /** 合作邮箱。 */
    @Schema(description = "合作邮箱", example = "contact@example.com")
    private String contactEmail;

    /** 联系电话。 */
    @Schema(description = "联系电话", example = "400-000-0000")
    private String contactPhone;

    /** 响应时效说明。 */
    @Schema(description = "响应时效说明", example = "1个工作日")
    private String response;
}

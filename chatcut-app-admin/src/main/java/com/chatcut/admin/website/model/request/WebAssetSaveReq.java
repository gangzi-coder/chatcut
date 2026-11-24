package com.chatcut.admin.website.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登记素材请求体。一期仅登记元数据，文件上传由前端/OSS 完成。
 */
@Data
public class WebAssetSaveReq {

    @NotBlank(message = "素材名称不能为空")
    @Size(max = 120, message = "素材名称长度不能超过{max}个字符")
    private String name;

    /** 素材类型：TEXT/VIDEO/AUDIO。 */
    @NotBlank(message = "素材类型不能为空")
    private String assetType;

    /** 预览展示色，六位十六进制。 */
    @Size(max = 16, message = "预览色长度不能超过{max}个字符")
    private String color;

    /** OSS 对象键。 */
    @Size(max = 512, message = "对象键长度不能超过{max}个字符")
    private String objectKey;

    /** 公开访问地址。 */
    @Size(max = 1024, message = "公开地址长度不能超过{max}个字符")
    private String publicUrl;

    /** MIME 类型。 */
    @Size(max = 120, message = "MIME 类型长度不能超过{max}个字符")
    private String mimeType;

    /** 素材说明。 */
    @Size(max = 600, message = "素材说明长度不能超过{max}个字符")
    private String description;
}

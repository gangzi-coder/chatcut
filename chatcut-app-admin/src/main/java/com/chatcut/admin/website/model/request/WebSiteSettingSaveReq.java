package com.chatcut.admin.website.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新站点设置请求体。带 version 乐观锁，更新 0 行返回冲突；保存后立即生效。
 */
@Data
public class WebSiteSettingSaveReq {

    /** 对外品牌名称。 */
    @NotBlank(message = "品牌名称不能为空")
    @Size(max = 100, message = "品牌名称长度不能超过{max}个字符")
    private String brand;

    /** 合作邮箱。 */
    @NotBlank(message = "合作邮箱不能为空")
    @Size(max = 150, message = "合作邮箱长度不能超过{max}个字符")
    private String contactEmail;

    /** 联系电话，可空。 */
    @Size(max = 40, message = "联系电话长度不能超过{max}个字符")
    private String contactPhone;

    /** 响应时效说明。 */
    @NotBlank(message = "响应时效说明不能为空")
    @Size(max = 100, message = "响应时效说明长度不能超过{max}个字符")
    private String response;

    /**
     * 乐观锁版本号。已有配置行时必须回传且与当前一致（服务端校验）；
     * 无配置行（首次保存创建）时可空。
     */
    private Integer version;
}

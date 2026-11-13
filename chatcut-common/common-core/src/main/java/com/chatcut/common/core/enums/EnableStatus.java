package com.chatcut.common.core.enums;

import com.chatcut.common.core.exception.BizException;
import com.chatcut.common.core.exception.CommonErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用启用状态：管理员、角色、权限项等所有「启用/停用」语义共用。
 *
 * <p>禁止在业务代码里直接写 0/1，一律走本枚举；
 * 实体类状态字段用 {@code @see EnableStatus} 标明取值来源。</p>
 */
@Getter
@AllArgsConstructor
@Schema(description = "通用启用状态：0=停用 1=启用")
public enum EnableStatus {

    /** 停用：不可登录、不参与鉴权与角色绑定校验。 */
    DISABLED(0, "停用"),

    /** 启用：正常可用。 */
    ENABLED(1, "启用");

    /** 存入数据库 status 列的值。 */
    private final int code;

    /** 中文描述。 */
    private final String desc;

    /** 按 code 取枚举，非法值直接抛业务异常（接口入参校验用）。 */
    public static EnableStatus of(Integer code) {
        if (code == null) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "状态不能为空");
        }
        for (EnableStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new BizException(CommonErrorCode.PARAM_INVALID, "非法的状态值：" + code);
    }

    /** 是否启用；null 视为未启用（DB 字段可能为 null）。 */
    public static boolean isEnabled(Integer code) {
        return code != null && code == ENABLED.code;
    }
}

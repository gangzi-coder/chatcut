package com.chatcut.admin.system.model.resp;

import com.chatcut.admin.system.model.resp.ProfileUserResp;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Profile响应数据。
 */
@Data
@AllArgsConstructor
public class ProfileResp {

    /**
     * user.
     */
    private ProfileUserResp user;

    /**
     * roleGroup.
     */
    private String roleGroup;

    /**
     * postGroup.
     */
    private String postGroup;

}

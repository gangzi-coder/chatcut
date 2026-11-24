package com.chatcut.admin.system.controller.system;

import com.chatcut.admin.system.model.resp.ProfileResp;
import com.chatcut.admin.system.model.resp.AvatarResp;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.BCrypt;
import lombok.RequiredArgsConstructor;
import com.chatcut.common.core.domain.R;
import com.chatcut.common.core.utils.StringUtils;
import com.chatcut.common.core.utils.file.MimeTypeUtils;
import com.chatcut.common.encrypt.annotation.ApiEncrypt;
import com.chatcut.common.idempotent.annotation.RepeatSubmit;
import com.chatcut.common.log.annotation.Log;
import com.chatcut.common.log.enums.BusinessType;
import com.chatcut.common.mybatis.helper.DataPermissionHelper;
import com.chatcut.common.satoken.utils.LoginHelper;
import com.chatcut.common.web.core.BaseController;
import com.chatcut.admin.system.model.req.SysUserReq;
import com.chatcut.admin.system.model.req.SysUserPasswordReq;
import com.chatcut.admin.system.model.req.SysUserProfileReq;
import com.chatcut.admin.system.model.resp.ProfileUserResp;
import com.chatcut.admin.system.model.resp.SysOssResp;
import com.chatcut.admin.system.model.resp.SysUserResp;
import com.chatcut.admin.system.service.ISysOssService;
import com.chatcut.admin.system.service.ISysUserService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

/**
 * 个人信息 业务处理
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/user/profile")
public class SysProfileController extends BaseController {

    private final ISysUserService userService;
    private final ISysOssService ossService;

    /**
     * 个人信息
     */
    @GetMapping
    public R<ProfileResp> profile() {
        SysUserResp user = userService.selectUserById(LoginHelper.getUserId());
        String roleGroup = userService.selectUserRoleGroup(user.getUserId());
        String postGroup = userService.selectUserPostGroup(user.getUserId());
        // 单独做一个vo专门给个人中心用 避免数据被脱敏
        ProfileUserResp profileUser = BeanUtil.toBean(user, ProfileUserResp.class);
        ProfileResp profileVo = new ProfileResp(profileUser, roleGroup, postGroup);
        return R.ok(profileVo);
    }

    /**
     * 修改用户信息
     */
    @RepeatSubmit
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PostMapping("/updateSysProfile")
    public R<Void> updateProfile(@Validated @RequestBody SysUserProfileReq profile) {
        SysUserReq user = BeanUtil.toBean(profile, SysUserReq.class);
        user.setUserId(LoginHelper.getUserId());
        String username = LoginHelper.getUsername();
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return R.fail("修改用户'" + username + "'失败，手机号码已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return R.fail("修改用户'" + username + "'失败，邮箱账号已存在");
        }
        int rows = DataPermissionHelper.ignore(() -> userService.updateUserProfile(user));
        if (rows > 0) {
            return R.ok();
        }
        return R.fail("修改个人信息异常，请联系管理员");
    }

    /**
     * 重置密码
     *
     * @param bo 新旧密码
     */
    @RepeatSubmit
    @ApiEncrypt
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PostMapping("/updatePwd")
    public R<Void> updatePwd(@Validated @RequestBody SysUserPasswordReq bo) {
        SysUserResp user = userService.selectUserById(LoginHelper.getUserId());
        String password = user.getPassword();
        if (!BCrypt.checkpw(bo.getOldPassword(), password)) {
            return R.fail("修改密码失败，旧密码错误");
        }
        if (BCrypt.checkpw(bo.getNewPassword(), password)) {
            return R.fail("新密码不能与旧密码相同");
        }
        int rows = DataPermissionHelper.ignore(() -> userService.resetUserPwd(user.getUserId(), BCrypt.hashpw(bo.getNewPassword())));
        if (rows > 0) {
            return R.ok();
        }
        return R.fail("修改密码异常，请联系管理员");
    }

    /**
     * 头像上传
     *
     * @param avatarfile 用户头像
     */
    @RepeatSubmit
    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<AvatarResp> avatar(@RequestPart("avatarfile") MultipartFile avatarfile) {
        if (ObjectUtil.isNotNull(avatarfile) && !avatarfile.isEmpty()) {
            String extension = FileUtil.extName(avatarfile.getOriginalFilename());
            if (!StringUtils.equalsAnyIgnoreCase(extension, MimeTypeUtils.IMAGE_EXTENSION)) {
                return R.fail("文件格式不正确，请上传" + Arrays.toString(MimeTypeUtils.IMAGE_EXTENSION) + "格式");
            }
            SysOssResp oss = ossService.upload(avatarfile);
            String avatar = oss.getUrl();
            boolean updateSuccess = DataPermissionHelper.ignore(() -> userService.updateUserAvatar(LoginHelper.getUserId(), oss.getOssId()));
            if (updateSuccess) {
                return R.ok(new AvatarResp(avatar));
            }
        }
        return R.fail("上传图片异常，请联系管理员");
    }
}

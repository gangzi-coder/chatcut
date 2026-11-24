package com.chatcut.portal.website.controller;

import com.chatcut.common.core.domain.R;
import com.chatcut.common.ratelimiter.annotation.RateLimiter;
import com.chatcut.common.ratelimiter.enums.LimitType;
import com.chatcut.portal.website.model.request.PortalCrmFeedbackSubmitReq;
import com.chatcut.portal.website.model.request.PortalCrmLeadSubmitReq;
import com.chatcut.portal.website.model.response.PortalCaptchaTokenResp;
import com.chatcut.portal.website.model.response.PortalCrmFeedbackSubmitResp;
import com.chatcut.portal.website.model.response.PortalCrmLeadSubmitResp;
import com.chatcut.portal.website.model.response.PublicWebContentDetailResp;
import com.chatcut.portal.website.model.response.PublicWebContentItemResp;
import com.chatcut.portal.website.model.response.PublicWebSiteProfileResp;
import com.chatcut.portal.website.service.PortalCrmSubmitService;
import com.chatcut.portal.website.service.PublicWebContentService;
import com.chatcut.portal.website.service.PublicWebSiteProfileService;
import com.chatcut.portal.website.service.WebCaptchaTokenService;
import com.chatcut.portal.website.support.CaptchaPair;
import com.chatcut.portal.website.support.PortalWebsiteUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 官网公开与访客提交接口。免登录、不校验权限（portal 不接入 Sa-Token），
 * 防护手段为限流 + 验证码令牌 + 输入校验 + 防重复提交。
 *
 * <p>遵循全项目接口规范：只允许 GET/POST、无路径变量、ID 一律 String。
 * 网关 /portal/** StripPrefix=1 后落到本控制器，完整路径为
 * /portal/website/**（本机直连为 /website/**）。</p>
 */
@Tag(name = "官网公开与访客提交", description = "官网公开内容读取与访客留资/反馈提交（免登录）")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/website")
public class PortalWebsiteController {

    private final PublicWebContentService publicWebContentService;
    private final PublicWebSiteProfileService publicWebSiteProfileService;
    private final WebCaptchaTokenService webCaptchaTokenService;
    private final PortalCrmSubmitService portalCrmSubmitService;

    @Operation(summary = "公开内容列表（可按类型/分组筛选，只返回已发布内容）",
            description = "contentType 与 groupKey 均可省略（返回全部已发布内容）；两个条件可叠加过滤。"
                    + "分组标识为可扩展字典，取值以运营后台创建的分组为准。")
    @GetMapping("/listPublicWebContents")
    public R<List<PublicWebContentItemResp>> listPublicWebContents(
            @Parameter(description = "内容类型：PAGE=页面区块 / NOTICE=公告 / CASE=展示案例 / RESOURCE=资源文档 / POLICY=法律条款 / PRODUCT=产品方案",
                    schema = @Schema(allowableValues = {"PAGE", "NOTICE", "CASE", "RESOURCE", "POLICY", "PRODUCT"}, example = "PRODUCT"))
            @RequestParam(value = "contentType", required = false) String contentType,
            @Parameter(description = "内容分组标识（可扩展字典）：当前已有 home=首页；其余分组（企业服务/资源与支持/法律与信任等）由运营后台创建时定义",
                    schema = @Schema(example = "home"))
            @RequestParam(value = "groupKey", required = false) String groupKey) {
        return R.ok(publicWebContentService.listPublicWebContents(contentType, groupKey));
    }

    @Operation(summary = "公开内容详情（读取当前发布版本，过滤草稿/下线/不存在）")
    @GetMapping("/getPublicWebContentInfo")
    public R<PublicWebContentDetailResp> getPublicWebContentInfo(
            @Parameter(description = "内容 ID（String，来自列表接口返回值）", example = "2124773869412671489", required = true)
            @NotBlank(message = "主键不能为空") @RequestParam("id") String id) {
        return R.ok(publicWebContentService.getPublicWebContentDetail(id));
    }

    @Operation(summary = "公开站点配置（站点名、联系与响应说明）")
    @GetMapping("/getPublicWebSiteProfile")
    public R<PublicWebSiteProfileResp> getPublicWebSiteProfile() {
        return R.ok(publicWebSiteProfileService.getPublicWebSiteProfile());
    }

    @Operation(summary = "生成一次性验证码凭据对（requestId + captchaToken 成对下发）",
            description = "requestId 已密码学绑定进令牌签名，提交留资/反馈时须成对原样回传；"
                    + "默认 10 分钟有效且一次性使用（token 与 requestId 任一被用过即失效）；"
                    + "只返回凭据，不发送短信/邮件。限流：同 IP 每分钟 30 次。")
    @RateLimiter(limitType = LimitType.IP, time = 60, count = 30, message = "请求过于频繁，请稍后再试")
    @GetMapping("/getWebCaptchaToken")
    public R<PortalCaptchaTokenResp> getWebCaptchaToken() {
        CaptchaPair pair = webCaptchaTokenService.generateCaptchaPair();
        return R.ok("操作成功", new PortalCaptchaTokenResp(pair.getRequestId(), pair.getCaptchaToken()));
    }

    @Operation(summary = "访客提交留资（企业类意向必填联系人和公司）",
            description = "先调 GET /getWebCaptchaToken 取凭据对，requestId 与 captchaToken 成对原样回传；"
                    + "token 与 requestId 任一已被使用即拒绝（多实例共享核销标记）；"
                    + "同凭据对重放会幂等返回原线索编号。"
                    + "intent=ENTERPRISE/API_TRIAL/TEAM_PLAN/CUSTOM 时"
                    + "contactName 与 companyName 必填；contactType=PHONE 时校验手机号格式、EMAIL 时校验邮箱格式。限流：同 IP 每分钟 5 次。")
    @RateLimiter(limitType = LimitType.IP, time = 60, count = 5, message = "请求过于频繁，请稍后再试")
    @PostMapping("/submitCrmLead")
    public R<PortalCrmLeadSubmitResp> submitCrmLead(@Validated @RequestBody PortalCrmLeadSubmitReq request) {
        PortalCrmLeadSubmitResp resp = portalCrmSubmitService.submitCrmLead(request);
        // 重复提交返回幂等成功语义（原线索编号），不向访客报错
        return Boolean.TRUE.equals(resp.getDuplicate())
                ? R.ok("请勿重复提交，已收到您的留资", resp)
                : R.ok(resp);
    }

    @Operation(summary = "访客提交反馈（初始状态待处理）",
            description = "先调 GET /getWebCaptchaToken 取凭据对，requestId 与 captchaToken 成对原样回传；"
                    + "token 与 requestId 任一已被使用即拒绝；同凭据对重放幂等返回。"
                    + "category 取值见请求模型（页面问题/使用问题/内容建议/合作咨询/其他）；联系方式可选，填写时必须同时给 contactType。限流：同 IP 每分钟 5 次。")
    @RateLimiter(limitType = LimitType.IP, time = 60, count = 5, message = "请求过于频繁，请稍后再试")
    @PostMapping("/submitCrmFeedback")
    public R<PortalCrmFeedbackSubmitResp> submitCrmFeedback(@Validated @RequestBody PortalCrmFeedbackSubmitReq request) {
        PortalCrmFeedbackSubmitResp resp = portalCrmSubmitService.submitCrmFeedback(request);
        return Boolean.TRUE.equals(resp.getDuplicate())
                ? R.ok("请勿重复提交，已收到您的反馈", resp)
                : R.ok(resp);
    }

}

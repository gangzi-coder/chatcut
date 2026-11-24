package com.chatcut.portal.website.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公开内容详情。读取 web_content 当前发布版本（web_content_version），
 * 过滤草稿/已下线/不存在的内容。媒体字段来自关联素材（web_asset），素材不可用时不返回。
 */
@Data
@Schema(description = "公开内容详情（只含公开展示字段）")
public class PublicWebContentDetailResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 内容 ID（String，规避 JS 长整型精度丢失）。 */
    @Schema(description = "内容 ID（String）", example = "2124773869412671489")
    private String id;

    /** 内容类型：PAGE/NOTICE/CASE/RESOURCE/POLICY/PRODUCT。 */
    @Schema(description = "内容类型：PAGE=页面区块 / NOTICE=公告 / CASE=展示案例 / RESOURCE=资源文档 / POLICY=法律条款 / PRODUCT=产品方案",
        allowableValues = {"PAGE", "NOTICE", "CASE", "RESOURCE", "POLICY", "PRODUCT"}, example = "PRODUCT")
    private String contentType;

    /** 内容分组。 */
    @Schema(description = "内容分组标识（可扩展字典），如 home=首页", example = "home")
    private String groupKey;

    /** 发布标题。 */
    @Schema(description = "发布标题", example = "测试产品A-v2")
    private String title;

    /** 发布摘要。 */
    @Schema(description = "发布摘要", example = "这是摘要")
    private String summary;

    /** 发布正文（纯文本，换行保留）。 */
    @Schema(description = "发布正文（纯文本，换行保留）")
    private String body;

    /** 开放状态：PREVIEW/AVAILABLE/PAUSED。 */
    @Schema(description = "开放状态：PREVIEW=预告（可收集预约） / AVAILABLE=可用 / PAUSED=暂停", allowableValues = {"PREVIEW", "AVAILABLE", "PAUSED"}, example = "AVAILABLE")
    private String openState;

    /** 按钮文案，可空。 */
    @Schema(description = "按钮文案，未配置按钮为空", example = "立即咨询")
    private String ctaLabel;

    /** 按钮目标原始串：lead:意向 / content:内容ID / https://地址，可空。 */
    @Schema(description = "按钮目标：lead:<意向>（留资）/ content:<内容ID>（站内跳转）/ https://地址（外链），未配置按钮为空", example = "lead:BETA_TRIAL")
    private String ctaTarget;

    /** 按钮目标类型：LEAD/CONTENT/EXTERNAL，未配置按钮为空。 */
    @Schema(description = "按钮目标类型：LEAD=留资 / CONTENT=站内内容 / EXTERNAL=外链，未配置按钮为空", allowableValues = {"LEAD", "CONTENT", "EXTERNAL"}, example = "LEAD")
    private String ctaTargetType;

    /** 关联媒体名称，无关联或素材已删除为空。 */
    @Schema(description = "关联媒体名称，无关联或素材已删除为空", example = "产品介绍视频")
    private String mediaName;

    /** 关联媒体公开访问地址，无关联或素材已删除为空。 */
    @Schema(description = "关联媒体公开访问地址，无关联或素材已删除为空")
    private String mediaUrl;

    /** 关联媒体 MIME 类型，无关联或素材已删除为空。 */
    @Schema(description = "关联媒体 MIME 类型，无关联或素材已删除为空", example = "video/mp4")
    private String mediaMimeType;

    /** 关联媒体类型：TEXT/VIDEO/AUDIO，无关联或素材已删除为空。 */
    @Schema(description = "关联媒体类型：TEXT=文本 / VIDEO=视频 / AUDIO=声音，无关联或素材已删除为空", allowableValues = {"TEXT", "VIDEO", "AUDIO"}, example = "VIDEO")
    private String mediaAssetType;

    /** 发布时间。 */
    @Schema(description = "发布时间（yyyy-MM-dd HH:mm:ss）", example = "2026-11-23 15:55:42")
    private LocalDateTime publishedAt;
}

package com.chatcut.portal.website.support;

import cn.hutool.http.HtmlUtil;
import com.chatcut.common.core.exception.ServiceException;
import com.chatcut.common.core.utils.StringUtils;
import com.chatcut.portal.website.enums.ContactTypeEnum;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * portal 官网域小工具：ID 解析、访客文本 HTML 转义、联系方式格式校验、线索编号生成。
 */
public final class PortalWebsiteUtils {

    /** 手机号格式（大陆 11 位）。 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /** 邮箱格式。 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final DateTimeFormatter LEAD_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private PortalWebsiteUtils() {
    }

    /**
     * 前端 ID 统一走 String（规避 JS 长整型精度丢失），此处转回 Long。
     */
    public static Long parseId(String id) {
        if (StringUtils.isBlank(id)) {
            throw new ServiceException("主键不能为空");
        }
        try {
            return Long.valueOf(id.trim());
        } catch (NumberFormatException e) {
            throw new ServiceException("非法 ID：" + id);
        }
    }

    /**
     * 访客自由文本统一做 HTML 转义（XSS 防护），入库即转义、展示端可直接输出。
     */
    public static String escapeText(String text) {
        return text == null ? null : HtmlUtil.escape(text);
    }

    /**
     * HTML 转义 + 转义后列宽校验。
     *
     * <p>转义会把 {@code < > " ' &} 膨胀为 {@code &lt; &gt; &quot; &#39; &amp;}，
     * 原文可通过 @Size(raw 长度) 校验但转义后可能超过数据库列宽，直接落库会变成
     * MysqlDataTruncation 裸 500。此处统一在入库前校验转义后长度，超长抛友好业务异常。</p>
     *
     * @param text            原始文本，可空
     * @param columnMaxLength 数据库列宽（与建表 SQL 对齐）
     * @return 转义后的文本
     * @throws ServiceException 转义后长度超过列宽
     */
    public static String escapeTextWithColumnLimit(String text, int columnMaxLength) {
        if (text == null) {
            return null;
        }
        String escaped = escapeText(text);
        if (escaped.length() > columnMaxLength) {
            throw new ServiceException("提交内容过长，请精简后重试");
        }
        return escaped;
    }

    /**
     * 联系方式格式校验：PHONE 大陆手机号正则，EMAIL 常规邮箱格式。
     */
    public static void validateContact(ContactTypeEnum contactType, String contact) {
        if (contactType == null || StringUtils.isBlank(contact)) {
            throw new ServiceException("联系方式不能为空");
        }
        String trimmed = contact.trim();
        if (contactType == ContactTypeEnum.PHONE && !PHONE_PATTERN.matcher(trimmed).matches()) {
            throw new ServiceException("手机号格式不正确");
        }
        if (contactType == ContactTypeEnum.EMAIL && !EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new ServiceException("邮箱格式不正确");
        }
    }

    /**
     * 生成线索编号：LD + 时间戳 + 6 位随机（LD + 14 + 6 = 22 字符，crm_lead.lead_no VARCHAR(32)）。
     *
     * <p>多实例安全：时间戳 + 随机段天然实例间独立，跨实例撞号概率极低；
     * crm_lead.lead_no 唯一键（uk_crm_lead_no）权威兜底，写入方对冲突做换号重试。</p>
     */
    public static String nextLeadNo() {
        return "LD" + LocalDateTime.now().format(LEAD_NO_FORMAT)
            + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }
}

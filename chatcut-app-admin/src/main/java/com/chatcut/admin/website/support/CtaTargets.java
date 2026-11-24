package com.chatcut.admin.website.support;

import com.chatcut.admin.website.enums.CtaTargetTypeEnum;
import com.chatcut.admin.website.enums.LeadIntentEnum;
import org.springframework.util.StringUtils;

import java.net.URI;

/**
 * 按钮目标（CTA target）解析与校验。
 *
 * <p>目标限三种前缀：{@code lead:<意向>} 收集留资、{@code content:<内容ID>} 站内跳转、
 * {@code https://...} 外链。文案与目标必须成对；外链仅 HTTPS 且不允许携带用户名/密码。</p>
 */
public final class CtaTargets {

    /** 解析结果：类型 + 前缀后的值（content 的值为内容 ID 十进制串）。 */
    public static final class TargetRef {

        private final CtaTargetTypeEnum type;
        private final String value;

        public TargetRef(CtaTargetTypeEnum type, String value) {
            this.type = type;
            this.value = value;
        }

        public CtaTargetTypeEnum getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }

    private CtaTargets() {
    }

    /**
     * 解析目标串。null / 空白表示未配置按钮，返回 null。
     *
     * @throws com.chatcut.common.core.exception.ServiceException 格式非法
     */
    public static TargetRef parse(String target) {
        if (!StringUtils.hasText(target)) {
            return null;
        }
        String trimmed = target.trim();
        if (trimmed.startsWith("lead:")) {
            String intent = trimmed.substring("lead:".length()).trim();
            if (!StringUtils.hasText(intent)) {
                throw new com.chatcut.common.core.exception.ServiceException("按钮目标 lead: 后缺少意向编码");
            }
            LeadIntentEnum.of(intent);
            return new TargetRef(CtaTargetTypeEnum.LEAD, intent);
        }
        if (trimmed.startsWith("content:")) {
            String id = trimmed.substring("content:".length()).trim();
            if (!id.matches("\\d+")) {
                throw new com.chatcut.common.core.exception.ServiceException("按钮目标 content: 后必须是内容 ID 数字");
            }
            return new TargetRef(CtaTargetTypeEnum.CONTENT, id);
        }
        if (trimmed.startsWith("https://")) {
            if (trimmed.length() <= "https://".length()) {
                throw new com.chatcut.common.core.exception.ServiceException("按钮目标外链地址不完整");
            }
            try {
                URI uri = URI.create(trimmed);
                if (uri.getUserInfo() != null) {
                    throw new com.chatcut.common.core.exception.ServiceException("外链不允许携带用户名/密码");
                }
            } catch (IllegalArgumentException e) {
                if (e.getMessage() != null && e.getMessage().contains("用户名")) {
                    throw e;
                }
                throw new com.chatcut.common.core.exception.ServiceException("外链地址格式非法");
            }
            return new TargetRef(CtaTargetTypeEnum.EXTERNAL, trimmed);
        }
        throw new com.chatcut.common.core.exception.ServiceException("按钮目标必须以 lead: / content: / https:// 开头");
    }

    /** 目标串是否指向指定内容 ID（用于下线引用检查的精确匹配）。 */
    public static boolean refersToContent(String target, Long contentId) {
        if (!StringUtils.hasText(target) || contentId == null) {
            return false;
        }
        return ("content:" + contentId).equals(target.trim());
    }
}

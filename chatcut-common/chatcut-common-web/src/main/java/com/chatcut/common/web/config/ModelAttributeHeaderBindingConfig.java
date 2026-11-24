package com.chatcut.common.web.config;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;

import java.util.Set;

/**
 * 全局禁用 Spring 6.1+ 的「请求头绑定进 @ModelAttribute」行为。
 *
 * <p>Spring 6.1 起 {@link ExtendedServletRequestDataBinder#addBindValues} 会把全部请求头
 * 归一化后绑定进 GET 查询用的 Req 对象（如 {@code Content-Type} 头 → {@code contentType} 字段，
 * {@code Priority}/{@code Sec-*} 头除外）。只要 Req 存在与标准头同名的字段，前端只要带上该头
 * （或代理网关附加头），查询条件即被污染——典型症状是列表筛选突然查不出任何数据。</p>
 *
 * <p>本工程所有 @ModelAttribute 只来自 query 参数，不存在 header 绑定诉求，全局禁用该行为。
 * 若未来确需 header 绑定，应在具体字段上显式使用 {@code @RequestHeader}。</p>
 */
@ControllerAdvice
public class ModelAttributeHeaderBindingConfig {

    /** 这几个头对探测/代理有意义，且几乎不会与业务字段同名，保留默认排除语义仅作文档说明。 */
    static final Set<String> NOTE_ONLY = Set.of("priority", "sec-fetch-mode");

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        if (binder instanceof ExtendedServletRequestDataBinder ex) {
            ex.setHeaderPredicate(name -> false);
        }
    }
}

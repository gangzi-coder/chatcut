/**
 * 服务内部端点：给网关、巡检、联调用的非业务接口，<b>不对前端开放</b>。
 *
 * <p>这里的接口不参与 RBAC，不写审计（不产生业务变更），也不返回敏感数据；
 * 放行靠 Sa-Token 白名单 + 网关限流，生产部署不要把服务端口直接暴露到公网。</p>
 *
 * <p>业务接口一律进域包（{@code system} / {@code content} / {@code audit}），不要图省事放这里。</p>
 */
package com.chatcut.admin.internal;

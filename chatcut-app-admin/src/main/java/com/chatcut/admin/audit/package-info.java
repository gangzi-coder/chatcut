/**
 * 审计域：管理员操作留痕、登录登出记录、审计日志查询。
 *
 * <p>分层与 {@code com.chatcut.admin.system} 同构（repository / model / service / controller），
 * 另有两个审计专属子包：</p>
 * <ul>
 *   <li>{@code annotation} —— 埋点入口：{@code @AuditLog}、{@code @AuditField}，
 *       以及动作、展示方式字典。业务代码只依赖这一层和 {@code AuditContext}；</li>
 *   <li>{@code support} —— 埋点实现：切面、ThreadLocal 上下文、白名单快照与 diff、待落库记录组装。
 *       包内协作，除 {@code AuditContext} 外不对外暴露，业务侧不要引用。</li>
 * </ul>
 *
 * <p>写入侧（{@code service.AuditLogService}）只 INSERT、独立事务，业务回滚不影响留痕；
 * 查询侧（{@code service.AuditQueryService}）只读，不提供任何改删接口——审计数据由数据库权限兜底。</p>
 */
package com.chatcut.admin.audit;

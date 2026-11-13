/**
 * 系统管理：管理员、角色、权限、字典、参数。
 *
 * <p>包结构约定（api 服务各业务域同构）：</p>
 * <ul>
 *   <li>{@code repository} —— 数据层：entity（*Entity，类与字段必须有 javadoc，状态字段 {@code @see} 标明取值枚举）+ mapper；</li>
 *   <li>{@code model.req} —— 入参，类名以 Req 结尾；{@code model.resp} —— 出参，类名以 Resp 结尾；</li>
 *   <li>{@code service} —— 业务逻辑与事务；DO 只在 service 内部使用，禁止透出到 controller；</li>
 *   <li>{@code controller} —— 参数校验、权限注解、分布式锁入口；请求方式只允许 GET / POST。</li>
 * </ul>
 */
package com.chatcut.admin.system;

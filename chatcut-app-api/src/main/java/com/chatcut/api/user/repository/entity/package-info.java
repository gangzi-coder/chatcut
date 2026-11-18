/**
 * user 域实体（表名驼峰 + Entity）。类与每个字段必须写 javadoc，状态字段用 @see 标明取值枚举。
 * <p>启动类 @MapperScan 的通配 {@code com.chatcut.api.**.repository.mapper} 要求 repository 包
 * 真实存在（IDE 静态检查），新建业务域时必须同步建 repository/{entity,mapper} 空包占位。</p>
 */
package com.chatcut.api.user.repository.entity;

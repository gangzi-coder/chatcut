package com.chatcut.portal.website.repository.mapper;

import com.chatcut.common.mybatis.core.mapper.BaseMapperPlus;
import com.chatcut.portal.website.repository.entity.WebContentEntity;
import com.chatcut.portal.website.repository.vo.WebContentVo;

/**
 * 官网内容 数据层（portal 只读副本）
 */
public interface WebContentMapper extends BaseMapperPlus<WebContentEntity, WebContentVo> {
}

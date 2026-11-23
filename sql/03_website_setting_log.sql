-- 官网运营后台 · 站点设置与变更留痕
-- 库：chatcut_admin_ruoyi
-- 对应设计文档：../官网运营后台后端设计.md 第 3.9 ~ 3.11 节

CREATE TABLE IF NOT EXISTS web_site_setting (
  id BIGINT NOT NULL COMMENT '站点设置主键，雪花 ID' PRIMARY KEY,
  setting_key VARCHAR(64) NOT NULL COMMENT '设置分组键，一期固定 site_profile',
  brand VARCHAR(100) NOT NULL COMMENT '对外品牌名称',
  contact_email VARCHAR(150) NOT NULL COMMENT '合作邮箱',
  contact_phone VARCHAR(40) NULL COMMENT '联系电话',
  response VARCHAR(100) NOT NULL COMMENT '响应时效说明',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  updated_by BIGINT NULL COMMENT '最后更新管理员 ID',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  UNIQUE KEY uk_web_site_setting_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='官网站点设置';

CREATE TABLE IF NOT EXISTS web_change_log (
  id BIGINT NOT NULL COMMENT '变更明细主键，雪花 ID' PRIMARY KEY,
  event_id BIGINT NOT NULL COMMENT '关联审计日志或业务事件日志 ID',
  entity_type VARCHAR(32) NOT NULL COMMENT '实体类型：CONTENT/LEAD/FEEDBACK/ASSET/SETTING',
  entity_id BIGINT NOT NULL COMMENT '实体主键',
  field_name VARCHAR(64) NOT NULL COMMENT '变更字段名',
  old_value VARCHAR(1000) NULL COMMENT '变更前值，长文本截断',
  new_value VARCHAR(1000) NULL COMMENT '变更后值，长文本截断',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  KEY idx_web_change_event (event_id),
  KEY idx_web_change_entity (entity_type, entity_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='官网运营字段变更明细';

-- 操作审计摘要复用项目已有表 sys_audit_log / sys_audit_log_change，
-- 定义见同目录 00_ruoyi_admin_init.sql，本目录不重复维护。

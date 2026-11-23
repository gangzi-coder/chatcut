-- 官网运营后台 · 线索与反馈
-- 库：chatcut_admin_ruoyi
-- 对应设计文档：../官网运营后台后端设计.md 第 3.5 ~ 3.8 节

CREATE TABLE IF NOT EXISTS crm_lead (
  id BIGINT NOT NULL COMMENT '线索主键，雪花 ID' PRIMARY KEY,
  lead_no VARCHAR(32) NOT NULL COMMENT '线索编号',
  company_name VARCHAR(120) NULL COMMENT '公司名称',
  contact_name VARCHAR(80) NULL COMMENT '联系人姓名',
  contact VARCHAR(200) NOT NULL COMMENT '联系方式明文：手机号或邮箱',
  contact_type VARCHAR(16) NOT NULL COMMENT '联系方式类型：PHONE手机号，EMAIL邮箱',
  intent VARCHAR(24) NOT NULL COMMENT '咨询意向 code：BETA_TRIAL内测预约，ENTERPRISE企业合作，API_TRIAL API试用，TEAM_PLAN团队版咨询，CUSTOM定制接入；字典可扩展',
  source VARCHAR(200) NULL COMMENT '留资来源页面/入口，前端传入',
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '线索状态：PENDING待处理，IN_PROGRESS跟进中，COMPLETED已完成，INVALID无效',
  validity VARCHAR(16) NOT NULL DEFAULT 'UNCONFIRMED' COMMENT '有效性：UNCONFIRMED待确认，VALID有效，INVALID无效',
  owner_admin_id BIGINT NULL COMMENT '负责人管理员 ID',
  next_follow_at DATETIME NULL COMMENT '下次跟进时间',
  result VARCHAR(2000) NULL COMMENT '处理结果或无效原因',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0正常，1已删除',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  UNIQUE KEY uk_crm_lead_no (lead_no),
  KEY idx_crm_lead_work (status, owner_admin_id, updated_at),
  KEY idx_crm_lead_intent (intent, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='官网访客线索';

CREATE TABLE IF NOT EXISTS crm_lead_submission (
  id BIGINT NOT NULL COMMENT '留资提交主键，雪花 ID' PRIMARY KEY,
  request_id VARCHAR(64) NOT NULL COMMENT '防重复提交标识，前端生成',
  captcha_token VARCHAR(64) NULL COMMENT '后端生成的一次性验证码令牌，校验后作废',
  lead_id BIGINT NOT NULL COMMENT '线索 ID，对应 crm_lead.id',
  source VARCHAR(200) NULL COMMENT '本次提交来源，前端传入',
  content VARCHAR(4000) NULL COMMENT '本次补充需求原文',
  submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  ip_hash BINARY(32) NULL COMMENT '客户端 IP HMAC 摘要，仅用于限流',
  UNIQUE KEY uk_crm_submission_request (request_id),
  UNIQUE KEY uk_crm_submission_captcha (captcha_token),
  KEY idx_crm_submission_lead (lead_id, submitted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='线索留资提交记录';

CREATE TABLE IF NOT EXISTS crm_lead_note (
  id BIGINT NOT NULL COMMENT '跟进记录主键，雪花 ID' PRIMARY KEY,
  lead_id BIGINT NOT NULL COMMENT '线索 ID，对应 crm_lead.id',
  author_admin_id BIGINT NOT NULL COMMENT '记录作者管理员 ID',
  note VARCHAR(4000) NOT NULL COMMENT '内部跟进内容，不向访客发送',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  KEY idx_crm_lead_note_time (lead_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='线索内部跟进记录';

CREATE TABLE IF NOT EXISTS crm_feedback (
  id BIGINT NOT NULL COMMENT '反馈主键，雪花 ID' PRIMARY KEY,
  request_id VARCHAR(64) NOT NULL COMMENT '防重复提交标识，前端生成',
  category VARCHAR(80) NOT NULL COMMENT '反馈类别：PAGE_ISSUE页面问题，USAGE_ISSUE使用问题，CONTENT_SUGGESTION内容建议，PARTNERSHIP合作咨询，OTHER其他',
  body VARCHAR(4000) NOT NULL COMMENT '反馈内容',
  contact VARCHAR(200) NULL COMMENT '联系方式明文，可空',
  contact_type VARCHAR(16) NULL COMMENT '联系方式类型：PHONE手机号，EMAIL邮箱；可空',
  source VARCHAR(200) NULL COMMENT '反馈来源，前端传入',
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '处理状态：PENDING待处理，PROCESSING处理中，COMPLETED已完成，REJECTED不予处理',
  owner_admin_id BIGINT NULL COMMENT '处理人管理员 ID',
  result VARCHAR(2000) NULL COMMENT '处理结果/不予处理原因',
  lead_id BIGINT NULL COMMENT '转化后关联线索 ID，可空',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0正常，1已删除',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  UNIQUE KEY uk_crm_feedback_request (request_id),
  KEY idx_crm_feedback_work (status, owner_admin_id, updated_at),
  KEY idx_crm_feedback_lead (lead_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='官网访客反馈';

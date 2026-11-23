-- 官网运营后台 · 内容与素材
-- 库：chatcut_admin_ruoyi
-- 对应设计文档：../官网运营后台后端设计.md 第 3.1 ~ 3.4 节

CREATE TABLE IF NOT EXISTS web_content (
  id BIGINT NOT NULL COMMENT '内容主键，雪花 ID' PRIMARY KEY,
  content_type VARCHAR(24) NOT NULL COMMENT '内容类型：PAGE页面区块，NOTICE公告，CASE展示案例，RESOURCE资源文档，POLICY法律条款，PRODUCT产品方案',
  group_key VARCHAR(64) NOT NULL COMMENT '内容分组：首页/企业服务/资源与支持/法律与信任，可扩展字典',
  title VARCHAR(120) NOT NULL COMMENT '当前展示标题',
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '内容状态：DRAFT草稿，PUBLISHED已发布，OFFLINE已下线',
  published_version INT NULL COMMENT '当前正式发布版本号',
  created_by BIGINT NULL COMMENT '创建管理员 ID',
  updated_by BIGINT NULL COMMENT '最后更新管理员 ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0正常，1已删除',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  KEY idx_web_content_list (status, content_type, group_key, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='官网内容';

CREATE TABLE IF NOT EXISTS web_content_draft (
  id BIGINT NOT NULL COMMENT '草稿主键，雪花 ID' PRIMARY KEY,
  content_id BIGINT NOT NULL COMMENT '内容 ID，对应 web_content.id',
  title VARCHAR(120) NOT NULL COMMENT '草稿标题',
  summary VARCHAR(600) NULL COMMENT '摘要，发布前必填',
  body TEXT NULL COMMENT '正文，纯文本，换行保留',
  cta_label VARCHAR(40) NULL COMMENT '按钮文案',
  cta_target VARCHAR(500) NULL COMMENT '按钮目标：lead:意向 / content:内容ID / https://地址',
  media_id BIGINT NULL COMMENT '关联素材 ID',
  open_state VARCHAR(16) NOT NULL DEFAULT 'PREVIEW' COMMENT '开放状态：PREVIEW预告，AVAILABLE可用，PAUSED暂停',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  UNIQUE KEY uk_web_content_draft (content_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='官网内容草稿';

CREATE TABLE IF NOT EXISTS web_content_version (
  id BIGINT NOT NULL COMMENT '版本主键，雪花 ID' PRIMARY KEY,
  content_id BIGINT NOT NULL COMMENT '内容 ID，对应 web_content.id',
  version_no INT NOT NULL COMMENT '版本号，从 1 递增',
  title VARCHAR(120) NOT NULL COMMENT '发布标题',
  summary VARCHAR(600) NULL COMMENT '发布摘要',
  body TEXT NULL COMMENT '发布正文',
  cta_label VARCHAR(40) NULL COMMENT '按钮文案',
  cta_target VARCHAR(500) NULL COMMENT '按钮目标',
  media_id BIGINT NULL COMMENT '关联素材 ID',
  open_state VARCHAR(16) NOT NULL COMMENT '开放状态：PREVIEW/AVAILABLE/PAUSED',
  publish_status VARCHAR(16) NOT NULL DEFAULT 'PUBLISHED' COMMENT '版本状态：PUBLISHED当前或曾发布，ARCHIVED已归档',
  published_at DATETIME NOT NULL COMMENT '发布时间',
  created_by BIGINT NULL COMMENT '发布管理员 ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  UNIQUE KEY uk_web_content_version_no (content_id, version_no),
  KEY idx_web_content_version_time (content_id, published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='官网内容发布版本';

CREATE TABLE IF NOT EXISTS web_asset (
  id BIGINT NOT NULL COMMENT '素材主键，雪花 ID' PRIMARY KEY,
  name VARCHAR(120) NOT NULL COMMENT '素材名称',
  asset_type VARCHAR(16) NOT NULL COMMENT '素材类型：TEXT文本，VIDEO视频，AUDIO声音',
  color VARCHAR(16) NULL COMMENT '预览展示色，六位十六进制',
  object_key VARCHAR(512) NULL COMMENT 'OSS 对象键',
  public_url VARCHAR(1024) NULL COMMENT '公开访问地址',
  mime_type VARCHAR(120) NULL COMMENT '媒体 MIME 类型',
  description VARCHAR(600) NULL COMMENT '素材说明',
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '素材状态：ACTIVE可用，DELETED已删除',
  created_by BIGINT NULL COMMENT '创建管理员 ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0正常，1已删除',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  KEY idx_web_asset_type_status (asset_type, status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='官网展示素材';

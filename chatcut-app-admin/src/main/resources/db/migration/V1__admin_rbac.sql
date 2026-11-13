-- ============================================================
-- ChatCut 管理端 RBAC 初始化
--   * 表结构：sys_admin / sys_role / sys_permission / sys_admin_role / sys_role_permission
--   * 种子数据：14 条权限（3 个菜单 + 11 个接口）、1 个超级管理员角色、1 个管理员账号
--   * 执行方式：项目未接 Flyway 自动迁移，由人工执行
--       mysql -h<host> -u<user> -p chatcut_admin < V1__admin_rbac.sql
--   * 可重复执行：建表用 IF NOT EXISTS，种子数据按固定 ID 先删后插
--
-- 固定 ID 规划（便于人工排查，全部 18 位）：
--   800000000000000001 ~ 0009      一级菜单
--   80000000000000001x             管理员模块子权限
--   80000000000000002x             角色模块子权限
--   80000000000000003x             权限模块子权限
--   900000000000000001             内置角色 super_admin
--   900000000000000101             内置管理员 admin
-- 权限 key 必须与 controller 上 @SaCheckPermission 的值逐字一致。
-- ============================================================

CREATE TABLE IF NOT EXISTS sys_admin (
  id BIGINT NOT NULL COMMENT '管理员主键，雪花 ID' PRIMARY KEY,
  username VARCHAR(64) NOT NULL COMMENT '登录用户名，全局唯一',
  password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码哈希，不保存明文',
  display_name VARCHAR(64) NOT NULL COMMENT '管理员显示名称',
  phone VARCHAR(32) NULL COMMENT '联系电话，可为空',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：1启用，0停用',
  last_login_at DATETIME NULL COMMENT '最近一次成功登录时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常，1已删除',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  UNIQUE KEY uk_sys_admin_username (username),
  KEY idx_sys_admin_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理端管理员账号';

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT NOT NULL COMMENT '角色主键，雪花 ID' PRIMARY KEY,
  role_key VARCHAR(64) NOT NULL COMMENT '角色唯一标识，用于程序识别',
  role_name VARCHAR(64) NOT NULL COMMENT '角色显示名称',
  description VARCHAR(255) NULL COMMENT '角色用途说明',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '角色状态：1启用，0停用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常，1已删除',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  UNIQUE KEY uk_sys_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理端角色';

CREATE TABLE IF NOT EXISTS sys_permission (
  id BIGINT NOT NULL COMMENT '权限主键，雪花 ID' PRIMARY KEY,
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父级权限 ID；顶级权限为0',
  permission_key VARCHAR(128) NOT NULL COMMENT '权限标识；需与 @SaCheckPermission 中的值一致',
  permission_name VARCHAR(100) NOT NULL COMMENT '权限显示名称',
  permission_type VARCHAR(16) NOT NULL COMMENT '权限类型：MENU菜单，BUTTON按钮，API接口',
  path VARCHAR(255) NULL COMMENT '前端路由或后端接口路径',
  http_method VARCHAR(16) NULL COMMENT '接口 HTTP 方法；非 API 权限为空',
  sort INT NOT NULL DEFAULT 0 COMMENT '同级显示顺序，值越小越靠前',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '权限状态：1启用，0停用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常，1已删除',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  UNIQUE KEY uk_sys_permission_key (permission_key),
  KEY idx_sys_permission_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理端菜单、按钮和接口权限';

CREATE TABLE IF NOT EXISTS sys_admin_role (
  admin_id BIGINT NOT NULL COMMENT '管理员 ID，对应 sys_admin.id',
  role_id BIGINT NOT NULL COMMENT '角色 ID，对应 sys_role.id',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
  PRIMARY KEY (admin_id, role_id),
  KEY idx_sys_admin_role_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员与角色关联；一个管理员可拥有多个角色';

CREATE TABLE IF NOT EXISTS sys_role_permission (
  role_id BIGINT NOT NULL COMMENT '角色 ID，对应 sys_role.id',
  permission_id BIGINT NOT NULL COMMENT '权限 ID，对应 sys_permission.id',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
  PRIMARY KEY (role_id, permission_id),
  KEY idx_sys_role_permission_permission (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色与权限关联；一个角色可拥有多个权限';

-- ------------------------------------------------------------
-- 清理旧的种子数据（按固定 ID 删除，保证脚本可重复执行）
-- 只重置内置角色/账号及其授权，自建角色与账号不受影响。
-- 若库里已有别人手工调整过的内置账号密码，重跑会把它重置回 chat@@cut。
-- ------------------------------------------------------------
DELETE FROM sys_admin_role WHERE admin_id = 900000000000000101;
DELETE FROM sys_role_permission
 WHERE role_id = 900000000000000001
    OR permission_id BETWEEN 800000000000000001 AND 800000000000000099;
DELETE FROM sys_admin WHERE id = 900000000000000101;
DELETE FROM sys_role WHERE id = 900000000000000001;
DELETE FROM sys_permission WHERE id BETWEEN 800000000000000001 AND 800000000000000099;
-- 早期脚本里的粗粒度权限 key，改名后清掉，避免权限树里残留无效项
DELETE FROM sys_permission
 WHERE permission_key IN ('system:admin:manage', 'system:role:manage', 'system:permission:read');

-- ------------------------------------------------------------
-- 权限：3 个菜单（MENU，供前端渲染导航）+ 11 个接口（API，供后端鉴权）
-- 层级：菜单为父节点，接口挂在对应菜单下
-- ------------------------------------------------------------
INSERT INTO sys_permission(id,parent_id,permission_key,permission_name,permission_type,path,http_method,sort,status,deleted,version) VALUES
-- 管理员管理
(800000000000000001,0,'system:admin:menu','管理员管理','MENU','/system/admins',NULL,10,1,0,0),
(800000000000000011,800000000000000001,'system:admin:list','管理员列表','API','/system/admins','GET',11,1,0,0),
(800000000000000012,800000000000000001,'system:admin:detail','管理员详情','API','/system/admins/{id}','GET',12,1,0,0),
(800000000000000013,800000000000000001,'system:admin:create','创建管理员','API','/system/admins','POST',13,1,0,0),
(800000000000000014,800000000000000001,'system:admin:status','启用/停用管理员','API','/system/admins/{id}/status','POST',14,1,0,0),
-- 角色管理
(800000000000000002,0,'system:role:menu','角色管理','MENU','/system/roles',NULL,20,1,0,0),
(800000000000000021,800000000000000002,'system:role:list','角色列表','API','/system/roles','GET',21,1,0,0),
(800000000000000022,800000000000000002,'system:role:detail','角色详情','API','/system/roles/{id}','GET',22,1,0,0),
(800000000000000023,800000000000000002,'system:role:permission:list','角色已授权限','API','/system/roles/{id}/permissions','GET',23,1,0,0),
(800000000000000024,800000000000000002,'system:role:create','创建角色','API','/system/roles','POST',24,1,0,0),
(800000000000000025,800000000000000002,'system:role:update','编辑角色','API','/system/roles/{id}','POST',25,1,0,0),
(800000000000000026,800000000000000002,'system:role:status','启用/停用角色','API','/system/roles/{id}/status','POST',26,1,0,0),
-- 权限管理
(800000000000000003,0,'system:permission:menu','权限管理','MENU','/system/permissions',NULL,30,1,0,0),
(800000000000000031,800000000000000003,'system:permission:list','权限项列表','API','/system/permissions','GET',31,1,0,0);

-- ------------------------------------------------------------
-- 内置角色：super_admin（中文名“超级管理员”）
-- 角色 key 不参与鉴权判断（鉴权只看 permission_key），保留给前端做按钮兜底展示
-- ------------------------------------------------------------
INSERT INTO sys_role(id,role_key,role_name,description,status,deleted,version) VALUES
(900000000000000001,'super_admin','超级管理员','内置角色，拥有全部权限；不可删除，慎用停用',1,0,0);

-- 超级管理员绑定全部内置权限（用区间选取，后续在本脚本里追加权限会自动带上）
INSERT INTO sys_role_permission(role_id,permission_id)
SELECT 900000000000000001, id
  FROM sys_permission
 WHERE id BETWEEN 800000000000000001 AND 800000000000000099
   AND deleted = 0;

-- ------------------------------------------------------------
-- 内置管理员：admin / chat@@cut
-- 密码为 BCrypt(strength=12) 哈希，明文 chat@@cut —— 仅限本地/联调环境。
-- 上线前必须：改密码（或直接停用本账号），并禁止把这套账号带到测试/生产库。
-- ------------------------------------------------------------
INSERT INTO sys_admin(id,username,password_hash,display_name,phone,status,last_login_at,deleted,version) VALUES
(900000000000000101,'admin','$2a$12$.KF6MpQW4bCSxO9QugLAS.UXspQcJZ9LJ8yTsvTd3/mohBLEIXzA6','超级管理员',NULL,1,NULL,0,0);

INSERT INTO sys_admin_role(admin_id,role_id) VALUES (900000000000000101,900000000000000001);

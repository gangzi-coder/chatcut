-- ============================================================================
-- 04_website_menu.sql —— 官网运营后台菜单与按钮权限初始化
--
-- 作用：
--   将 website 域（web_content / web_asset / crm_lead / crm_feedback /
--   web_site_setting / 操作日志）的全部 @SaCheckPermission 权限标识
--   初始化为 sys_menu 菜单（目录/菜单/按钮），并授权到 sys_role_menu。
--
-- 约定：
--   - menu_id 使用 2000 段（现有最大 menu_id 为 1623，不冲突）。
--   - 全部使用 INSERT IGNORE，可重复执行（幂等）；配合 00 脚本整库重建时直接生效。
--   - 超级管理员角色（role_id=1）在框架内放行所有权限，此处仍显式授权，
--     便于后续新增运营类角色时按菜单勾选复制。
--
-- 依赖：00_ruoyi_admin_init.sql（sys_menu / sys_role_menu 表）
-- ============================================================================

-- ----------------------------
-- 1. 顶级目录：官网管理
-- ----------------------------
INSERT IGNORE INTO sys_menu VALUES(2000, '官网管理', '0', '2', 'website', null, '', 1, 0, 'M', '0', '0', '', 'guide', 103, 1, NOW(), NULL, NULL, '官网运营后台目录');

-- ----------------------------
-- 2. 二级菜单（C，菜单级权限 = 列表/读取权限）
-- ----------------------------
INSERT IGNORE INTO sys_menu VALUES(2001, '工作概览',   '2000', '1', 'overview', 'website/overview/index', '', 1, 0, 'C', '0', '0', 'website:overview:list',  'dashboard', 103, 1, NOW(), NULL, NULL, '官网工作概览菜单');
INSERT IGNORE INTO sys_menu VALUES(2002, '内容管理',   '2000', '2', 'content',  'website/content/index',  '', 1, 0, 'C', '0', '0', 'website:content:list',   'documentation', 103, 1, NOW(), NULL, NULL, '官网内容管理菜单');
INSERT IGNORE INTO sys_menu VALUES(2003, '素材管理',   '2000', '3', 'asset',    'website/asset/index',    '', 1, 0, 'C', '0', '0', 'website:asset:list',     'upload', 103, 1, NOW(), NULL, NULL, '官网素材管理菜单');
INSERT IGNORE INTO sys_menu VALUES(2004, '线索管理',   '2000', '4', 'lead',     'website/lead/index',     '', 1, 0, 'C', '0', '0', 'website:lead:list',      'peoples', 103, 1, NOW(), NULL, NULL, '官网线索管理菜单');
INSERT IGNORE INTO sys_menu VALUES(2005, '反馈管理',   '2000', '5', 'feedback', 'website/feedback/index', '', 1, 0, 'C', '0', '0', 'website:feedback:list',  'message', 103, 1, NOW(), NULL, NULL, '官网反馈管理菜单');
INSERT IGNORE INTO sys_menu VALUES(2006, '站点设置',   '2000', '6', 'setting',  'website/setting/index',  '', 1, 0, 'C', '0', '0', 'website:settings:read',  'edit', 103, 1, NOW(), NULL, NULL, '官网站点设置菜单');
INSERT IGNORE INTO sys_menu VALUES(2007, '操作日志',   '2000', '7', 'log',      'website/log/index',      '', 1, 0, 'C', '0', '0', 'website:logs:list',      'log', 103, 1, NOW(), NULL, NULL, '官网操作日志菜单');

-- ----------------------------
-- 3. 按钮级权限（F，与 Controller @SaCheckPermission 一一对应）
-- ----------------------------
-- 内容管理 website:content:*
INSERT IGNORE INTO sys_menu VALUES(2010, '内容详情',   '2002', '1', '', '', '', 1, 0, 'F', '0', '0', 'website:content:detail',   '#', 103, 1, NOW(), NULL, NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2011, '内容保存',   '2002', '2', '', '', '', 1, 0, 'F', '0', '0', 'website:content:save',     '#', 103, 1, NOW(), NULL, NULL, '新建内容/保存草稿');
INSERT IGNORE INTO sys_menu VALUES(2012, '内容发布',   '2002', '3', '', '', '', 1, 0, 'F', '0', '0', 'website:content:publish',  '#', 103, 1, NOW(), NULL, NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2013, '内容下线',   '2002', '4', '', '', '', 1, 0, 'F', '0', '0', 'website:content:offline',  '#', 103, 1, NOW(), NULL, NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2014, '版本列表',   '2002', '5', '', '', '', 1, 0, 'F', '0', '0', 'website:content:versions', '#', 103, 1, NOW(), NULL, NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2015, '版本恢复',   '2002', '6', '', '', '', 1, 0, 'F', '0', '0', 'website:content:restore',  '#', 103, 1, NOW(), NULL, NULL, '');

-- 素材管理 website:asset:*
INSERT IGNORE INTO sys_menu VALUES(2020, '素材维护',   '2003', '1', '', '', '', 1, 0, 'F', '0', '0', 'website:asset:manage', '#', 103, 1, NOW(), NULL, NULL, '登记素材/删除素材');

-- 线索管理 website:lead:*
INSERT IGNORE INTO sys_menu VALUES(2030, '线索详情',   '2004', '1', '', '', '', 1, 0, 'F', '0', '0', 'website:lead:detail', '#', 103, 1, NOW(), NULL, NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2031, '线索更新',   '2004', '2', '', '', '', 1, 0, 'F', '0', '0', 'website:lead:update', '#', 103, 1, NOW(), NULL, NULL, '分配/状态/有效性/下次跟进/结果');
INSERT IGNORE INTO sys_menu VALUES(2032, '跟进记录',   '2004', '3', '', '', '', 1, 0, 'F', '0', '0', 'website:lead:note',   '#', 103, 1, NOW(), NULL, NULL, '添加内部跟进记录');
INSERT IGNORE INTO sys_menu VALUES(2033, '线索导出',   '2004', '4', '', '', '', 1, 0, 'F', '0', '0', 'website:lead:export', '#', 103, 1, NOW(), NULL, NULL, '按筛选同步导出明文 CSV');

-- 反馈管理 website:feedback:*
INSERT IGNORE INTO sys_menu VALUES(2040, '反馈详情',   '2005', '1', '', '', '', 1, 0, 'F', '0', '0', 'website:feedback:detail',  '#', 103, 1, NOW(), NULL, NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2041, '反馈更新',   '2005', '2', '', '', '', 1, 0, 'F', '0', '0', 'website:feedback:update',  '#', 103, 1, NOW(), NULL, NULL, '分配/状态/处理结果');
INSERT IGNORE INTO sys_menu VALUES(2042, '反馈转线索', '2005', '3', '', '', '', 1, 0, 'F', '0', '0', 'website:feedback:convert', '#', 103, 1, NOW(), NULL, NULL, '');

-- 站点设置 website:settings:*
INSERT IGNORE INTO sys_menu VALUES(2050, '设置更新',   '2006', '1', '', '', '', 1, 0, 'F', '0', '0', 'website:settings:update', '#', 103, 1, NOW(), NULL, NULL, '带 version 更新，实时生效');

-- ----------------------------
-- 4. 授权到角色（现有超管角色显式授权；后续新角色按需勾选）
-- ----------------------------
INSERT IGNORE INTO sys_role_menu(role_id, menu_id) VALUES
(1, 2000),
(1, 2001), (1, 2002), (1, 2003), (1, 2004), (1, 2005), (1, 2006), (1, 2007),
(1, 2010), (1, 2011), (1, 2012), (1, 2013), (1, 2014), (1, 2015),
(1, 2020),
(1, 2030), (1, 2031), (1, 2032), (1, 2033),
(1, 2040), (1, 2041), (1, 2042),
(1, 2050);

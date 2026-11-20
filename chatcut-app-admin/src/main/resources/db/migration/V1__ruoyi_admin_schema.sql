-- ============================================================================
-- ChatCut 管理端初始化脚本（MySQL 8.0+，UTF-8）
--
-- 基于 RuoYi-Vue-Plus 5.6.2 system/auth 原始表结构，仅保留单租户后台必需的表：
--   用户 / 角色 / 菜单 / 部门 / 岗位 / 客户端 / 操作日志 / 登录日志 / OSS
-- 不含：社会化登录、租户与套餐、字典、参数配置、通知公告、代码生成器、demo 表。
--
-- 使用方式：
--   1. 建库：CREATE DATABASE chatcut_admin_ruoyi DEFAULT CHARACTER SET utf8mb4;
--   2. 导入：mysql -uroot -p --default-character-set=utf8mb4 chatcut_admin_ruoyi < V1__ruoyi_admin_schema.sql
--
-- 注意：脚本会先删除同名表再重建（审计表除外，见下方说明），仅用于空库初始化或重建，
--       勿在有业务数据的库上执行。审计表为追加写，重复执行不会丢审计数据。
-- 初始账号：admin / admin123，首次登录后请修改密码。
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

drop table if exists sys_dept;
create table sys_dept (
    dept_id           bigint(20)      not null                   comment '部门id',
    parent_id         bigint(20)      default 0                  comment '父部门id',
    ancestors         varchar(500)    default ''                 comment '祖级列表',
    dept_name         varchar(30)     default ''                 comment '部门名称',
    dept_category     varchar(100)    default null               comment '部门类别编码',
    order_num         int(4)          default 0                  comment '显示顺序',
    leader            bigint(20)      default null               comment '负责人',
    phone             varchar(11)     default null               comment '联系电话',
    email             varchar(50)     default null               comment '邮箱',
    status            char(1)         default '0'                comment '部门状态（0正常 1停用）',
    del_flag          char(1)         default '0'                comment '删除标志（0代表存在 1代表删除）',
    create_dept       bigint(20)      default null               comment '创建部门',
    create_by         bigint(20)      default null               comment '创建者',
    create_time       datetime                                   comment '创建时间',
    update_by         bigint(20)      default null               comment '更新者',
    update_time       datetime                                   comment '更新时间',
    primary key (dept_id)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment = '部门表';
insert into sys_dept values(100, 0,   '0',          'ChatCut',   null,0, null, '', '', '0', '0', 103, 1, sysdate(), null, null);
insert into sys_dept values(103, 100, '0,100',  '管理部门',   null,1, 1, '', '', '0', '0', 103, 1, sysdate(), null, null);
drop table if exists sys_user;
create table sys_user (
    user_id           bigint(20)      not null                   comment '用户ID',
    dept_id           bigint(20)      default null               comment '部门ID',
    user_name         varchar(30)     not null                   comment '用户账号',
    nick_name         varchar(30)     not null                   comment '用户昵称',
    user_type         varchar(10)     default 'sys_user'         comment '用户类型（sys_user系统用户）',
    email             varchar(50)     default ''                 comment '用户邮箱',
    phonenumber       varchar(11)     default ''                 comment '手机号码',
    sex               char(1)         default '0'                comment '用户性别（0男 1女 2未知）',
    avatar            bigint(20)                                 comment '头像地址',
    password          varchar(100)    default ''                 comment '密码',
    status            char(1)         default '0'                comment '账号状态（0正常 1停用）',
    del_flag          char(1)         default '0'                comment '删除标志（0代表存在 1代表删除）',
    login_ip          varchar(128)    default ''                 comment '最后登录IP',
    login_date        datetime                                   comment '最后登录时间',
    create_dept       bigint(20)      default null               comment '创建部门',
    create_by         bigint(20)      default null               comment '创建者',
    create_time       datetime                                   comment '创建时间',
    update_by         bigint(20)      default null               comment '更新者',
    update_time       datetime                                   comment '更新时间',
    remark            varchar(500)    default null               comment '备注',
    primary key (user_id)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment = '用户信息表';
insert into sys_user values(1, 103, 'admin', '系统管理员', 'sys_user', '', '', '0', null, '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), 103, 1, sysdate(), null, null, '管理员');
drop table if exists sys_post;
create table sys_post
(
    post_id       bigint(20)      not null                   comment '岗位ID',
    dept_id       bigint(20)      not null                   comment '部门id',
    post_code     varchar(64)     not null                   comment '岗位编码',
    post_category varchar(100)    default null               comment '岗位类别编码',
    post_name     varchar(50)     not null                   comment '岗位名称',
    post_sort     int(4)          not null                   comment '显示顺序',
    status        char(1)         not null                   comment '状态（0正常 1停用）',
    create_dept   bigint(20)      default null               comment '创建部门',
    create_by     bigint(20)      default null               comment '创建者',
    create_time   datetime                                   comment '创建时间',
    update_by     bigint(20)      default null               comment '更新者',
    update_time   datetime                                   comment '更新时间',
    remark        varchar(500)    default null               comment '备注',
    primary key (post_id)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment = '岗位信息表';
insert into sys_post values(1, 103, 'ceo',  null, '董事长',    1, '0', 103, 1, sysdate(), null, null, '');
insert into sys_post values(2, 100, 'se',   null, '项目经理',  2, '0', 103, 1, sysdate(), null, null, '');
insert into sys_post values(3, 100, 'hr',   null, '人力资源',  3, '0', 103, 1, sysdate(), null, null, '');
insert into sys_post values(4, 100, 'user', null, '普通员工',  4, '0', 103, 1, sysdate(), null, null, '');
drop table if exists sys_role;
create table sys_role (
    role_id              bigint(20)      not null                   comment '角色ID',
    role_name            varchar(30)     not null                   comment '角色名称',
    role_key             varchar(100)    not null                   comment '角色权限字符串',
    role_sort            int(4)          not null                   comment '显示顺序',
    data_scope           char(1)         default '1'                comment '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限 5：仅本人数据权限 6：部门及以下或本人数据权限）',
    menu_check_strictly  tinyint(1)      default 1                  comment '菜单树选择项是否关联显示',
    dept_check_strictly  tinyint(1)      default 1                  comment '部门树选择项是否关联显示',
    status               char(1)         not null                   comment '角色状态（0正常 1停用）',
    del_flag             char(1)         default '0'                comment '删除标志（0代表存在 1代表删除）',
    create_dept          bigint(20)      default null               comment '创建部门',
    create_by            bigint(20)      default null               comment '创建者',
    create_time          datetime                                   comment '创建时间',
    update_by            bigint(20)      default null               comment '更新者',
    update_time          datetime                                   comment '更新时间',
    remark               varchar(500)    default null               comment '备注',
    primary key (role_id)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment = '角色信息表';
insert into sys_role values(1, '超级管理员',  'superadmin',  1, 1, 1, 1, '0', '0', 103, 1, sysdate(), null, null, '超级管理员');
drop table if exists sys_menu;
create table sys_menu (
    menu_id           bigint(20)      not null                   comment '菜单ID',
    menu_name         varchar(50)     not null                   comment '菜单名称',
    parent_id         bigint(20)      default 0                  comment '父菜单ID',
    order_num         int(4)          default 0                  comment '显示顺序',
    path              varchar(200)    default ''                 comment '路由地址',
    component         varchar(255)    default null               comment '组件路径',
    query_param       varchar(255)    default null               comment '路由参数',
    is_frame          int(1)          default 1                  comment '是否为外链（0是 1否）',
    is_cache          int(1)          default 0                  comment '是否缓存（0缓存 1不缓存）',
    menu_type         char(1)         default ''                 comment '菜单类型（M目录 C菜单 F按钮）',
    visible           char(1)         default 0                  comment '显示状态（0显示 1隐藏）',
    status            char(1)         default 0                  comment '菜单状态（0正常 1停用）',
    perms             varchar(100)    default null               comment '权限标识',
    icon              varchar(100)    default '#'                comment '菜单图标',
    create_dept       bigint(20)      default null               comment '创建部门',
    create_by         bigint(20)      default null               comment '创建者',
    create_time       datetime                                   comment '创建时间',
    update_by         bigint(20)      default null               comment '更新者',
    update_time       datetime                                   comment '更新时间',
    remark            varchar(500)    default ''                 comment '备注',
    primary key (menu_id)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment = '菜单权限表';
insert into sys_menu values('1', '系统管理', '0', '1', 'system',           null, '', 1, 0, 'M', '0', '0', '', 'system',   103, 1, sysdate(), null, null, '系统管理目录');
insert into sys_menu values('2', '系统监控', '0', '3', 'monitor',          null, '', 1, 0, 'M', '0', '0', '', 'monitor',  103, 1, sysdate(), null, null, '系统监控目录');
insert into sys_menu values('100',  '用户管理',     '1',   '1', 'user',             'system/user/index',            '', 1, 0, 'C', '0', '0', 'system:user:list',            'user',          103, 1, sysdate(), null, null, '用户管理菜单');
insert into sys_menu values('101',  '角色管理',     '1',   '2', 'role',             'system/role/index',            '', 1, 0, 'C', '0', '0', 'system:role:list',            'peoples',       103, 1, sysdate(), null, null, '角色管理菜单');
insert into sys_menu values('102',  '菜单管理',     '1',   '3', 'menu',             'system/menu/index',            '', 1, 0, 'C', '0', '0', 'system:menu:list',            'tree-table',    103, 1, sysdate(), null, null, '菜单管理菜单');
insert into sys_menu values('103',  '部门管理',     '1',   '4', 'dept',             'system/dept/index',            '', 1, 0, 'C', '0', '0', 'system:dept:list',            'tree',          103, 1, sysdate(), null, null, '部门管理菜单');
insert into sys_menu values('104',  '岗位管理',     '1',   '5', 'post',             'system/post/index',            '', 1, 0, 'C', '0', '0', 'system:post:list',            'post',          103, 1, sysdate(), null, null, '岗位管理菜单');
insert into sys_menu values('108',  '日志管理',     '1',   '9', 'log',              '',                             '', 1, 0, 'M', '0', '0', '',                            'log',           103, 1, sysdate(), null, null, '日志管理菜单');
insert into sys_menu values('109',  '在线用户',     '2',   '1', 'online',           'monitor/online/index',         '', 1, 0, 'C', '0', '0', 'monitor:online:list',         'online',        103, 1, sysdate(), null, null, '在线用户菜单');
insert into sys_menu values('113',  '缓存监控',     '2',   '5', 'cache',            'monitor/cache/index',          '', 1, 0, 'C', '0', '0', 'monitor:cache:list',          'redis',         103, 1, sysdate(), null, null, '缓存监控菜单');
insert into sys_menu values('123',  '客户端管理',   '1',   '11', 'client',           'system/client/index',          '', 1, 0, 'C', '0', '0', 'system:client:list',          'international', 103, 1, sysdate(), null, null, '客户端管理菜单');
insert into sys_menu values('130', '分配用户',     '1',   '2', 'role-auth/user/:roleId', 'system/role/authUser', '', 1, 1, 'C', '1', '0', 'system:role:edit',      '#',               103, 1, sysdate(), null, null, '/system/role');
insert into sys_menu values('131', '分配角色',     '1',   '1', 'user-auth/role/:userId', 'system/user/authRole', '', 1, 1, 'C', '1', '0', 'system:user:edit',      '#',               103, 1, sysdate(), null, null, '/system/user');
insert into sys_menu values('133', '文件配置管理',  '1',   '10', 'oss-config/index',              'system/oss/config', '', 1, 1, 'C', '1', '0', 'system:ossConfig:list',  '#',                103, 1, sysdate(), null, null, '/system/oss');
insert into sys_menu values('118',  '文件管理',     '1',   '10', 'oss',              'system/oss/index',            '', 1, 0, 'C', '0', '0', 'system:oss:list',              'upload',        103, 1, sysdate(), null, null, '文件管理菜单');
insert into sys_menu values('500',  '操作日志', '108', '1', 'operlog',    'monitor/operlog/index',    '', 1, 0, 'C', '0', '0', 'monitor:operlog:list',    'form',          103, 1, sysdate(), null, null, '操作日志菜单');
insert into sys_menu values('501',  '登录日志', '108', '2', 'logininfor', 'monitor/logininfor/index', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor',    103, 1, sysdate(), null, null, '登录日志菜单');
insert into sys_menu values('1001', '用户查询', '100', '1',  '', '', '', 1, 0, 'F', '0', '0', 'system:user:query',          '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1002', '用户新增', '100', '2',  '', '', '', 1, 0, 'F', '0', '0', 'system:user:add',            '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1003', '用户修改', '100', '3',  '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit',           '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1004', '用户删除', '100', '4',  '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove',         '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1005', '用户导出', '100', '5',  '', '', '', 1, 0, 'F', '0', '0', 'system:user:export',         '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1006', '用户导入', '100', '6',  '', '', '', 1, 0, 'F', '0', '0', 'system:user:import',         '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1007', '重置密码', '100', '7',  '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd',       '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1008', '角色查询', '101', '1',  '', '', '', 1, 0, 'F', '0', '0', 'system:role:query',          '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1009', '角色新增', '101', '2',  '', '', '', 1, 0, 'F', '0', '0', 'system:role:add',            '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1010', '角色修改', '101', '3',  '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit',           '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1011', '角色删除', '101', '4',  '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove',         '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1012', '角色导出', '101', '5',  '', '', '', 1, 0, 'F', '0', '0', 'system:role:export',         '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1013', '菜单查询', '102', '1',  '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query',          '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1014', '菜单新增', '102', '2',  '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add',            '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1015', '菜单修改', '102', '3',  '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit',           '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1016', '菜单删除', '102', '4',  '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove',         '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1017', '部门查询', '103', '1',  '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query',          '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1018', '部门新增', '103', '2',  '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add',            '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1019', '部门修改', '103', '3',  '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit',           '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1020', '部门删除', '103', '4',  '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove',         '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1021', '岗位查询', '104', '1',  '', '', '', 1, 0, 'F', '0', '0', 'system:post:query',          '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1022', '岗位新增', '104', '2',  '', '', '', 1, 0, 'F', '0', '0', 'system:post:add',            '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1023', '岗位修改', '104', '3',  '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit',           '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1024', '岗位删除', '104', '4',  '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove',         '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1025', '岗位导出', '104', '5',  '', '', '', 1, 0, 'F', '0', '0', 'system:post:export',         '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1040', '操作查询', '500', '1', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query',      '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1041', '操作删除', '500', '2', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove',     '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1042', '日志导出', '500', '4', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export',     '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1043', '登录查询', '501', '1', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query',   '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1044', '登录删除', '501', '2', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove',  '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1045', '日志导出', '501', '3', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export',  '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1050', '账户解锁', '501', '4', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock',  '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1046', '在线查询', '109', '1', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query',       '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1047', '批量强退', '109', '2', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1048', '单条强退', '109', '3', '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1600', '文件查询', '118', '1', '#', '', '', 1, 0, 'F', '0', '0', 'system:oss:query',        '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1601', '文件上传', '118', '2', '#', '', '', 1, 0, 'F', '0', '0', 'system:oss:upload',       '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1602', '文件下载', '118', '3', '#', '', '', 1, 0, 'F', '0', '0', 'system:oss:download',     '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1603', '文件删除', '118', '4', '#', '', '', 1, 0, 'F', '0', '0', 'system:oss:remove',       '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1620', '配置列表', '118', '5', '#', '', '', 1, 0, 'F', '0', '0', 'system:ossConfig:list',        '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1621', '配置添加', '118', '6', '#', '', '', 1, 0, 'F', '0', '0', 'system:ossConfig:add',         '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1622', '配置编辑', '118', '6', '#', '', '', 1, 0, 'F', '0', '0', 'system:ossConfig:edit',        '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1623', '配置删除', '118', '6', '#', '', '', 1, 0, 'F', '0', '0', 'system:ossConfig:remove',      '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1061', '客户端管理查询', '123', '1',  '#', '', '', 1, 0, 'F', '0', '0', 'system:client:query',        '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1062', '客户端管理新增', '123', '2',  '#', '', '', 1, 0, 'F', '0', '0', 'system:client:add',          '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1063', '客户端管理修改', '123', '3',  '#', '', '', 1, 0, 'F', '0', '0', 'system:client:edit',         '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1064', '客户端管理删除', '123', '4',  '#', '', '', 1, 0, 'F', '0', '0', 'system:client:remove',       '#', 103, 1, sysdate(), null, null, '');
insert into sys_menu values('1065', '客户端管理导出', '123', '5',  '#', '', '', 1, 0, 'F', '0', '0', 'system:client:export',       '#', 103, 1, sysdate(), null, null, '');
drop table if exists sys_user_role;
create table sys_user_role (
    user_id   bigint(20) not null comment '用户ID',
    role_id   bigint(20) not null comment '角色ID',
    primary key(user_id, role_id)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment = '用户和角色关联表';
insert into sys_user_role values ('1', '1');
drop table if exists sys_role_menu;
create table sys_role_menu (
    role_id   bigint(20) not null comment '角色ID',
    menu_id   bigint(20) not null comment '菜单ID',
    primary key(role_id, menu_id)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment = '角色和菜单关联表';
drop table if exists sys_role_dept;
create table sys_role_dept (
    role_id   bigint(20) not null comment '角色ID',
    dept_id   bigint(20) not null comment '部门ID',
    primary key(role_id, dept_id)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment = '角色和部门关联表';
drop table if exists sys_user_post;
create table sys_user_post
(
    user_id   bigint(20) not null comment '用户ID',
    post_id   bigint(20) not null comment '岗位ID',
    primary key (user_id, post_id)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment = '用户与岗位关联表';
insert into sys_user_post values ('1', '1');
drop table if exists sys_oper_log;
create table sys_oper_log (
    oper_id           bigint(20)      not null                   comment '日志主键',
    title             varchar(50)     default ''                 comment '模块标题',
    business_type     int(2)          default 0                  comment '业务类型（0其它 1新增 2修改 3删除）',
    method            varchar(100)    default ''                 comment '方法名称',
    request_method    varchar(10)     default ''                 comment '请求方式',
    operator_type     int(1)          default 0                  comment '操作类别（0其它 1后台用户 2手机端用户）',
    oper_name         varchar(50)     default ''                 comment '操作人员',
    dept_name         varchar(50)     default ''                 comment '部门名称',
    oper_url          varchar(255)    default ''                 comment '请求URL',
    oper_ip           varchar(128)    default ''                 comment '主机地址',
    oper_location     varchar(255)    default ''                 comment '操作地点',
    oper_param        varchar(4000)   default ''                 comment '请求参数',
    json_result       varchar(4000)   default ''                 comment '返回参数',
    status            int(1)          default 0                  comment '操作状态（0正常 1异常）',
    error_msg         varchar(4000)   default ''                 comment '错误消息',
    oper_time         datetime                                   comment '操作时间',
    cost_time         bigint(20)      default 0                  comment '消耗时间',
    primary key (oper_id),
    key idx_sys_oper_log_bt (business_type),
    key idx_sys_oper_log_s  (status),
    key idx_sys_oper_log_ot (oper_time)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment = '操作日志记录';
drop table if exists sys_logininfor;
create table sys_logininfor (
    info_id        bigint(20)     not null                  comment '访问ID',
    user_name      varchar(50)    default ''                comment '用户账号',
    client_key     varchar(32)    default ''                comment '客户端',
    device_type    varchar(32)    default ''                comment '设备类型',
    ipaddr         varchar(128)   default ''                comment '登录IP地址',
    login_location varchar(255)   default ''                comment '登录地点',
    browser        varchar(50)    default ''                comment '浏览器类型',
    os             varchar(50)    default ''                comment '操作系统',
    status         char(1)        default '0'               comment '登录状态（0成功 1失败）',
    msg            varchar(255)   default ''                comment '提示消息',
    login_time     datetime                                 comment '访问时间',
    primary key (info_id),
    key idx_sys_logininfor_s  (status),
    key idx_sys_logininfor_lt (login_time)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment = '系统访问记录';
drop table if exists sys_oss;
create table sys_oss (
    oss_id          bigint(20)   not null                   comment '对象存储主键',
    file_name       varchar(255) not null default ''        comment '文件名',
    original_name   varchar(255) not null default ''        comment '原名',
    file_suffix     varchar(10)  not null default ''        comment '文件后缀名',
    url             varchar(500) not null                   comment 'URL地址',
    ext1            text                  default null      comment '扩展字段',
    create_dept     bigint(20)            default null      comment '创建部门',
    create_time     datetime              default null      comment '创建时间',
    create_by       bigint(20)            default null      comment '上传人',
    update_time     datetime              default null      comment '更新时间',
    update_by       bigint(20)            default null      comment '更新人',
    service         varchar(20)  not null default 'minio'   comment '服务商',
    primary key (oss_id)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment ='OSS对象存储表';
drop table if exists sys_oss_config;
create table sys_oss_config (
    oss_config_id   bigint(20)    not null                  comment '主键',
    config_key      varchar(20)   not null  default ''      comment '配置key',
    access_key      varchar(255)            default ''      comment 'accessKey',
    secret_key      varchar(255)            default ''      comment '秘钥',
    bucket_name     varchar(255)            default ''      comment '桶名称',
    prefix          varchar(255)            default ''      comment '前缀',
    endpoint        varchar(255)            default ''      comment '访问站点',
    domain          varchar(255)            default ''      comment '自定义域名',
    is_https        char(1)                 default 'N'     comment '是否https（Y=是,N=否）',
    region          varchar(255)            default ''      comment '域',
    access_policy   char(1)       not null  default '1'     comment '桶权限类型(0=private 1=public 2=custom)',
    status          char(1)                 default '1'     comment '是否默认（0=是,1=否）',
    ext1            varchar(255)            default ''      comment '扩展字段',
    create_dept     bigint(20)              default null    comment '创建部门',
    create_by       bigint(20)              default null    comment '创建者',
    create_time     datetime                default null    comment '创建时间',
    update_by       bigint(20)              default null    comment '更新者',
    update_time     datetime                default null    comment '更新时间',
    remark          varchar(500)            default null    comment '备注',
    primary key (oss_config_id)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='对象存储配置表';
drop table if exists sys_client;
create table sys_client (
    id                  bigint(20)    not null            comment 'id',
    client_id           varchar(64)   default null        comment '客户端id',
    client_key          varchar(32)   default null        comment '客户端key',
    client_secret       varchar(255)  default null        comment '客户端秘钥',
    grant_type          varchar(255)  default null        comment '授权类型',
    device_type         varchar(32)   default null        comment '设备类型',
    active_timeout      int(11)       default 1800        comment 'token活跃超时时间',
    timeout             int(11)       default 604800      comment 'token固定超时',
    status              char(1)       default '0'         comment '状态（0正常 1停用）',
    del_flag            char(1)       default '0'         comment '删除标志（0代表存在 1代表删除）',
    create_dept         bigint(20)    default null        comment '创建部门',
    create_by           bigint(20)    default null        comment '创建者',
    create_time         datetime      default null        comment '创建时间',
    update_by           bigint(20)    default null        comment '更新者',
    update_time         datetime      default null        comment '更新时间',
    primary key (id)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='系统授权表';
insert into sys_client values (1, 'e5cd7e4891bf95d1d19206ce24a7b32e', 'pc', 'pc123', 'password', 'pc', 1800, 604800, 0, 0, 103, 1, sysdate(), 1, sysdate());
INSERT INTO sys_oss_config (oss_config_id,config_key,access_key,secret_key,bucket_name,prefix,endpoint,domain,is_https,region,access_policy,status,ext1,create_dept,create_by,create_time,remark) VALUES (1,'aliyun','','','','','','','N','','1','0','',103,1,NOW(),'填写实际对象存储参数后启用');

-- ============================================================================
-- ChatCut 管理端审计表（追加写、不做删除标记，重复执行不会丢数据：
--   建表用 IF NOT EXISTS，菜单与授权用 INSERT IGNORE）
-- ============================================================================

CREATE TABLE IF NOT EXISTS sys_audit_log (
  id BIGINT NOT NULL PRIMARY KEY, trace_id VARCHAR(32) NOT NULL, operator_id BIGINT NULL, operator_name VARCHAR(64) NULL,
  operator_ip VARCHAR(64) NOT NULL, operator_ua VARCHAR(255) NULL, caller VARCHAR(32) NOT NULL, module VARCHAR(32) NOT NULL,
  action VARCHAR(32) NOT NULL, action_name VARCHAR(64) NOT NULL, target_type VARCHAR(32) NULL, target_id VARCHAR(64) NULL,
  target_name VARCHAR(128) NULL, content VARCHAR(500) NOT NULL, before_data JSON NULL, after_data JSON NULL,
  changed_fields VARCHAR(500) NULL, detail JSON NULL, request_uri VARCHAR(255) NOT NULL, http_method VARCHAR(16) NOT NULL,
  request_data JSON NULL, result TINYINT NOT NULL, error_code INT NULL, error_msg VARCHAR(500) NULL, cost_ms INT NOT NULL,
  op_time DATETIME NOT NULL, KEY idx_audit_operator(operator_id,op_time), KEY idx_audit_module(module,op_time),
  KEY idx_audit_target(target_type,target_id), KEY idx_audit_trace(trace_id), KEY idx_audit_time(op_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='管理操作审计记录';

CREATE TABLE IF NOT EXISTS sys_audit_log_change (
  id BIGINT NOT NULL PRIMARY KEY, audit_id BIGINT NOT NULL, field_name VARCHAR(64) NOT NULL, field_label VARCHAR(64) NOT NULL,
  old_value TEXT NULL, new_value TEXT NULL, old_display VARCHAR(500) NULL, new_display VARCHAR(500) NULL,
  changed_type VARCHAR(16) NOT NULL, KEY idx_audit_change_audit(audit_id), KEY idx_audit_change_field(field_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='管理操作审计字段变更明细';

-- 给内置超级管理员授予审计接口权限（menu_id 99001/99002 仅用于按钮级权限标识）。
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query_param,is_frame,is_cache,menu_type,visible,status,perms,icon,create_dept,create_by,create_time,update_by,update_time,remark) VALUES
(99001,'审计日志查询',1,90,'#','','',1,0,'F','1','0','system:audit:list','#',103,1,NOW(),NULL,NULL,'审计日志列表权限'),
(99002,'审计日志详情',1,91,'#','','',1,0,'F','1','0','system:audit:detail','#',103,1,NOW(),NULL,NULL,'审计日志详情权限');

INSERT IGNORE INTO sys_role_menu(role_id,menu_id) VALUES(1,99001),(1,99002);

SET FOREIGN_KEY_CHECKS = 1;

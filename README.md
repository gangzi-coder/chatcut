# ChatCut 后端

五个服务 + ChatCut 公共组件。公共能力层以 **RuoYi-Vue-Plus 5.6.2** 为底座做源码 vendoring（模块与包名全部收敛为 `chatcut`），admin 承载若依后台 + ChatCut 审计；api、enterprise、portal 保持 ChatCut 自有业务与多账号体系。

## 服务总览

| 服务 | 端口 | 网关前缀 | 账号体系 | 职责 |
|---|---|---|---|---|
| `chatcut-gateway` | 9001 | — | — | 路由、token 粗校验、身份注入、traceId 起点、路由日志 |
| `chatcut-app-api` | 9201 | `/api/**` | `user` | C 端业务：认证、用户、剪辑、对话、文件、任务 |
| `chatcut-app-admin` | 9101 | `/admin/**` | 若依 `login` JWT | 后台管理：若依用户/角色/菜单权限、OSS、ChatCut 审计 |
| `chatcut-app-enterprise` | 9301 | `/enterprise/**` | `enterprise` | B 端业务：企业认证、组织/成员/项目协作 |
| `chatcut-app-portal` | 9401 | `/portal/**` | 免登录 | 官网门户：只读查询后台配置的展示数据 |

## 模块

```
chatcut
├── chatcut-dependencies            BOM：统一第三方版本
├── chatcut-common                  公共能力层（RuoYi-Vue-Plus 5.6.2 源码 vendoring）
│   ├── chatcut-common-bom              各公共模块版本 BOM
│   ├── chatcut-common-core             R / 全局异常 / 工具集；+ 多账号 StpLogic、内部签名、TraceContext
│   ├── chatcut-common-web              全局异常、XSS、重复读取过滤器；+ trace 过滤器、调用日志切面、注解鉴权拦截器
│   ├── chatcut-common-json             Jackson 序列化
│   ├── chatcut-common-redis            RedisUtils、Redisson、分布式锁（lock4j）
│   ├── chatcut-common-satoken          Sa-Token 配置 + PlusSaTokenDao + LoginHelper（若依单账号体系）
│   ├── chatcut-common-mybatis          BaseDO、分页/乐观锁/防全表更新、字段填充
│   ├── chatcut-common-translation      翻译注解（含 NoopDictService 兜底）
│   ├── chatcut-common-oss              对象存储（S3 协议）
│   ├── chatcut-common-log              @Log 操作日志写 sys_oper_log
│   ├── chatcut-common-excel            Excel 导入导出
│   ├── chatcut-common-tenant           租户插件（已裁剪 TenantEntity，仅引入不启用）
│   ├── chatcut-common-idempotent       幂等 @Idempotent
│   ├── chatcut-common-sensitive        字段脱敏
│   ├── chatcut-common-encrypt          数据库字段加解密
│   ├── chatcut-common-sse              SSE 推送
│   ├── chatcut-common-ratelimiter      限流
│   ├── chatcut-common-mail             邮件
│   └── chatcut-common-sms              短信（sms4j，已纳入构建，暂未启用）
├── chatcut-gateway                 9001 网关（WebFlux）
│   └── GatewaySaTokenConfig / SharedSaTokenDao    网关专用：sa-token 配置绑定 + 与 admin 兼容的会话 DAO
└── chatcut-app-{api,admin,enterprise,portal}      四个业务服务
```

vendored 模块的 Maven 坐标（`com.chatcut:chatcut-common-*:1.0.0-SNAPSHOT`）与 **Java 包名（`com.chatcut.common.*`）已全部统一为 chatcut**；
ChatCut 自有的胶水代码在同一包树下：`com.chatcut.common.security`（多账号 Stp/内部签名）、`com.chatcut.common.core.trace`、`com.chatcut.common.web.*`，
随 `chatcut-common-core` / `chatcut-common-web` 一起构建；网关专用类下沉在 `chatcut-gateway`。

业务服务包结构统一为三类：

```
com.chatcut.<service>
├── <Service>Application
├── config/       服务级装配：Swagger、PasswordEncoder 等（脱离业务域还成立的配置）
├── internal/     服务内部端点：/internal/health、/internal/whoami（不参与 RBAC、不写审计）
└── <域>/         业务域：annotation/controller/model/repository/service
                   repository/{entity,mapper} 建域时必须同步建空包占位（@MapperScan 通配检查包存在）
```

admin 额外遵循 `com.chatcut.admin.audit` 的包规范：`repository/`（entity+mapper）、实体类名 = 表名驼峰 + `Entity`、
`model.req` / `model.resp`（类名以 `Req` / `Resp` 结尾，`@Data` class 禁 record）、对外查询禁止返回 Entity。

## 请求链路

```
客户端 → 网关 AuthWebFilter（-100）
         白名单放行：/api/auth/** /enterprise/auth/** /portal/**（官网全免登录）/internal/health…
         否则解析 Bearer token，按路径选体系：/admin/→若依 login JWT、/enterprise/→enterprise、其余→user
         剥离并按 HMAC 重写身份头（三套互斥）：X-User-Id / X-Enterprise-Id / X-Admin-Id + X-Internal-Sign
         生成/复用 traceId 写 X-Trace-Id、写 X-Caller
       → 业务服务
         InternalSignFilter 校验签名（密钥为空跳过，仅限本地）
         SaTokenWebConfig 按 chatcut.security.type 注册 ChatCut 拦截器（user / enterprise / ruoyi / none）
         细粒度权限：ChatCut 接口用对应体系；admin（type=ruoyi）走若依 @SaCheckPermission（login 体系）
```

- 三套登录态共用同一个 Redis；JWT 密钥 `sa-token.jwt-secret-key` 按环境在 Nacos 配置，网关与 admin 必须一致。
- 未登录/token 失效 → `401 + code 10004`；已登录缺权限 → `403 + code 10002`（`SaTokenExceptionHandler`，不得落进全局兜底）。
- 业务服务取身份：controller 层调 `CurrentUser.userId()` / `enterpriseId()` / `adminId()`，往下传参。

## 快速开始

前置：JDK 21（17 亦可）、Maven 3.9+、MySQL 8、Redis 7、Nacos 3.x。

> 根 pom 已把 lombok 固定到 `1.18.48`（旧版在 JDK 21.0.12 上 mvn 编译报 `TypeTag :: UNKNOWN`；IDE 编译不受影响）。

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn -DskipTests package

# 启动（业务服务先注册，网关最后）
java -jar chatcut-app-admin/target/chatcut-app-admin-1.0.0-SNAPSHOT.jar
java -jar chatcut-app-api/target/chatcut-app-api-1.0.0-SNAPSHOT.jar
java -jar chatcut-app-enterprise/target/chatcut-app-enterprise-1.0.0-SNAPSHOT.jar
java -jar chatcut-app-portal/target/chatcut-app-portal-1.0.0-SNAPSHOT.jar
java -jar chatcut-gateway/target/chatcut-gateway-1.0.0-SNAPSHOT.jar

# 冒烟
curl http://127.0.0.1:9401/internal/health            # 直连 portal
curl http://127.0.0.1:9001/portal/internal/health     # 经网关（免登录）
curl http://127.0.0.1:9001/enterprise/internal/health # 经网关（白名单）
curl http://127.0.0.1:9001/actuator/health            # 网关自身
```

### 本地基础设施（Docker）

| 容器 | 说明 |
|---|---|
| `mysql8` | MySQL 8，root/root |
| `redis-test` | Redis 7，**带密码 `cc-dev-123`**（容器启动命令 `--requirepass`，重启不丢） |
| `nacos-standalone` | Nacos 3.3.0，控制台 `8080`、OpenAPI `8848`、gRPC `9848`，账号 `nacos/nacos` |

Redis 密码已写死在 Nacos `chatcut-common-{profile}.yaml` 的 `spring.data.redis.password`，
**启动服务不需要任何环境变量**；换环境时在对应 dataId 里改即可。

### 管理端初始化

新建空的 `chatcut_admin_ruoyi` 数据库后执行唯一初始化脚本（若依 16 张系统表 + ChatCut 审计 2 张表，可重复执行）：

```bash
mysql --default-character-set=utf8mb4 -h<host> -u<user> -p chatcut_admin_ruoyi \
  < chatcut-app-admin/src/main/resources/db/migration/V1__ruoyi_admin_schema.sql
```

schema 包含初始管理员 **admin / admin123**、角色/菜单和 `sys_client` 初始配置
（`client_id = e5cd7e4891bf95d1d19206ce24a7b32e`）。首次登录后请立即修改初始密码，
并在 Nacos `chatcut-common-{profile}.yaml` 设置 `sa-token.jwt-secret-key`。

登录请求体用若依字段 `clientId`、`grantType=password`、`username`、`password`；
本地 `captcha.enable=false` 可不带验证码，开启后需先调 `/auth/code` 拿 `uuid` + `code`。
请求头 `clientid`（全小写）必须与 body 的 `clientId` 一致。

```bash
curl -X POST http://127.0.0.1:9101/auth/login \
  -H 'Content-Type: application/json' \
  -H 'clientid: e5cd7e4891bf95d1d19206ce24a7b32e' \
  -d '{"clientId":"e5cd7e4891bf95d1d19206ce24a7b32e","grantType":"password","username":"admin","password":"<密码>"}'
```

### 文档入口（swagger + knife4j，四个业务服务同配置）

| 服务 | 直连 | 经网关 |
|---|---|---|
| api | `http://127.0.0.1:9201/doc.html` | `/api/doc.html` |
| admin | `http://127.0.0.1:9101/doc.html` | `/admin/doc.html` |
| enterprise | `http://127.0.0.1:9301/doc.html` | `/enterprise/doc.html` |
| portal | `http://127.0.0.1:9401/doc.html` | `/portal/doc.html` |

Swagger UI `/swagger-ui/index.html`、spec `/v3/api-docs` 并存。鉴权服务先调 `/auth/login`，
再点 Authorize 填 tokenValue；portal 免登录。测试/生产通过 Nacos 关 `springdoc.api-docs.enabled`。

> **springdoc 必须锁 2.3.x**：knife4j 4.5.0 是对 2.3.0 编译的，升到 2.6 会
> `NoSuchMethodError: SpringDocConfigProperties.getGroupConfigs()`，表现是 `/v3/api-docs`
> 返回 200 但 body 是 `{"code":10999}`、文档页空白。

## 配置

| 层 | 内容 | 位置 |
|---|---|---|
| 服务私有静态配置 | 端口、Nacos 接入信息、安全体系（`chatcut.security.type`） | jar 内 `application.yml` |
| 公共配置 | Redis、公共 MyBatis、Sa-Token 与共享 JWT 密钥、management、日志底座 | Nacos：`chatcut-common-{profile}.yaml` |
| 服务私有配置 | 数据源、若依登录/系统参数、网关路由日志与 gateway 端点 | Nacos：`chatcut-{服务}-{profile}.yaml` |

jar 内只保留服务端口、服务名、Nacos 引导连接和固定安全类型；数据源、Redis、JWT 等环境项均从 Nacos 加载。
`optional:` 导入缺失时启动会因拿不到数据源直接失败——故意的 fail-fast。样例在 `nacos-config/`，按 profile 改名上传。
portal 例外：不依赖 sa-token 拦截器，全部接口只读、匿名可访问。

### Nacos（v3.3.0）

**一个环境一个命名空间，隔离靠命名空间不靠 dataId 后缀**：

| 环境 | 命名空间 ID | `SPRING_PROFILES_ACTIVE` |
|---|---|---|
| 本地 `chatcut-local` | `1898c274-9625-4117-ba85-e3757117deeb` | `local` |
| 测试 / 生产 | 建好命名空间后填进 `application.yml` 的 `nacos.namespaces.*` | `dev` / `prod` |

> **坑 1**：命名空间参数要的是 **ID**（UUID），不是名称。填名字能启动但拉不到任何配置。
> **坑 2**：Nacos 3.x 默认开鉴权，客户端不配 `NACOS_USERNAME` / `NACOS_PASSWORD`（默认 `nacos`/`nacos`）会 403。
> **坑 3**：Nacos 3.3.0 已下线 v1/v2 配置写接口；脚本化读写走
> `GET /nacos/v3/client/cs/config`、`POST /nacos/v3/admin/cs/config`（表单 + `serverIdentity: security` 头）。

每组命名空间固定 6 个 dataId（YAML）：

| dataId | 内容 | 谁加载 |
|---|---|---|
| `chatcut-common-{profile}.yaml` | Redis（含密码）、公共底座 | 全部业务服务 + 网关 |
| `chatcut-gateway-{profile}.yaml` | 路由日志开关、gateway 端点 | gateway |
| `chatcut-app-api-{profile}.yaml` | 数据源（`chatcut` 库） | app-api |
| `chatcut-app-admin-{profile}.yaml` | 数据源（`chatcut_admin_ruoyi`）+ 若依系统配置 | app-admin |
| `chatcut-app-enterprise-{profile}.yaml` | 数据源（`chatcut_enterprise`） | app-enterprise |
| `chatcut-app-portal-{profile}.yaml` | 数据源（连 `chatcut_admin`，**生产用只读账号**） | app-portal |

地址/命名空间由 profile 推导（`nacos.endpoints.${profile}` / `nacos.namespaces.${profile}`），
打包一次跑所有环境，切环境只改 `SPRING_PROFILES_ACTIVE`。

## 可观测

### 日志链路（traceId 全链路贯通）

- **网关** `AccessLogWebFilter`（最外层 GlobalFilter）：`【网关路由-请求】-【路由】-【目标服务】-【请求IP】-【地址】-【方法】`、
  `【网关路由-响应】-【目标实例 ip:port】-【状态码】-【用时】`、异常 `【网关路由-异常】`；4xx/5xx 升 WARN。
  生成/复用 traceId，是链路起点；不读 body（流式）。开关与排除路径在网关私有配置 `chatcut.log.gateway.*`。
- **业务服务** `InvocationLogAspect`（chatcut-common-web）：controller 层自动切入，service 层用 `@LogInvocation` 标记。
  三段式：`【服务入口-请求】`、`【服务入口-入参】`、`【服务入口-响应】-【用时】-【响应数据】`、异常 `【服务入口-异常】`。
  不截断；敏感字段按 `chatcut.log.invocation.sensitive-fields` 黑名单脱敏为 `***`；慢接口升 WARN。
  已知限制：同类自调用、final 方法不生效；路径参数绑定失败发生在进 controller 前，切面拦不到。
- **查链路**：网关日志与业务日志 traceId 一致，可按 traceId 串起「网关转给谁 → 落到哪个实例 → 处理了多久」。
  审计表也存 trace_id，接口支持 `GET /system/audits?traceId=xxx` 反查。注意：一个 traceId 只对应一个请求。

### 审计留痕（admin）

后台写操作在 controller 方法上加 `@AuditLog(AuditAction.X)`，落：

- `sys_audit_log`：谁（operator/IP/UA/caller）/何时/对谁/做了什么/结果（成功、业务拒绝、系统异常都留痕）/耗时/traceId
- `sys_audit_log_change`：字段级旧值→新值（含旧/新显示值），集合增删走 `detail` 清单

要点：切面只采操作者、来源、入参、结果、耗时；**对象、摘要、前后快照由 service 用 `AuditContext` 补**
（`target` / `content` / `captureBefore` / `captureAfter` / `collectionChange`）。快照字段由实体
`@AuditField` **白名单**决定（密码哈希等不加注解即永不入内），入参按字段名黑名单脱敏。
写入 `REQUIRES_NEW` 独立事务：**业务回滚也留痕**；审计失败只降级 ERROR 日志，不阻断业务。
表只 INSERT，无改删；查询走 `system:audit:list` / `system:audit:detail`。
已知边界：401/403 在拦截器被拦的请求不留痕（未进 controller）；查询类 GET 不记。

## 工程约定

### 接口契约

| 事项 | 规则 |
|---|---|
| 响应 | 统一若依 **`R<T>{code,msg,data}`**，`code=200` 成功；分页用 **`TableDataInfo<T>`** |
| 异常 | 业务失败抛 `ServiceException`（HTTP 200 + 对应 code）；框架级故障 = HTTP 500 + `10999`；鉴权失败见「请求链路」 |
| 请求方式 | 只允许 GET（查询）和 POST（写入，含创建/更新/启停/删除），禁止 PUT/PATCH/DELETE |
| Req/Resp | 类名以 **Req**（`model.req`）/ **Resp**（`model.resp`）结尾，Lombok `@Data` class，**禁止 record**；对外查询一律返回 Resp，禁止返回 Entity |
| Swagger | Req/Resp 加 `@Schema`（类+字段），查询参数加 `@Parameter`，接口加 `@Operation` |
| 分页 | 上限 200，超过用游标 |

### 数据访问

| 事项 | 规则 |
|---|---|
| 分层目录 | 数据层 `repository/`（entity + mapper），实体类名 = 表名驼峰 + `Entity`（`sys_admin` → `SysAdminEntity`）；类和每个字段必须有 javadoc，状态字段用 `@see` 标取值枚举 |
| Entity 边界 | Entity 不出 service 层 |
| 枚举 | 枚举类必须有类注释，常量与字段逐个写 javadoc；固定取值必须用枚举，**禁止魔法值** |

### 并发与事务

| 事项 | 规则 |
|---|---|
| 事务 | 只加在 service 写方法上，`rollbackFor = Exception.class` |
| 并发 | 多节点写操作先套分布式锁（lock4j / `RedisUtils`），service 内保持 `@Transactional`；DB 唯一索引兜底 |
| 幂等 | 写接口 `@Idempotent` |
| 会话 | Sa-Token 登录态存 Redis（gateway 与若依使用兼容 DAO），禁止退化成内存实现 |

### 安全

| 事项 | 规则 |
|---|---|
| 账号体系 | api/enterprise 使用 ChatCut `user`/`enterprise`；admin 使用若依默认 `login` JWT，网关只在 `/admin/**` 校验此 token 并透传签名身份头 |
| 白名单 | 只写具体路径（`/auth/login`），**禁止 `/auth/**` 前缀**——会把 me/logout 一起放行且身份为 null；新增免登录接口必须显式补一行。网关与服务两层白名单都要改 |
| 日志 | 必带 traceId；禁止打印 token、手机号明文、AK/SK |

### 代码风格

- `if/else/for/while` **一律带大括号**，禁止单行省略
- SQL 里状态不写死，用 `#{status}` 传枚举 code

## 下一步（按顺序）

1. ~~admin 若依后端集成 + ChatCut 审计~~ 已完成（脚本位于 `chatcut-app-admin/src/main/resources/db/migration/V1__ruoyi_admin_schema.sql`）
2. 认证闭环：api `auth` 域短信登录 + `StpUserUtil.login()`
3. 任务闭环：api `task` 域 `biz_task` 状态机
4. 文件闭环：api `file` 域 STS 下发 + complete 校验
5. C 端建表 SQL 人工执行，新表追加到对应初始化脚本（`CREATE TABLE IF NOT EXISTS` + 固定 ID 先删后插，可重复执行）
6. enterprise / portal 业务：B 端 `chatcut_enterprise` 建表（本地库已建好）；官网 `content` 表由 admin 写入、portal 只读，注意过滤未发布状态

## 明确不做

- 服务间 RPC
- Seata 分布式事务（单库事务 + 最终一致）
- WebSocket（前端轮询任务状态）

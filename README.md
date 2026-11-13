# ChatCut 后端工程骨架

三个服务、一套自建底座，不含任何建表脚本（表结构等接口定稿后再补 `db/sql/`）。

## 模块

```
chatcut
├── chatcut-dependencies         BOM：统一第三方版本
├── chatcut-common
│   ├── common-core              统一响应 / 错误码 / 异常 / traceId
│   ├── common-web               全局异常、trace 过滤器、Jackson、CORS
│   ├── common-mybatis           BaseDO、分页/乐观锁/防全表更新、字段填充
│   ├── common-redis             Key 规范、分布式锁、@RateLimit、@Idempotent
│   ├── common-mq                消息外壳、生产者、幂等消费基类
│   ├── common-oss               OSS 客户端、签名 URL
│   ├── common-security-core     Sa-Token 双账号体系 + 内部签名（网关共用）
│   └── common-security-web      注解鉴权拦截器、内部签名校验过滤器
├── chatcut-gateway              8083 路由 + token 粗校验 + 身份注入
├── chatcut-app-api              8081 C 端业务
└── chatcut-app-admin            8082 管理端
```

## 本地启动

前置：JDK 17 或 21、Maven 3.9+、MySQL 8、Redis 7、RocketMQ 5、Nacos 3.x
（本机为 Nacos 3.3.0 Docker：控制台 `8080`、OpenAPI `8848`、gRPC `9848`）。

> 必须用 JDK 17/21 跑 Maven。本机若 `mvn -v` 显示的是 JDK 24，Lombok 会报
> `Fatal error compiling: com.sun.tools.javac.code.TypeTag :: UNKNOWN`，
> 先 `export JAVA_HOME=$(/usr/libexec/java_home -v 21)` 再编译。

```bash
# 0. 指定 JDK（装在 /Library/Java 或 sdkman 的机器上按实际路径改）
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# 1. 编译
mvn -q -DskipTests package

# 2. 加载本地环境变量（Nacos 地址/命名空间、MySQL、Redis 都在里面）
source scripts/env-local.sh
# 不 source 也能跑：application-local.yml 提供了中间件兜底值（profile=local），
# 但那只是"连不上 Nacos 也能启动"，正常开发请走 Nacos。

# 3. 依次启动（顺序：api/admin 先注册，网关最后）
java -jar chatcut-app-api/target/chatcut-app-api-1.0.0-SNAPSHOT.jar
java -jar chatcut-app-admin/target/chatcut-app-admin-1.0.0-SNAPSHOT.jar
java -jar chatcut-gateway/target/chatcut-gateway-1.0.0-SNAPSHOT.jar

# 4. 冒烟
curl http://127.0.0.1:8081/internal/health     # 直连 api
curl http://127.0.0.1:8083/api/internal/health # 经网关（需登录，白名单仅 /api/auth/**）
curl http://127.0.0.1:8083/actuator/health
```

> 管理端表结构与初始数据在 `chatcut-app-admin/src/main/resources/db/migration/V1__admin_rbac.sql`，
> 人工执行（未接 Flyway 自动迁移），可重复执行：
> ```bash
> mysql -h<host> -u<user> -p chatcut_admin < chatcut-app-admin/src/main/resources/db/migration/V1__admin_rbac.sql
> ```
> 脚本会建 5 张表 + 14 条权限（3 菜单 + 11 接口）+ 角色 `super_admin` + 账号 **admin / chat@@cut**（拥有全部权限）。
> 该账号仅限本地联调，上线前必须改密或停用。
>
> 冒烟登录：
> ```bash
> curl -X POST http://127.0.0.1:8082/auth/login \
>   -H 'Content-Type: application/json' -d '{"username":"admin","password":"chat@@cut"}'
> ```

### 配置分层

| 层 | 内容 | 位置 |
|---|---|---|
| 静态配置 | 端口、mybatis-plus、sa-token、springdoc、日志 | jar 内 `application.yml` |
| 本地兜底 | 数据源 / Redis / MQ 的 127.0.0.1 默认值 | jar 内 `application-local.yml`（仅 profile=local） |
| 环境配置 | 数据源、Redis、RocketMQ（test/prod 真实地址） | **Nacos**：`chatcut-common-{profile}.yaml` + 各服务 `{dataId}` |

Nacos 样例在 `nacos-config/` 目录，按 profile 改名上传（如 `chatcut-common-test.yaml`）。jar 内不放中间件配置，`optional:` 导入缺失时启动会因拿不到数据源直接失败——这是故意的 fail-fast。

### MQ 开关

`chatcut.mq.enabled`（代码缺省 `true`，只有显式 `false` 才关），放在 Nacos 的各服务 dataId 里，按环境给值。

关闭后不是"假装发了"式地降级，而是整块自动装配被排除：

| 关掉之后 | 效果 |
|---|---|
| `RocketMQAutoConfiguration` | 不装配：不创建 `DefaultMQProducer` / `RocketMQTemplate`，不连 NameServer |
| `@RocketMQMessageListener` | 不注册，没有消费者线程 |
| `MessagePublisher` | 不装配（`@ConditionalOnProperty`）→ 依赖它的代码启动即失败 |
| 启动日志 | 打一条 WARN：`chatcut.mq.enabled=false，MQ 已关闭……` |

实现方式：`common-mq` 里的 `MqSwitchEnvironmentPostProcessor` 在自动装配条件评估之前，按开关值把
`RocketMQAutoConfiguration` 写进 `spring.autoconfigure.exclude`。之所以不用 yml 里写死 `spring.autoconfigure.exclude`，
是因为那份配置是静态的，做不到"按属性值决定是否排除"。

### Nacos 配置（v3.3.0）

**① 命名空间：一个环境一个，隔离靠命名空间不靠 dataId 后缀**

| 环境 | 命名空间名称 | 命名空间 ID | `SPRING_PROFILES_ACTIVE` |
|---|---|---|---|
| 本地 | `chatcut-local` | `1898c274-9625-4117-ba85-e3757117deeb` | `local` |
| 测试 | `chatcut-dev` | 建后填这里 | `dev` |
| 生产 | `chatcut-prod` | 建后填这里 | `prod` |

> **坑点**：命名空间参数要的是 **ID**（那串 UUID），不是名称 `chatcut-local`。
> 填名字能启动，但拉不到任何配置，报错还很不直观。

**② 分组与 dataId：DEFAULT_GROUP + 4 个 dataId**

命名空间已经隔离了环境，组内不再分 group。每个命名空间固定 4 个 dataId（YAML 格式）：

| dataId | 内容 | 谁加载 |
|---|---|---|
| `chatcut-common-{profile}.yaml` | Redis（会话共享） | 三个服务 |
| `chatcut-gateway-{profile}.yaml` | 网关私有开关，目前留空 | gateway |
| `chatcut-app-api-{profile}.yaml` | 数据源 + MQ | app-api |
| `chatcut-app-admin-{profile}.yaml` | 数据源（`chatcut_admin` 独立库）+ MQ | app-admin |

对应 jar 内 `application.yml` 的 `spring.config.import`：

```yaml
spring:
  config:
    import:
      - optional:nacos:chatcut-common-${spring.profiles.active}.yaml
      - optional:nacos:chatcut-app-admin-${spring.profiles.active}.yaml
```

**③ 地址与命名空间由 profile 推导（不靠环境变量）**

三个服务的 `application.yml` 末尾都有一段环境清单，`server-addr` / `namespace` 用嵌套占位符
按 `spring.profiles.active` 取值，**打包一次即可跑所有环境**：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: ${nacos.server-addr}
        namespace: ${nacos.namespace}
        group: ${nacos.group}
      config:
        server-addr: ${nacos.server-addr}
        namespace: ${nacos.namespace}
        group: ${nacos.group}
        file-extension: yaml

nacos:
  group: DEFAULT_GROUP
  username: ${NACOS_USERNAME:nacos}
  password: ${NACOS_PASSWORD:nacos}
  server-addr: ${nacos.endpoints.${spring.profiles.active}}
  namespace: ${nacos.namespaces.${spring.profiles.active}}
  endpoints:
    local: 127.0.0.1:8848
    dev: nacos-dev.chatcut.internal:8848
    prod: nacos.chatcut.internal:8848
  namespaces:
    local: 1898c274-9625-4117-ba85-e3757117deeb
    dev: <建好后填 ID>
    prod: <建好后填 ID>
```

切换环境只改 `SPRING_PROFILES_ACTIVE`（或 `--spring.profiles.active=dev`），不碰任何地址。

> **profile 名不规则时**（如 dev1~dev10 共用一套集群），改用多文档分组，
> 注意 Spring Boot 3 的写法是 `spring.config.activate.on-profile`，
> 老版本的 `spring.profiles` 在文档块里已不生效：
>
> ```yaml
> ---
> spring:
>   config:
>     activate:
>       on-profile: dev1,dev2,dev3,dev4,dev5
> nacos:
>   server-addr: ${nacos.endpoints.dev}
>   namespace: ${nacos.namespaces.dev}
> ```

| 变量 | 默认值 | 说明 |
|---|---|---|
| `NACOS_USERNAME` / `NACOS_PASSWORD` | `nacos` / `nacos` | **Nacos 3.x 默认开鉴权，不配会 403** |

**④ 控制台端口冲突**

Nacos 3.x 控制台默认监听 `8080`，早期版本与本工程网关撞车。现在网关已挪到 **8083**，
两者可以共存：控制台继续用 `http://localhost:8080`，网关 `http://localhost:8083`。
如果还想把控制台也挪开，改 `nacos-docker/example/standalone-derby.yaml` 的
`"8080:8080"` 为 `"8088:8080"` 后 `docker compose up -d` 即可（数据在 volume 里不会丢）。

**⑤ 建完怎么验**

```bash
source scripts/env-local.sh
java -jar chatcut-app-admin/target/chatcut-app-admin-1.0.0-SNAPSHOT.jar
# 日志出现下面两行即说明 Nacos → Spring 的链路通了：
#   [Nacos Config] Load config[dataId=chatcut-common-local.yaml, group=DEFAULT_GROUP] success
#   Sa-Token 会话存储：SaTokenDaoRedisJackson（Redis 持久化，多节点共享）
```

## 约定速查

| 事项 | 规则 |
|---|---|
| 响应 | `Result<T>{code,msg,data,traceId}`，`code=0` 成功 |
| HTTP 状态码 | 业务失败 = **HTTP 200 + 非 0 code**（按 code 分支）；框架级故障（`SystemException` / 未知异常兜底）= **HTTP 500 + 10999**，网关与监控按状态码即可发现服务异常；鉴权失败见下「鉴权失败」行 |
| 错误码 | 10xxx 通用 / 20xxx 认证 / 30xxx 用户 / 40xxx 剪辑 / 50xxx AI / 60xxx 文件 / 70xxx 任务 / 80xxx 管理端 |
| 契约 | 公共枚举放 `common-core` 的 `enums` 包；仅单服务使用的放该服务内；枚举值只增不改 |
| 身份 | 网关剥离并按 HMAC 重写 `X-User-Id` / `X-Admin-Id`，业务服务用 `InternalSignFilter` 校验 |
| 取当前用户 | controller 层调 `CurrentUser.userId()`，往下传参，不要在深层到处取 |
| 事务 | 只加在 service 写方法上，`rollbackFor = Exception.class` |
| 并发 | 多节点的写操作在 **controller 层入口**先套 `DistributedLock`（粒度按业务键：username/id/roleKey），service 内保持 `@Transactional`——跨 bean 调用保证事务在锁内开启，禁止 service 里再拿锁；DB 唯一索引兜底 |
| Entity 边界 | Entity（原 DO）不出 service 层，controller 只收 Resp |
| 分层目录 | 数据层 `repository/`（entity + mapper），实体类名 = **表名驼峰 + Entity**（`sys_admin` → `SysAdminEntity`），不允许与表名不一致，**类和每个字段必须有 javadoc 注释**，状态类字段用 `@see` 标明取值枚举（如 `@see EnableStatus`）；`model/` 只放 `req/`（入参）和 `resp/`（出参）；对外查询一律返回 Resp，禁止返回 Entity |
| 枚举 | 枚举类必须有类注释，常量与字段逐个写 javadoc 说明含义与用途 |
| Req/Resp | 请求参数类名以 **Req** 结尾（`model.req`），返回类名以 **Resp** 结尾（`model.resp`）；一律用 Lombok `@Data` class，**禁止使用 Java record** |
| 请求方式 | 只允许 **GET（查询）和 POST（写入，含创建/更新/启停/删除）**，禁止 PUT/PATCH/DELETE；CORS 同步只放行 GET/POST/OPTIONS |
| Swagger | 对外 Req/Resp 必须加 `@Schema`（类 + 每个字段），查询参数加 `@Parameter`，接口加 `@Operation`；新增接口文档必须可直接联调 |
| 文档入口 | 增强页 knife4j `/doc.html`（经网关 `/admin/doc.html`、`/api/doc.html`）与官方 `/swagger-ui/index.html` 并存，spec 统一在 `/v3/api-docs`。**springdoc 必须锁在 2.3.x**：knife4j 4.5.0 是对 2.3.0 编译的，升到 2.6 会 `NoSuchMethodError: SpringDocConfigProperties.getGroupConfigs()`，表现是 `/v3/api-docs` 返回 200 但 body 是 `{"code":10999}`、文档页空白 |
| 代码风格 | `if/else/for/while` **一律带大括号**（哪怕只有一行），禁止单行省略；状态、类型等固定取值必须用枚举（如 `EnableStatus`），**禁止魔法值**（0/1、`"admin"` 等）；SQL 里也不写死，用 `#{status}` 传枚举 code |
| 分页 | 上限 200，超过用游标 |
| 缓存 Key | 必须走 `RedisKeys.of(...)`，禁止手写拼接 |
| 会话 | Sa-Token 登录态存 **Redis**（`SaTokenDaoRedisJackson`，由 `SaTokenStorageConfig` 注册进 `SaManager`）；三服务共用同一 Redis 实例与 database，禁止退化成内存 `SaTokenDaoDefaultImpl` |
| 鉴权失败 | 未登录/token 失效 → **HTTP 401 + code 10004**；已登录但缺权限 → **HTTP 403 + code 10002**。由 `common-security-web` 的 `SaTokenExceptionHandler` 处理（优先级最高），**不得落进 `GlobalExceptionHandler` 的 `Exception` 兜底**（那会打成 ERROR 堆栈并回 10999，前端无法跳登录）。网关 `AuthWebFilter` 返回同一套状态码与业务码 |
| 白名单 | 只写具体路径（`/auth/login`），**禁止 `/auth/**` 这种前缀**——会把 `/auth/me`、`/auth/logout` 一起放行，未登录也能进且身份为 null；新增免登录接口必须显式加一行 |
| 双账号体系 | 服务用哪套 StpLogic 由 `chatcut.security.type` 决定（api=user，**admin 必须配 admin**）；漏配会让 admin 拿 C 端体系校验 token，Redis 里明明有 token 却一律判"token 无效"→ 所有管理端接口 401。同理 `@SaCheckPermission` 必须写 `type = "admin"`，注解默认走的是 login 体系 |
| 幂等 | 写接口 `@Idempotent`；MQ 消费 `message_id` + DB 唯一键 |
| 日志 | 必带 traceId；禁止打印 token、手机号明文、AK/SK |

## 下一步（按顺序）

1. ~~admin 端接 RBAC~~ 已完成：`V1__admin_rbac.sql` 建表 + 14 条权限 + `super_admin` + `admin/chat@@cut`，接口按细粒度 key 鉴权（`system:admin:list` / `system:role:update` 等）；
2. 认证闭环：`auth` 包下实现短信登录 + `StpUserUtil.login()`，配 `application-local.yml`；
3. 任务闭环：`task` 包下实现 `biz_task` 状态机与 `chatcut-task-dispatch` 消费；
4. 文件闭环：`file` 包下 STS 下发 + `complete` 校验；
5. C 端表结构：`db_auth` / `db_task` 建表 SQL 按 `V2__`、`V3__` 顺序编号往后加，不改已执行过的脚本。

## 明确不做

- 服务间 RPC（用 MQ）
- Seata 分布式事务（单库事务 + 最终一致）
- WebSocket / SSE（前端轮询任务状态）

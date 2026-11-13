package com.chatcut.common.redis.constant;

/**
 * Redis Key 规范：cc:{env}:{biz}:{id}[:{field}]
 * 禁止在业务代码里手写字符串拼接，统一走这里，便于排查与批量清理。
 */
public final class RedisKeys {

    private static volatile String env = "local";

    private RedisKeys() {
    }

    public static void initEnv(String value) {
        env = value;
    }

    public static String of(String biz, Object... parts) {
        StringBuilder sb = new StringBuilder("cc:").append(env).append(':').append(biz);
        for (Object p : parts) {
            sb.append(':').append(p);
        }
        return sb.toString();
    }

    public static String lock(String biz, Object id) {
        return of("lock", biz, id);
    }

    public static String rateLimit(String name, String identity) {
        return of("rl", name, identity);
    }

    public static String idempotent(String name, String digest) {
        return of("idem", name, digest);
    }
}

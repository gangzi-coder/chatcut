#!/usr/bin/env bash
# ============================================================
# chatcut 本地开发环境变量（source 后启动任意服务即可连上 Nacos）
#
#   source scripts/env-local.sh
#   mvn -pl chatcut-app-admin spring-boot:run
#
# Nacos 的地址与命名空间不在这里配：由 profile 从 application.yml 的
# nacos.endpoints / nacos.namespaces 里推导（profile=local → 本地 Nacos）。
# 这个脚本只管「跟环境无关的凭据」和「中间件地址」。
# ============================================================

# ---------- Nacos 凭据（地址/命名空间由 profile 决定） ----------
export NACOS_USERNAME=${NACOS_USERNAME:-nacos}
export NACOS_PASSWORD=${NACOS_PASSWORD:-nacos}

# ---------- Profile：改这里就等于切环境（唯一的切环境入口） ----------
# Nacos 地址/命名空间、Redis key 前缀都由它推导，不再有 CHATCUT_ENV 这类重复变量
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-local}

# ---------- MySQL（远程开发库） ----------
export MYSQL_HOST=${MYSQL_HOST:-10.0.43.167}
export MYSQL_PORT=${MYSQL_PORT:-3306}
export MYSQL_USER=${MYSQL_USER:-root}
export MYSQL_PASSWORD=${MYSQL_PASSWORD:-root}

# ---------- Redis（本机 docker） ----------
export REDIS_HOST=${REDIS_HOST:-127.0.0.1}
export REDIS_PORT=${REDIS_PORT:-6379}
export REDIS_PASSWORD=${REDIS_PASSWORD:-}

# ---------- RocketMQ（未启动时保留占位，消费端起来前不影响启动） ----------
export ROCKETMQ_NAMESRV=${ROCKETMQ_NAMESRV:-127.0.0.1:9876}

# ---------- 服务间内部签名（本地留空即跳过校验） ----------
export INTERNAL_SECRET=${INTERNAL_SECRET:-}

echo "[chatcut] Nacos      : 由 profile 推导（profile=local → 127.0.0.1:8848）"
echo "[chatcut] MySQL      : ${MYSQL_HOST}:${MYSQL_PORT}"
echo "[chatcut] Redis      : ${REDIS_HOST}:${REDIS_PORT}"
echo "[chatcut] Profile    : ${SPRING_PROFILES_ACTIVE}"

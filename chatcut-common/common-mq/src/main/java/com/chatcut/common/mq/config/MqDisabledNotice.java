package com.chatcut.common.mq.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * MQ 关闭时的启动提示。
 *
 * <p>排除自动装配是"静默"的：日志里既没有生产者初始化，也没有报错，
 * 排查"消息为什么没发出去"时容易走弯路，所以在这里主动打一条 WARN 留痕。</p>
 *
 * <p>真正的关闭动作在 {@link MqSwitchEnvironmentPostProcessor}，
 * 本类只负责提示，不做任何装配。</p>
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "chatcut.mq", name = "enabled", havingValue = "false")
public class MqDisabledNotice {

    @PostConstruct
    public void notice() {
        log.warn("chatcut.mq.enabled=false，MQ 已关闭：不创建 RocketMQ 客户端、不注册消费者、不发送消息");
    }
}

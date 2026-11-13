package com.chatcut.common.mq.producer;

import com.chatcut.common.core.exception.SystemException;
import com.chatcut.common.mq.message.BaseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 生产者统一出口：日志 + 失败即抛，调用方要么在事务提交后调用，要么交给补偿任务重投。
 * 注意：不要在这里吞异常——吞掉的后果是任务永远卡在 CREATED。
 *
 * <p>是否装配由 {@code chatcut.mq.enabled} 决定（缺省启用）：关闭时本 Bean 不注册，
 * 需要发消息的代码会在启动阶段直接报缺少依赖，而不是运行期静默丢消息。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "chatcut.mq", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MessagePublisher {

    private final RocketMQTemplate rocketMQTemplate;

    public void send(String topic, String tag, BaseMessage<?> message) {
        String destination = (tag == null || tag.isBlank()) ? topic : topic + ":" + tag;
        try {
            SendResult result = rocketMQTemplate.syncSend(destination, message);
            if (result.getSendStatus() != SendStatus.SEND_OK) {
                log.error("mq send not ok, topic={} status={} msgId={}", destination, result.getSendStatus(), message.getMsgId());
                throw new SystemException("消息发送失败: " + result.getSendStatus());
            }
            log.info("mq sent topic={} msgId={}", destination, message.getMsgId());
        } catch (SystemException e) {
            throw e;
        } catch (Exception e) {
            log.error("mq send failed, topic={} msgId={}", destination, message.getMsgId(), e);
            throw new SystemException("消息发送失败", e);
        }
    }
}

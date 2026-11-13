package com.chatcut.common.mq.message;

import com.chatcut.common.core.trace.TraceContext;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/**
 * 消息统一外壳：所有 Topic 的消息体都包这一层，
 * msgId 用于幂等与追踪，traceId 用于串联全链路日志。
 */
@Getter
@AllArgsConstructor
@Schema(description = "MQ 消息统一外壳")
public class BaseMessage<T> {

    @Schema(description = "消息唯一 id，消费方用于幂等去重")
    private final String msgId;

    @Schema(description = "全链路追踪 id，与产生该消息的请求一致")
    private final String traceId;

    @Schema(description = "消息产生时间（毫秒时间戳）")
    private final long ts;

    @Schema(description = "业务载荷")
    private final T payload;

    public static <T> BaseMessage<T> of(long msgId, T payload) {
        return new BaseMessage<>(String.valueOf(msgId), TraceContext.get(), Instant.now().toEpochMilli(), payload);
    }
}

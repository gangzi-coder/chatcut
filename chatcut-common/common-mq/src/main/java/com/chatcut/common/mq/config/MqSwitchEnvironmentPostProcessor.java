package com.chatcut.common.mq.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * MQ 总开关：{@code chatcut.mq.enabled=false} 时排除 RocketMQ 的自动装配。
 *
 * <p>为什么不用 yml 里写 {@code spring.autoconfigure.exclude}：那份配置是静态的，
 * 做不到"按某个属性的值决定是否排除"。{@link EnvironmentPostProcessor} 在自动装配条件
 * 评估之前执行，此时 yml 与 Nacos 的配置都已加载完成，可以按开关值动态写入排除项。</p>
 *
 * <p>关闭后的效果：不再创建 {@code DefaultMQProducer} / {@code RocketMQTemplate}，
 * 不再注册 {@code @RocketMQMessageListener}，因此也不会去连 NameServer；
 * 同时 {@code MessagePublisher} 不装配，依赖它的代码启动即失败，避免"以为发了其实没发"。</p>
 *
 * @see com.chatcut.common.mq.producer.MessagePublisher
 * @see MqDisabledNotice
 */
public class MqSwitchEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    /** 开关属性名，缺省视为启用。 */
    public static final String ENABLED_PROPERTY = "chatcut.mq.enabled";

    private static final String EXCLUDE_PROPERTY = "spring.autoconfigure.exclude";

    private static final String ROCKETMQ_AUTO_CONFIGURATION =
            "org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration";

    private static final String PROPERTY_SOURCE_NAME = "chatcutMqSwitch";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        boolean enabled = environment.getProperty(ENABLED_PROPERTY, Boolean.class, Boolean.TRUE);
        if (enabled) {
            return;
        }
        // 保留调用方已经声明的排除项，避免覆盖
        Set<String> excludes = new LinkedHashSet<>();
        String existing = environment.getProperty(EXCLUDE_PROPERTY);
        if (StringUtils.hasText(existing)) {
            Arrays.stream(existing.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .forEach(excludes::add);
        }
        excludes.add(ROCKETMQ_AUTO_CONFIGURATION);
        environment.getPropertySources().addFirst(
                new MapPropertySource(PROPERTY_SOURCE_NAME, Map.of(EXCLUDE_PROPERTY, String.join(",", excludes))));
        // 这里不打日志：日志系统此时还没初始化，提示统一由 MqDisabledNotice 在启动后输出
    }

    @Override
    public int getOrder() {
        // 必须晚于 ConfigDataEnvironmentPostProcessor，否则读不到 yml / Nacos 里的开关值
        return Ordered.LOWEST_PRECEDENCE;
    }
}

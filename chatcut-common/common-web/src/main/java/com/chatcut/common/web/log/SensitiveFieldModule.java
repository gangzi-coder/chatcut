package com.chatcut.common.web.log;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;

import java.util.List;
import java.util.Set;

/**
 * 按字段名黑名单脱敏的 Jackson 模块：命中黑名单的 Bean 属性一律输出 "***"。
 *
 * <p>注意两点：
 * 1) 只覆盖 JavaBean 属性（含 record），Map 的 key 不经过 BeanSerializerModifier，覆盖不到；
 * 2) 字段名比较统一转小写，password / PASSWORD / Password 都会命中。</p>
 */
public class SensitiveFieldModule extends SimpleModule {

    private static final JsonSerializer<Object> MASK = new JsonSerializer<>() {
        @Override
        public void serialize(Object value, JsonGenerator gen, com.fasterxml.jackson.databind.SerializerProvider serializers)
                throws java.io.IOException {
            gen.writeString("***");
        }
    };

    public SensitiveFieldModule(Set<String> lowerCaseFields) {
        super("chatcut-sensitive-fields");
        setSerializerModifier(new MaskingModifier(lowerCaseFields));
    }

    private static class MaskingModifier extends BeanSerializerModifier {
        private final Set<String> fields;

        MaskingModifier(Set<String> fields) {
            this.fields = fields;
        }

        @Override
        public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                         BeanDescription beanDesc,
                                                         List<BeanPropertyWriter> beanProperties) {
            for (BeanPropertyWriter w : beanProperties) {
                if (fields.contains(w.getName().toLowerCase())) {
                    w.assignSerializer(MASK);
                }
            }
            return beanProperties;
        }
    }
}

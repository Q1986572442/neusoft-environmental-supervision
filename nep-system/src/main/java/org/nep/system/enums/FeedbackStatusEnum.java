package org.nep.system.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 监督反馈状态枚举（MyBatis-Plus @EnumValue 映射到 TINYINT）
 * <p>
 * DB 存储: 0-待指派(PENDING), 1-已指派(ASSIGNED), 2-已完成(COMPLETED)
 * JSON 序列化: 使用 name() 输出字符串 "PENDING"/"ASSIGNED"/"COMPLETED"，保持前端兼容
 */
public enum FeedbackStatusEnum {

    PENDING(0, "待指派"),
    ASSIGNED(1, "已指派"),
    COMPLETED(2, "已完成");

    @EnumValue
    private final Integer code;

    private final String label;

    FeedbackStatusEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    public Integer getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 根据 DB code 获取枚举
     */
    public static FeedbackStatusEnum fromCode(Integer code) {
        if (code == null) return null;
        for (FeedbackStatusEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }

    /**
     * Jackson 反序列化：兼容字符串名称("PENDING")和数字code(0)
     */
    @JsonCreator
    public static FeedbackStatusEnum fromValue(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return fromCode((Integer) value);
        if (value instanceof String s) {
            // 尝试按枚举名称匹配
            try { return valueOf(s); } catch (IllegalArgumentException ignored) {}
            // 尝试按数字 code 解析
            try { return fromCode(Integer.parseInt(s)); } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}

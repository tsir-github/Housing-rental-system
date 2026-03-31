package com.atguigu.lease.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 创建者类型枚举
 */
public enum CreatorType implements BaseEnum {

    SYSTEM(0, "系统"),
    OFFICIAL(1, "官方"),
    LANDLORD(2, "房东");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    CreatorType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * 根据code值获取枚举
     * 支持JSON反序列化
     */
    @JsonCreator
    public static CreatorType fromCode(Object value) {
        if (value == null) {
            return null;
        }
        
        Integer code = null;
        if (value instanceof Integer) {
            code = (Integer) value;
        } else if (value instanceof String) {
            try {
                code = Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid CreatorType code: " + value);
            }
        } else {
            throw new IllegalArgumentException("Invalid CreatorType code type: " + value.getClass());
        }
        
        for (CreatorType type : CreatorType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid CreatorType code: " + code);
    }
}
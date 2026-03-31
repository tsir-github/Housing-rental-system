package com.atguigu.lease.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum LeaseSourceType implements BaseEnum {

    NEW(1, "新签"),
    RENEW(2, "续约"),
    DIRECT_APPLICATION(3, "直接入住申请");

    @JsonValue
    @EnumValue
    private Integer code;

    private String name;

    LeaseSourceType(Integer code, String name) {
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
    public static LeaseSourceType fromCode(Object value) {
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
                throw new IllegalArgumentException("Invalid LeaseSourceType code: " + value);
            }
        } else {
            throw new IllegalArgumentException("Invalid LeaseSourceType code type: " + value.getClass());
        }
        
        for (LeaseSourceType type : LeaseSourceType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid LeaseSourceType code: " + code);
    }
}

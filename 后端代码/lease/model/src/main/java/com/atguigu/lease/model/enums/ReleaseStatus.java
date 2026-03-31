package com.atguigu.lease.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReleaseStatus implements BaseEnum {

    RELEASED(1, "已发布"),
    NOT_RELEASED(0, "未发布");


    @EnumValue
    @JsonValue
    private Integer code;

    private String name;


    ReleaseStatus(Integer code, String name) {
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
     * 修复JSON反序列化问题：直接接收数字值而不是JSON对象
     */
    @JsonCreator
    public static ReleaseStatus fromCode(Object value) {
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
                throw new IllegalArgumentException("Invalid ReleaseStatus code: " + value);
            }
        } else {
            throw new IllegalArgumentException("Invalid ReleaseStatus code type: " + value.getClass());
        }
        
        for (ReleaseStatus status : ReleaseStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid ReleaseStatus code: " + code);
    }

    /**
     * 根据字符串code值获取枚举
     */
    public static ReleaseStatus fromString(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return fromCode(Integer.parseInt(code));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ReleaseStatus code: " + code);
        }
    }
}

package com.atguigu.lease.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DirectMoveInStatus implements BaseEnum {

    PENDING(1, "待处理"),
    APPROVED(2, "已确认"),
    REJECTED(3, "已拒绝");

    @EnumValue
    @JsonValue
    private Integer code;

    private String name;

    DirectMoveInStatus(Integer code, String name) {
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
    public static DirectMoveInStatus fromCode(Object value) {
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
                throw new IllegalArgumentException("Invalid DirectMoveInStatus code: " + value);
            }
        } else {
            throw new IllegalArgumentException("Invalid DirectMoveInStatus code type: " + value.getClass());
        }
        
        for (DirectMoveInStatus status : DirectMoveInStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid DirectMoveInStatus code: " + code);
    }

    /**
     * 根据字符串code值获取枚举
     */
    public static DirectMoveInStatus fromString(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return fromCode(Integer.parseInt(code));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid DirectMoveInStatus code: " + code);
        }
    }
}
package com.atguigu.lease.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReviewStatus implements BaseEnum {

    PENDING(1, "待审核"),
    APPROVED(2, "审核通过"),
    REJECTED(3, "审核拒绝");

    @EnumValue
    @JsonValue
    private Integer code;

    private String name;

    ReviewStatus(Integer code, String name) {
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
    public static ReviewStatus fromCode(Object value) {
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
                throw new IllegalArgumentException("Invalid ReviewStatus code: " + value);
            }
        } else {
            throw new IllegalArgumentException("Invalid ReviewStatus code type: " + value.getClass());
        }
        
        for (ReviewStatus status : ReviewStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid ReviewStatus code: " + code);
    }

    /**
     * 根据字符串code值获取枚举
     */
    public static ReviewStatus fromString(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return fromCode(Integer.parseInt(code));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ReviewStatus code: " + code);
        }
    }
}
package com.atguigu.lease.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LeaseStatus implements BaseEnum {

    SIGNING(1, "签约待确认"),
    SIGNED(2, "已签约"),
    CANCELED(3, "已取消"),
    EXPIRED(4, "已到期"),
    WITHDRAWING(5, "退租待确认"),
    WITHDRAWN(6, "已退租"),
    RENEWING(7, "续约待确认");

    @EnumValue
    @JsonValue
    private Integer code;

    private String name;

    LeaseStatus(Integer code, String name) {
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
    public static LeaseStatus fromCode(Object value) {
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
                throw new IllegalArgumentException("Invalid LeaseStatus code: " + value);
            }
        } else {
            throw new IllegalArgumentException("Invalid LeaseStatus code type: " + value.getClass());
        }
        
        for (LeaseStatus status : LeaseStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid LeaseStatus code: " + code);
    }

    /**
     * 根据字符串code值获取枚举
     */
    public static LeaseStatus fromString(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return fromCode(Integer.parseInt(code));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid LeaseStatus code: " + code);
        }
    }
}

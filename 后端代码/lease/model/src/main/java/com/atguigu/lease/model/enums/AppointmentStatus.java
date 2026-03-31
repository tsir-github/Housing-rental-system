package com.atguigu.lease.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AppointmentStatus implements BaseEnum {


    WAITING(1, "待看房"),

    CANCELED(2, "已取消"),

    VIEWED(3, "已看房");


    @EnumValue
    @JsonValue
    private Integer code;


    private String name;

    AppointmentStatus(Integer code, String name) {
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
     * 根据code获取枚举值
     */
    public static AppointmentStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AppointmentStatus status : AppointmentStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的预约状态代码: " + code);
    }

    /**
     * 根据字符串code值获取枚举
     */
    public static AppointmentStatus fromString(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return fromCode(Integer.parseInt(code));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid AppointmentStatus code: " + code);
        }
    }
}

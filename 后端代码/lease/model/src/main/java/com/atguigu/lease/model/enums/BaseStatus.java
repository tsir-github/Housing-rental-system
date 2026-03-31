package com.atguigu.lease.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;


public enum BaseStatus implements BaseEnum {


    ENABLE(1, "正常"),

    DISABLE(0, "禁用");


    @EnumValue
    @JsonValue
    private Integer code;

    private String name;

    BaseStatus(Integer code, String name) {
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
     */
    public static BaseStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (BaseStatus status : BaseStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid BaseStatus code: " + code);
    }

    /**
     * 根据字符串code值获取枚举
     */
    public static BaseStatus fromString(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return fromCode(Integer.parseInt(code));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid BaseStatus code: " + code);
        }
    }
}

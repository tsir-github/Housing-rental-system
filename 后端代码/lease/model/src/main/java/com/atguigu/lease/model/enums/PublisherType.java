package com.atguigu.lease.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PublisherType implements BaseEnum {

    OFFICIAL(1, "官方"),
    LANDLORD(2, "房东");

    @EnumValue
    @JsonValue
    private Integer code;

    private String name;

    PublisherType(Integer code, String name) {
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
}
package com.atguigu.lease.web.app.converter;

import com.atguigu.lease.model.enums.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

/**
 * 通用枚举转换器工厂
 * 自动为所有实现BaseEnum接口的枚举类型提供字符串到枚举的转换功能
 */
@Component
public class StringToBaseEnumConverterFactory implements ConverterFactory<String, BaseEnum> {

    @Override
    public <T extends BaseEnum> Converter<String, T> getConverter(Class<T> targetType) {
        return new Converter<String, T>() {
            @Override
            public T convert(String code) {
                if (code == null || code.trim().isEmpty()) {
                    return null;
                }
                
                T[] enumConstants = targetType.getEnumConstants();
                for (T enumConstant : enumConstants) {
                    if (enumConstant.getCode().equals(Integer.valueOf(code))) {
                        return enumConstant;
                    }
                }
                throw new IllegalArgumentException("无效的枚举代码：" + code + "，枚举类型：" + targetType.getSimpleName());
            }
        };
    }
}
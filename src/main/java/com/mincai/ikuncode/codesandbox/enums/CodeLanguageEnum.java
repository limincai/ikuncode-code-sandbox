package com.mincai.ikuncode.codesandbox.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

/**
 * 代码语言枚举类
 *
 * @author limincai
 */
@Getter
public enum CodeLanguageEnum {

    JAVA("java");

    private final String value;

    CodeLanguageEnum(String value) {
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static CodeLanguageEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (CodeLanguageEnum anEnum : CodeLanguageEnum.values()) {
            if (anEnum.getValue().equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}

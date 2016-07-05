package org.light4j.sso.server.uic.system.account.entity;

import lombok.Getter;

/**
 * Created by longjiazuo on 2014/11/30.
 */
public enum AccountTypeEnum {

    USER(0, "user"),
    APP(1, "app");

    AccountTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    @Getter
    private int value;
    @Getter
    private String name;

    public static String getNameByValue(int value) {
        for (AccountTypeEnum type : values()) {
            if (type.value == value) {
                return type.name;
            }
        }
        return null;
    }

    public static AccountTypeEnum getTypeByName(String name) {
        for (AccountTypeEnum type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }
}

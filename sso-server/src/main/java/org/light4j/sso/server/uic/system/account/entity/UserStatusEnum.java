package org.light4j.sso.server.uic.system.account.entity;

import lombok.Getter;

/**
 * Created by longjiazuo on 2015/6/25.
 */
public enum UserStatusEnum {

    UNCERTIFIED(0, "Uncertified"),
    CERTIFIED(1, "Certified");

    UserStatusEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    @Getter
    private int value;
    @Getter
    private String name;

    public static String getNameByValue(int value) {
        for (UserStatusEnum status : values()) {
            if (status.value == value) {
                return status.name;
            }
        }
        return null;
    }
}

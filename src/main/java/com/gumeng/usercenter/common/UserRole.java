package com.gumeng.usercenter.common;

/**
 * @author 顾梦
 * @description TODO
 * @since 2023/9/16
 */
public enum UserRole {
    USER(0),
    ADMIN(1);

    private int value;

    private UserRole(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}


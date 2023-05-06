package com.irlab.view.common;

public enum Type {
    SMS_LOGIN(2),
    PASSWORD_LOGIN(1);

    Integer value;
    Type (Integer value) { this.value = value; }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}

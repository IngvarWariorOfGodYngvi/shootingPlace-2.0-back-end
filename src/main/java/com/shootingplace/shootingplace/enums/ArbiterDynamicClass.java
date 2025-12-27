package com.shootingplace.shootingplace.enums;

import lombok.Getter;

@Getter
public enum ArbiterDynamicClass {
    ARO("Assistant Range Officer"),
    RO("Range Officer"),
    CRO("Chef Range Officer"),
    RM("Range Master");

    private final String name;

    ArbiterDynamicClass(String name) {
        this.name = name;
    }

}

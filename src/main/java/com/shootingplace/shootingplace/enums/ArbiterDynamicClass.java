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
    public static ArbiterDynamicClass fromName(String name) {
        for (ArbiterDynamicClass arbiterClass : values()) {
            if (arbiterClass.name.equalsIgnoreCase(name)) {
                return arbiterClass;
            }
        }
        throw new IllegalArgumentException("Nieznana klasa: " + name);
    }
}

package com.shootingplace.shootingplace.enums;

import lombok.Getter;

@Getter
public enum ArbiterStaticClass {
    III("Klasa 3"),
    II("Klasa 2"),
    I("Klasa 1"),
    P("Klasa Państwowa");
    private final String name;

    ArbiterStaticClass(String name) {
        this.name = name;
    }

    public static ArbiterStaticClass fromName(String name) {
        for (ArbiterStaticClass arbiterClass : values()) {
            if (arbiterClass.name.equalsIgnoreCase(name)) {
                return arbiterClass;
            }
        }
        throw new IllegalArgumentException("Nieznana klasa: " + name);
    }
}

package com.shootingplace.shootingplace.enums;

import lombok.Getter;

@Getter
public enum CountingMethod {
    NORMAL("NORMAL"),
    COMSTOCK("COMSTOCK"),
    TIME("CZAS"),
    IPSC("IPSC"),
    DYNAMIKADZIESIATKA("Dynamika Dziesiątka"),
    POJEDYNEK("Pojedynek Strzelecki");

    private final String name;

    CountingMethod(String name) {
        this.name = name;
    }

}

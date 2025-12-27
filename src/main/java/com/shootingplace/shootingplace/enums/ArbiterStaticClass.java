package com.shootingplace.shootingplace.enums;

import lombok.Getter;

@Getter
public enum ArbiterStaticClass {
    CLASS_3("Klasa 3"),
    CLASS_2("Klasa 2"),
    CLASS_1("Klasa 1"),
    CLASS_STATE("Klasa Państwowa"),
    CLASS_INTERNATIONAL("Klasa Międzynarodowa");

    private final String name;

    ArbiterStaticClass(String name) {
        this.name = name;
    }


}

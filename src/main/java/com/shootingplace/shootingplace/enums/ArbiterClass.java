package com.shootingplace.shootingplace.enums;

public enum ArbiterClass {
    CLASS_3("Klasa 3"),
    CLASS_2("Klasa 2"),
    CLASS_1("Klasa 1"),
    CLASS_STATE("Klasa Państwowa"),
    CLASS_INTERNATIONAL("Klasa Międzynarodowa");

    private final String name;

    ArbiterClass(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }



}

package com.shootingplace.shootingplace.enums;

public enum ArbiterWorkClass {
    MAIN_ARBITER("Sędzia Główny Zawodów"),
    RTS_ARBITER("Przewodniczący Komisji RTS"),
    HELP("Sędzia Stanowiskowy"),
    RTS_HELP("Sędzia Biura Obliczeń");

    private final String name;

    ArbiterWorkClass(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }



}

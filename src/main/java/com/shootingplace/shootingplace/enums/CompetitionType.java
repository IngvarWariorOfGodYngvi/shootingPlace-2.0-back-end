package com.shootingplace.shootingplace.enums;

public enum CompetitionType {
    OPEN("OPEN"),
    STANDARD("STANDARD"),
    YOUTH("Młodzieżowa"),
    MINOR("MINOR"),
    MAJOR("MAJOR");

    private final String name;

    CompetitionType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}

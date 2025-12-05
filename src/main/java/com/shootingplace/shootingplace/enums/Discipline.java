package com.shootingplace.shootingplace.enums;

public enum Discipline {
    PISTOL("Pistolet"),
    RIFLE("Karabin"),
    SHOTGUN("Strzelba");

    private final String name;

    Discipline(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


}

package com.shootingplace.shootingplace.enums;

import lombok.Getter;

@Getter
public enum Discipline {
    PISTOL("Pistolet"),
    RIFLE("Karabin"),
    SHOTGUN("Strzelba");

    private final String name;

    Discipline(String name) {
        this.name = name;
    }


}

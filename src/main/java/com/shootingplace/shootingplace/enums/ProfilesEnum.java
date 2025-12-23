package com.shootingplace.shootingplace.enums;

import lombok.Getter;

@Getter
public enum ProfilesEnum {
    DZIESIATKA("prod"),
    TEST("test"),
    PANASZEW("rcs"),
    MECHANIK("uks"),
    GUARDIANS("guardians");

    private final String name;

    ProfilesEnum(String name) {
        this.name = name;
    }

}

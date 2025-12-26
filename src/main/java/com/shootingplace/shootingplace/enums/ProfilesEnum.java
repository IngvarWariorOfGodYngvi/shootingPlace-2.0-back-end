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
    public static ProfilesEnum fromName(String name) {
        for (ProfilesEnum profile : values()) {
            if (profile.name.equalsIgnoreCase(name)) {
                return profile;
            }
        }
        throw new IllegalArgumentException("Unknown profile: " + name);
    }
}

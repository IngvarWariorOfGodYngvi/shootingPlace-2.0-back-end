package com.shootingplace.shootingplace.enums;

public enum GunType {
    RIMFIRE_RIFLE("Karabin Bocznego zapłonu"),
    CENTERFIRE_RIFLE("Karabin Centralnego Zapłonu"),
    RIMFIRE_PISTOL("Pistolet Bocznego zapłonu"),
    CENTERFIRE_PISTOL("Pistolet Centralnego Zapłonu"),
    RIMFIRE_REWOLVER("Rewolwer Bocznego zapłonu"),
    CENTERFIRE_REWOLVER("Rewolwer Centralnego Zapłonu"),
    SMOOTHBORE_SHOTGUN("Strzelba Gładkolufowa");
    private final String name;

    GunType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


}

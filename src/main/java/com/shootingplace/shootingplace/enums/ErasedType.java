package com.shootingplace.shootingplace.enums;

public enum ErasedType {
    NONE("Brak"),
    RESIGNATION("Rezygnacja z członkostwa"),
    CHANGE_BELONGING("Zmiana barw klubowych"),
    CLUB_DECISION("Decyzja klubu"),
    OTHER("Inne");

    private final String name;

    ErasedType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


}

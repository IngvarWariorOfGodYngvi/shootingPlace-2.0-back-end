package com.shootingplace.shootingplace.enums;

import lombok.Getter;

@Getter
public enum ErasedType {
    RESIGNATION("Rezygnacja z członkostwa"),
    CHANGE_BELONGING("Zmiana barw klubowych"),
    CLUB_DECISION("Decyzja klubu"),
    OTHER("Inne");

    private final String name;

    ErasedType(String name) {
        this.name = name;
    }


}

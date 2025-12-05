package com.shootingplace.shootingplace.enums;

public enum UserSubType {
    ADMIN("Admin"),
    MANAGEMENT("Zarząd"),
    WEAPONS_WAREHOUSEMAN("Magazynier"),
    WORKER("Pracownik"),
    REVISION_COMMITTEE("Komisja Rewizyjna"),
    VISITOR("Gość"),
    SUPER_USER("Super Użytkownik"),
    CEO("Prezes");
    private final String name;

    UserSubType(String name) {this.name = name;}


    public String getName() {
        return name;
    }
}

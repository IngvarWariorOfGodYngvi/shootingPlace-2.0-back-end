package com.shootingplace.shootingplace.utils;

public class NameNormalizer {

    public static String normalizeFirstName(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String v = value.trim().toLowerCase();
        return v.substring(0, 1).toUpperCase() + v.substring(1);
    }
    public static String normalizeSecondName(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.trim().replaceAll("\\s+", " ").toUpperCase();
    }

}

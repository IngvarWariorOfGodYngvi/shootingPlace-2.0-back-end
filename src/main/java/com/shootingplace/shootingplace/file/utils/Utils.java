package com.shootingplace.shootingplace.file.utils;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;

import java.io.IOException;
import java.text.Collator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Utils {

    public static String dateFormat(LocalDate date) {

        String day = String.valueOf(date.getDayOfMonth());
        String month = "";

        if (date.getMonth().getValue() == 1) {
            month = "Stycznia";
        }
        if (date.getMonth().getValue() == 2) {
            month = "Lutego";
        }
        if (date.getMonth().getValue() == 3) {
            month = "Marca";
        }
        if (date.getMonth().getValue() == 4) {
            month = "Kwietnia";
        }
        if (date.getMonth().getValue() == 5) {
            month = "Maja";
        }
        if (date.getMonth().getValue() == 6) {
            month = "Czerwca";
        }
        if (date.getMonth().getValue() == 7) {
            month = "Lipca";
        }
        if (date.getMonth().getValue() == 8) {
            month = "Sierpnia";
        }
        if (date.getMonth().getValue() == 9) {
            month = "Września";
        }
        if (date.getMonth().getValue() == 10) {
            month = "Października";
        }
        if (date.getMonth().getValue() == 11) {
            month = "Listopada";
        }
        if (date.getMonth().getValue() == 12) {
            month = "Grudnia";
        }
        String year = String.valueOf(date.getYear());
        return day + " " + month + " " + year;
    }

    public static String arabicToRomanNumberConverter(int arabicNumber) {
        return switch (arabicNumber) {
            case 0 -> "";
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> "error";
        };
    }

    public static String getArbiterClass(String arbiterClass) {
        switch (arbiterClass) {
            case "Klasa 3" -> arbiterClass = "Sędzia Klasy Trzeciej";
            case "Klasa 2" -> arbiterClass = "Sędzia Klasy Drugiej";
            case "Klasa 1" -> arbiterClass = "Sędzia Klasy Pierwszej";
            case "Klasa Państwowa" -> arbiterClass = "Sędzia Klasy Państwowej";
            case "Klasa Międzynarodowa" -> arbiterClass = "Sędzia Klasy Międzynarodowej";
        }
        return arbiterClass;
    }

    public static Collator pl() {
        return Collator.getInstance(Locale.forLanguageTag("pl"));
    }

    public static DateTimeFormatter dateFormat() {
        String europeanDatePattern = "dd.MM.yyyy";
        return DateTimeFormatter.ofPattern(europeanDatePattern);
    }

    public static Font font(int size, int style) throws IOException, DocumentException {
        BaseFont base = BaseFont.createFont("font/times.ttf", BaseFont.IDENTITY_H, BaseFont.CACHED);
        return new Font(base, size, style);
    }
}

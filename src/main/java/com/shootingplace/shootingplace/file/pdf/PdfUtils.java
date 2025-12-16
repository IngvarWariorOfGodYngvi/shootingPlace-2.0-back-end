package com.shootingplace.shootingplace.file.pdf;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;

import java.io.IOException;
import java.text.Collator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class PdfUtils {

    private PdfUtils() {
    }

    public static Font font(int size, int style) throws IOException, DocumentException {
        BaseFont base = BaseFont.createFont("font/times.ttf", BaseFont.IDENTITY_H, BaseFont.CACHED);
        return new Font(base, size, style);
    }

    public static Collator pl() {
        return Collator.getInstance(Locale.forLanguageTag("pl"));
    }

    public static DateTimeFormatter dateFormat() {
        return DateTimeFormatter.ofPattern("dd.MM.yyyy");
    }

    public static String monthFormat(LocalDate date) {

        String day = String.valueOf(date.getDayOfMonth());
        String month = "";

        if (date.getMonth().getValue() == 1) {
            month = "stycznia";
        }
        if (date.getMonth().getValue() == 2) {
            month = "lutego";
        }
        if (date.getMonth().getValue() == 3) {
            month = "marca";
        }
        if (date.getMonth().getValue() == 4) {
            month = "kwietnia";
        }
        if (date.getMonth().getValue() == 5) {
            month = "maja";
        }
        if (date.getMonth().getValue() == 6) {
            month = "czerwca";
        }
        if (date.getMonth().getValue() == 7) {
            month = "lipca";
        }
        if (date.getMonth().getValue() == 8) {
            month = "sierpnia";
        }
        if (date.getMonth().getValue() == 9) {
            month = "września";
        }
        if (date.getMonth().getValue() == 10) {
            month = "października";
        }
        if (date.getMonth().getValue() == 11) {
            month = "listopada";
        }
        if (date.getMonth().getValue() == 12) {
            month = "grudnia";
        }
        String year = String.valueOf(date.getYear());
        return day + " " + month + " " + year;
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
}

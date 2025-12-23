package com.shootingplace.shootingplace.enums;

import lombok.Getter;

@Getter
public enum MailType {
    TEST("Test"),
    SUBSCRIPTION_REMINDER("Przypomnienie o składkach"),
    SUBSCRIPTION_REMINDER_BEFORE("Przypomnienie o składkach przed wygaśnięciem"),
    CONTRIBUTION_CONFIRMATION("Potwierdzenie Składki"),
    LICENSE_PAYMENT_CONFIRMATION("Potwierdzenie Opłacenia Licencji"),
    CONGRATULATIONS_ANNIVERSSARY("Gratulacje Rocznicy"),
    CUSTOM("Własny"),
    REGISTRATION_CONFIRMATION("Potwierdzenie zapisu");
    private final String name;

    MailType(String name) {
        this.name = name;
    }


}

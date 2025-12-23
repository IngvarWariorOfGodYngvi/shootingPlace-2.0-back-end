package com.shootingplace.shootingplace.enums;

import com.shootingplace.shootingplace.email.EmailSendList;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public enum MailToggleOptions {
    SEND_REMINDERS_FOR_ACTIVE_ONE_MONTH_BEFORE("Przypomnienie o składkach miesiąc przed", EmailSendList::isSendRemindersForActiveOneMonthBefore),
    SEND_REMINDERS_FOR_NON_ACTIVE("Przypomnienie o składkach", EmailSendList::isSendRemindersForNonActive),
    SEND_CONGRATULATIONS_ON_THE_ANNIVERSARY("Gratulacje Rocznicy", EmailSendList::isSendCongratulationsOnTheAnniversary),
    SEND_REGISTRATION_CONFIRMATION("Potwierdzenie zapisu", EmailSendList::isSendRegistrationConfirmation),
    SEND_CONTRIBUTION_CONFIRMATION("Potwierdzenie opłacenia składki", EmailSendList::isSendContributionConfirmation),
    SEND_LICENSE_PAYMENT_CONFIRMATION("Potwierdzenie opłacenia licencji", EmailSendList::isSendLicensePaymentConfirmation),
    SEND_INDIVIDUAL("Indywidualne wiadomości", EmailSendList::isSendIndividual);

    @Getter
    private final String name;

    private final Function<EmailSendList, Boolean> extractor;

    MailToggleOptions(String name, Function<EmailSendList, Boolean> extractor) {
        this.name = name;
        this.extractor = extractor;
    }

    public Boolean extract(EmailSendList list) {
        return extractor.apply(list);
    }

    public static Optional<MailToggleOptions> fromDisplayName(String name) {
        return Arrays.stream(values())
                .filter(v -> v.name.equalsIgnoreCase(name))
                .findFirst();
    }


}

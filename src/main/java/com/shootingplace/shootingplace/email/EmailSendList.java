package com.shootingplace.shootingplace.email;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Getter
@Setter
public class EmailSendList {
    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;

    private boolean sendRemindersForActiveOneMonthBefore;
    private boolean sendRemindersForNonActive;
    private boolean sendCongratulationsOnTheAnniversary;
    private boolean sendRegistrationConfirmation;
    private boolean sendContributionConfirmation;
    private boolean sendLicensePaymentConfirmation;
    private boolean sendIndividual;

    @Override
    public String toString() {
        return "EmailSendList{" +
                "uuid='" + uuid + '\'' +
                ", sendRemindersForActiveOneMonthBefore=" + sendRemindersForActiveOneMonthBefore +
                ", sendRemindersForNonActive=" + sendRemindersForNonActive +
                ", sendCongratulationsOnTheAnniversary=" + sendCongratulationsOnTheAnniversary +
                ", sendRegistrationConfirmation=" + sendRegistrationConfirmation +
                ", sendContributionConfirmation=" + sendContributionConfirmation +
                ", sendLicensePaymentConfirmation=" + sendLicensePaymentConfirmation +
                ", sendIndividual=" + sendIndividual +
                '}';
    }
}

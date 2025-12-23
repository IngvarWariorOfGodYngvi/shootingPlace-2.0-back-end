package com.shootingplace.shootingplace.email;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
@Data
@Entity
public class EmailSendList {
    @Id
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

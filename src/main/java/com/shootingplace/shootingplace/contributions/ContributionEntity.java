package com.shootingplace.shootingplace.contributions;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContributionEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;

    private LocalDate paymentDay;
    private LocalDate validThru;

    private String historyUUID;
    private String acceptedBy;
    private boolean edited;

    public String getUuid() {
        return uuid;
    }

    public LocalDate getPaymentDay() {
        return paymentDay;
    }

    public void setPaymentDay(LocalDate paymentDay) {
        this.paymentDay = paymentDay;
    }

    public LocalDate getValidThru() {
        return validThru;
    }

    public void setValidThru(LocalDate validThru) {
        this.validThru = validThru;
    }

    public String getHistoryUUID() {
        return historyUUID;
    }

    public void setHistoryUUID(String historyUUID) {
        this.historyUUID = historyUUID;
    }

    public String getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(String acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }
}

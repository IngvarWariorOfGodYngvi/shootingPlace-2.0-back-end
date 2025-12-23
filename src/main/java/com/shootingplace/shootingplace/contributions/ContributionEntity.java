package com.shootingplace.shootingplace.contributions;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContributionEntity {
    @Id
    @UuidGenerator
    private String uuid;

    private LocalDate paymentDay;
    private LocalDate validThru;

    private String historyUUID;
    private String acceptedBy;
    private boolean edited;

    public void setPaymentDay(LocalDate paymentDay) {
        this.paymentDay = paymentDay;
    }

    public void setValidThru(LocalDate validThru) {
        this.validThru = validThru;
    }

    public void setHistoryUUID(String historyUUID) {
        this.historyUUID = historyUUID;
    }

    public void setAcceptedBy(String acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }
}

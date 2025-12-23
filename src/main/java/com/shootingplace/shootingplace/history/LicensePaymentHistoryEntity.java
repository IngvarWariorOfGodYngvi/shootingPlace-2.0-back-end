package com.shootingplace.shootingplace.history;

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
public class LicensePaymentHistoryEntity {

    @Id
    @UuidGenerator
    private String uuid;

    private LocalDate date;

    private String memberUUID;

    private Integer validForYear;

    private String acceptedBy;

    private boolean isPayInPZSSPortal;

    private boolean isNew;

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setMemberUUID(String memberUUID) {
        this.memberUUID = memberUUID;
    }

    public void setValidForYear(Integer validForYear) {
        this.validForYear = validForYear;
    }

    public void setAcceptedBy(String acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public void setPayInPZSSPortal(boolean payInPZSSPortal) {
        isPayInPZSSPortal = payInPZSSPortal;
    }

}

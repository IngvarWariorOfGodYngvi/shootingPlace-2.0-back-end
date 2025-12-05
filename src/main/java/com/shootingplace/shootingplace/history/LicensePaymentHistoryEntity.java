package com.shootingplace.shootingplace.history;

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
public class LicensePaymentHistoryEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;

    private LocalDate date;

    private String memberUUID;

    private Integer validForYear;

    private String acceptedBy;

    private boolean isPayInPZSSPortal;

    private boolean isNew;

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public String getUuid() {
        return uuid;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getMemberUUID() {
        return memberUUID;
    }

    public void setMemberUUID(String memberUUID) {
        this.memberUUID = memberUUID;
    }

    public Integer getValidForYear() {
        return validForYear;
    }

    public void setValidForYear(Integer validForYear) {
        this.validForYear = validForYear;
    }

    public String getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(String acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public boolean isPayInPZSSPortal() {
        return isPayInPZSSPortal;
    }

    public void setPayInPZSSPortal(boolean payInPZSSPortal) {
        isPayInPZSSPortal = payInPZSSPortal;
    }

}

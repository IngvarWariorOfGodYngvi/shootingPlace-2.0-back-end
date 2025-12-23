package com.shootingplace.shootingplace.ammoEvidence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmmoDTO {

    private String number;
    private String evidenceUUID;
    private LocalDate date;

    public void setEvidenceUUID(String evidenceUUID) {
        this.evidenceUUID = evidenceUUID;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}

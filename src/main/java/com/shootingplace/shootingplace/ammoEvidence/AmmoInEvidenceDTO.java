package com.shootingplace.shootingplace.ammoEvidence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmmoInEvidenceDTO {

    private String uuid;

    private String caliberName;

    private Integer quantity;

    private List<AmmoUsedToEvidenceDTO> ammoUsedToEvidenceDTOList;

    private String signedBy;

    private String imageUUID;
    private LocalDate date;
    private LocalTime time;
    private boolean locked;

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setCaliberName(String caliberName) {
        this.caliberName = caliberName;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setAmmoUsedToEvidenceDTOList(List<AmmoUsedToEvidenceDTO> ammoUsedToEvidenceDTOList) {
        this.ammoUsedToEvidenceDTOList = ammoUsedToEvidenceDTOList;
    }

    public void setSignedBy(String signedBy) {
        this.signedBy = signedBy;
    }

    public void setImageUUID(String imageUUID) {
        this.imageUUID = imageUUID;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}

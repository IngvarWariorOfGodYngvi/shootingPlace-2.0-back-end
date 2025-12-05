package com.shootingplace.shootingplace.ammoEvidence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCaliberName() {
        return caliberName;
    }

    public void setCaliberName(String caliberName) {
        this.caliberName = caliberName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public List<AmmoUsedToEvidenceDTO> getAmmoUsedToEvidenceDTOList() {
        return ammoUsedToEvidenceDTOList;
    }

    public void setAmmoUsedToEvidenceDTOList(List<AmmoUsedToEvidenceDTO> ammoUsedToEvidenceDTOList) {
        this.ammoUsedToEvidenceDTOList = ammoUsedToEvidenceDTOList;
    }

    public String getSignedBy() {
        return signedBy;
    }

    public void setSignedBy(String signedBy) {
        this.signedBy = signedBy;
    }

    public String getImageUUID() {
        return imageUUID;
    }

    public void setImageUUID(String imageUUID) {
        this.imageUUID = imageUUID;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}

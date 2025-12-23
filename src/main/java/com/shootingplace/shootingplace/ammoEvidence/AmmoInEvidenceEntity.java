package com.shootingplace.shootingplace.ammoEvidence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmmoInEvidenceEntity {

    @Id
    @UuidGenerator
    private String uuid;

    private String caliberName;

    private String caliberUUID;

    private String evidenceUUID;

    private Integer quantity;
    @ManyToMany
    private List<AmmoUsedToEvidenceEntity> ammoUsedToEvidenceEntityList;

    private LocalDateTime dateTime;

    private String imageUUID;

    private String signedBy;
    private LocalDate signedDate;
    private LocalTime signedTime;
    private boolean locked;

    private float price;

    public void setCaliberName(String caliberName) {
        this.caliberName = caliberName;
    }

    public void setCaliberUUID(String caliberUUID) {
        this.caliberUUID = caliberUUID;
    }

    public void setEvidenceUUID(String evidenceUUID) {
        this.evidenceUUID = evidenceUUID;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;

    }

    public void setAmmoUsedToEvidenceEntityList(List<AmmoUsedToEvidenceEntity> ammoUsedToEvidenceEntityList) {
        this.ammoUsedToEvidenceEntityList = ammoUsedToEvidenceEntityList;
    }

    public void setImageUUID(String imageUUID) {
        this.imageUUID = imageUUID;
    }

    public void setSignedBy(String signedBy) {
        this.signedBy = signedBy;
    }

    public void setSignedDate(LocalDate signedDate) {
        this.signedDate = signedDate;
    }

    public void setSignedTime(LocalTime signedTime) {
        this.signedTime = signedTime;
    }

    public void lock() {
        this.locked = true;
    }

    @Override
    public String toString() {
        return "AmmoInEvidenceEntity{" +
                "uuid='" + uuid + '\'' +
                ", caliberName='" + caliberName + '\'' +
                ", caliberUUID='" + caliberUUID + '\'' +
                ", evidenceUUID='" + evidenceUUID + '\'' +
                ", quantity=" + quantity +
                ", ammoUsedToEvidenceEntityList=" + ammoUsedToEvidenceEntityList +
                ", dateTime=" + dateTime +
                ", imageUUID='" + imageUUID + '\'' +
                ", signedBy='" + signedBy + '\'' +
                ", signedDate=" + signedDate +
                ", signedTime=" + signedTime +
                ", locked=" + locked +
                ", price=" + price +
                '}';
    }
}

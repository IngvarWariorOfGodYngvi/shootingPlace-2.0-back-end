package com.shootingplace.shootingplace.ammoEvidence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmmoInEvidenceEntity {

    @Id
    @GeneratedValue
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

    public String getUuid() {
        return uuid;
    }

    public String getCaliberName() {
        return caliberName;
    }

    public void setCaliberName(String caliberName) {
        this.caliberName = caliberName;
    }

    public String getCaliberUUID() {
        return caliberUUID;
    }

    public void setCaliberUUID(String caliberUUID) {
        this.caliberUUID = caliberUUID;
    }

    public String getEvidenceUUID() {
        return evidenceUUID;
    }

    public void setEvidenceUUID(String evidenceUUID) {
        this.evidenceUUID = evidenceUUID;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;

    }

    public List<AmmoUsedToEvidenceEntity> getAmmoUsedToEvidenceEntityList() {
        return ammoUsedToEvidenceEntityList;
    }

    public void setAmmoUsedToEvidenceEntityList(List<AmmoUsedToEvidenceEntity> ammoUsedToEvidenceEntityList) {
        this.ammoUsedToEvidenceEntityList = ammoUsedToEvidenceEntityList;
    }

    public String getImageUUID() {
        return imageUUID;
    }

    public void setImageUUID(String imageUUID) {
        this.imageUUID = imageUUID;
    }

    public String getSignedBy() {
        return signedBy;
    }

    public void setSignedBy(String signedBy) {
        this.signedBy = signedBy;
    }

    public LocalDate getSignedDate() {
        return signedDate;
    }

    public void setSignedDate(LocalDate signedDate) {
        this.signedDate = signedDate;
    }

    public LocalTime getSignedTime() {
        return signedTime;
    }

    public void setSignedTime(LocalTime signedTime) {
        this.signedTime = signedTime;
    }

    public boolean isLocked() {
        return locked;
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

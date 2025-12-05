package com.shootingplace.shootingplace.armory;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaliberEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;

    private String name;
    private Integer quantity;
    private boolean active;
    @OneToMany
    private List<CaliberUsedEntity> ammoUsed;
    @OneToMany
    private List<CalibersAddedEntity> ammoAdded;

    private float unitPrice;
    private float unitPriceForNotMember;

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public List<CaliberUsedEntity> getAmmoUsed() {
        return ammoUsed;
    }

    public float getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(float unitPrice) {
        this.unitPrice = unitPrice;
    }

    public float getUnitPriceForNotMember() {
        return unitPriceForNotMember;
    }

    public void setUnitPriceForNotMember(float unitPriceForNotMember) {
        this.unitPriceForNotMember = unitPriceForNotMember;
    }

    public void setAmmoUsed(List<CaliberUsedEntity> ammoUsed) {
        this.ammoUsed = ammoUsed;
    }

    public List<CalibersAddedEntity> getAmmoAdded() {
        return ammoAdded;
    }

    public void setAmmoAdded(List<CalibersAddedEntity> ammoAdded) {
        this.ammoAdded = ammoAdded;
    }

    public void changeActive(){
        this.active = !this.active;
    }

    public boolean isActive() {
        return active;
    }
}

package com.shootingplace.shootingplace.armory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Caliber {
    private String uuid;
    private String name;

    private Integer quantity;
    private float unitPrice;
    private float unitPriceForNotMember;
    private boolean active;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    @Override
    public String toString() {
        return "Caliber{" +
                "name='" + name + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}

package com.shootingplace.shootingplace.armory;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;


@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaliberEntity {
    @Id
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

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setUnitPrice(float unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setUnitPriceForNotMember(float unitPriceForNotMember) {
        this.unitPriceForNotMember = unitPriceForNotMember;
    }

    public void setAmmoUsed(List<CaliberUsedEntity> ammoUsed) {
        this.ammoUsed = ammoUsed;
    }

    public void setAmmoAdded(List<CalibersAddedEntity> ammoAdded) {
        this.ammoAdded = ammoAdded;
    }

    public void changeActive(){
        this.active = !this.active;
    }

}

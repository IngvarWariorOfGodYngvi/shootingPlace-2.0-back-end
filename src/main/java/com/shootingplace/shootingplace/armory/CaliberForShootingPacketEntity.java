package com.shootingplace.shootingplace.armory;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaliberForShootingPacketEntity {

    @Id
    @UuidGenerator
    private String uuid;

    private String caliberUUID;
    private String caliberName;

    private int quantity;

    public void setCaliberUUID(String caliberUUID) {
        this.caliberUUID = caliberUUID;
    }

    public void setCaliberName(String caliberName) {
        this.caliberName = caliberName;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}

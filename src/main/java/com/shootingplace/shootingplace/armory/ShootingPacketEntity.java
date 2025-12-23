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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShootingPacketEntity {

    @Id
    @UuidGenerator
    private String uuid;

    private String name;
    @OneToMany
    private List<CaliberForShootingPacketEntity> calibers;
    private float price;

    public void setName(String name) {
        this.name = name;
    }

    public void setCalibers(List<CaliberForShootingPacketEntity> calibers) {
        this.calibers = calibers;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}

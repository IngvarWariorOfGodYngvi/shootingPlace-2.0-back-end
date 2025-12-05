package com.shootingplace.shootingplace.armory;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShootingPacketEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;

    private String name;
    @OneToMany
    private List<CaliberForShootingPacketEntity> calibers;
    private float price;

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CaliberForShootingPacketEntity> getCalibers() {
        return calibers;
    }

    public void setCalibers(List<CaliberForShootingPacketEntity> calibers) {
        this.calibers = calibers;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}

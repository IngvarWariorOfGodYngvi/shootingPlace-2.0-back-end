package com.shootingplace.shootingplace.armory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ShootingPacketDTO {

    private String name;
    private List<CaliberForShootingPacketDTO> calibers;
    private float price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CaliberForShootingPacketDTO> getCalibers() {
        return calibers;
    }

    public void setCalibers(List<CaliberForShootingPacketDTO> calibers) {
        this.calibers = calibers;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}

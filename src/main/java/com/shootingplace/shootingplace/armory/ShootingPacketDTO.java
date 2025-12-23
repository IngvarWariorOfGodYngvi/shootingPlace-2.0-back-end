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

}

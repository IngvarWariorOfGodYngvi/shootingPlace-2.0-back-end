package com.shootingplace.shootingplace.armory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShootingPacketDTO {

    private String name;
    private List<CaliberForShootingPacketDTO> calibers;
    private Float price;

}

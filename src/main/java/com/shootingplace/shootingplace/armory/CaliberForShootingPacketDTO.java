package com.shootingplace.shootingplace.armory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CaliberForShootingPacketDTO {

    private String caliberName;
    private String caliberUUID;

    private int quantity;
}

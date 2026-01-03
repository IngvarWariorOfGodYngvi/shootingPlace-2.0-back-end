package com.shootingplace.shootingplace.ammoEvidence;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmmoUsedPersonal {
    private String caliberName;

    private String memberUUID;

    private String caliberUUID;

    private Integer otherPersonEntityID;

    private String memberName;

    private Integer counter;

    private LocalDate date;

    private LocalTime time;
}

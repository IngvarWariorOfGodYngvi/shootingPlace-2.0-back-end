package com.shootingplace.shootingplace.ammoEvidence;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmmoDTO {

    private String number;
    private String evidenceUUID;
    private LocalDate date;
}

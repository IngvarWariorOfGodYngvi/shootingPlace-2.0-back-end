package com.shootingplace.shootingplace.shootingPatent;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShootingPatent {


    private String patentNumber;

    private Boolean pistolPermission;

    private Boolean riflePermission;

    private Boolean shotgunPermission;

    private LocalDate dateOfPosting;

}

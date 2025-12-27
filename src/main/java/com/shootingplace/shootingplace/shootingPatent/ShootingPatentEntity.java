package com.shootingplace.shootingplace.shootingPatent;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShootingPatentEntity {

    @Id
    @UuidGenerator
    private String uuid;
    private String patentNumber;

    private boolean pistolPermission;

    private boolean riflePermission;

    private boolean shotgunPermission;

    private LocalDate dateOfPosting;

}

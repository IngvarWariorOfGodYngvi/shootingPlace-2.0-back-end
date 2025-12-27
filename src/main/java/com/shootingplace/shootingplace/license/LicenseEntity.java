package com.shootingplace.shootingplace.license;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LicenseEntity {

    @Id
    @UuidGenerator
    private String uuid;
    @Pattern(regexp = "[0-9]*")
    private String number;
    private LocalDate validThru;
    private boolean pistolPermission;
    private boolean riflePermission;
    private boolean shotgunPermission;
    private boolean valid;
    private boolean canProlong;
    private boolean paid;
}

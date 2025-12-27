package com.shootingplace.shootingplace.armory;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaliberUsedEntity {

    @Id
    @UuidGenerator
    private String uuid;

    private String belongTo;

    private LocalDate date;
    private LocalTime time;
    private Integer ammoUsed;
    private float unitPrice;

}

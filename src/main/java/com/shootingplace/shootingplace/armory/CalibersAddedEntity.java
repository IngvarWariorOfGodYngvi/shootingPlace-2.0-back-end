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
public class CalibersAddedEntity {

    @Id
    @UuidGenerator
    private String uuid;

    private String belongTo;
    private String caliberName;
    private String description;

    private LocalDate date;
    private LocalTime time;
    private Integer ammoAdded;
    private String imageUUID;

    private Integer stateForAddedDay;
    private Integer finalStateForAddedDay;
    private String addedBy;

}

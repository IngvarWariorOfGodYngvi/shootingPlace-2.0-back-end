package com.shootingplace.shootingplace.history;

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
public class JudgingHistoryEntity {

    @Id
    @UuidGenerator
    private String uuid;
    private String name;

    private String tournamentUUID;
    private String judgingFunction;
    private LocalDate date;
    private LocalTime time;

}

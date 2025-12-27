package com.shootingplace.shootingplace.history;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsedHistoryEntity {

    @Id
    @UuidGenerator
    private String uuid;

    private String gunName;
    private String gunUUID;
    private String gunSerialNumber;
    private LocalDateTime dateTime;

    private String usedType;
    private String evidenceUUID;
    private boolean returnToStore;
    private String userName;
    private String memberUUID;
}

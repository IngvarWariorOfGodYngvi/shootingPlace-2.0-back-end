package com.shootingplace.shootingplace.history;

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
public class LicensePaymentHistoryEntity {

    @Id
    @UuidGenerator
    private String uuid;

    private LocalDate date;

    private String memberUUID;

    private Integer validForYear;

    private String acceptedBy;

    private boolean isPayInPZSSPortal;

    private boolean isNew;

}

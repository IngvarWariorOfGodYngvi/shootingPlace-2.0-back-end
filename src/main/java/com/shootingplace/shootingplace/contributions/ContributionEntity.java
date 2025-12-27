package com.shootingplace.shootingplace.contributions;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContributionEntity {
    @Id
    @UuidGenerator
    private String uuid;

    private LocalDate paymentDay;
    private LocalDate validThru;

    private String historyUUID;
    private String acceptedBy;
    private boolean edited;
}

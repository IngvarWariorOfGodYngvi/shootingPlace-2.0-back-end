package com.shootingplace.shootingplace.history;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LicensePaymentHistory {

    private LocalDate date;

    private String memberUUID;

    private Integer validForYear;

    private boolean isPayInPZSSPortal;

}

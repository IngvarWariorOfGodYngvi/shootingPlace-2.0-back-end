package com.shootingplace.shootingplace.history;

import lombok.*;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LicensePaymentHistoryDTO {

    private String paymentUuid;
    private String firstName;
    private String secondName;
    private String email;
    private Integer legitimationNumber;
    private Boolean adult;
    private Boolean active;
    private String memberUUID;
    private LocalDate date;
    private String licenseUUID;
    private Integer validForYear;
    private boolean isPayInPZSSPortal;
    private boolean isNew;

}

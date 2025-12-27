package com.shootingplace.shootingplace.contributions;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Contribution {

    private LocalDate paymentDay;
    private LocalDate validThru;
    private String historyUUID;

}

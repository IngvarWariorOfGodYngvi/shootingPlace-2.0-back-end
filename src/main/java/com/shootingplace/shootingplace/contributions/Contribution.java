package com.shootingplace.shootingplace.contributions;

import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Contribution {

    private LocalDate paymentDay;
    private LocalDate validThru;
    private String historyUUID;

}

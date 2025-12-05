package com.shootingplace.shootingplace.workingTimeEvidence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class WorkingTimeEvidenceDTO {

    private String uuid;

    private LocalDateTime start;
    private LocalDateTime stop;
    private String cardNumber;
    private String workTime;
    private boolean isAccepted;
    private boolean isClose;
    private boolean isAutomatedClosed;
    private boolean toClarify;
    private String user;
    private String workType;
}

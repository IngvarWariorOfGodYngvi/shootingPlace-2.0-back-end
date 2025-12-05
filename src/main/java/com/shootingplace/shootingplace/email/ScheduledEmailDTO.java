package com.shootingplace.shootingplace.email;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ScheduledEmailDTO {

    private String uuid;

    private String recipient;
    private String subject;
    private String htmlContent;

    private LocalDateTime scheduledFor;
    private String mailType;
    private String memberName;
}

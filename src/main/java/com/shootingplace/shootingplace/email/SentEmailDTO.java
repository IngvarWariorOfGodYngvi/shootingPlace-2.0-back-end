package com.shootingplace.shootingplace.email;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SentEmailDTO {

    private String uuid;

    private String recipient;
    private String subject;

    private String content;

    private LocalDateTime sentAt;
    private boolean success;
    private String errorMessage;

    private String memberName;
    private String mailType;
}

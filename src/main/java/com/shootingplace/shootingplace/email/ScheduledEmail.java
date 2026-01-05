package com.shootingplace.shootingplace.email;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduledEmail {

    @Id
    @UuidGenerator
    private String uuid;

    private String recipient;
    private String subject;
    @Column(columnDefinition = "TEXT")
    private String htmlContent;

    private LocalDateTime scheduledFor;

    private String mailType;

    private String memberUUID;
}

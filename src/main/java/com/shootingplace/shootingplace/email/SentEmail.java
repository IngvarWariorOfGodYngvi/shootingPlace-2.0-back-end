package com.shootingplace.shootingplace.email;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SentEmail {

    @Id
    @UuidGenerator
    private String uuid;

    private String recipient;
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime sentAt;
    private boolean success;
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private String memberUUID;
    private String mailType;

}


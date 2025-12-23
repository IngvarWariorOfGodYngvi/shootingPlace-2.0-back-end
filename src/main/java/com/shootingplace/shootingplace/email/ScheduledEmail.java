package com.shootingplace.shootingplace.email;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Getter
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

    public void setRecipient(String toAddress) {
        this.recipient = toAddress;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setHtmlContent(String content) {
        this.htmlContent = content;
    }

    public void setScheduledFor(LocalDateTime scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    public void setMailType(String mailType) {
        this.mailType = mailType;
    }

    public void setMemberUUID(String memberUUID) {
        this.memberUUID = memberUUID;
    }
}

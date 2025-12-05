package com.shootingplace.shootingplace.email;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduledEmail {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;

    private String recipient;
    private String subject;
    @Column(columnDefinition = "TEXT")
    private String htmlContent;

    private LocalDateTime scheduledFor;

    private String mailType;

    private String memberUUID;

    public String getUuid() {
        return uuid;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String toAddress) {
        this.recipient = toAddress;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String content) {
        this.htmlContent = content;
    }

    public LocalDateTime getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(LocalDateTime scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    public String getMailType() {
        return mailType;
    }

    public void setMailType(String mailType) {
        this.mailType = mailType;
    }

    public String getMemberUUID() {
        return memberUUID;
    }

    public void setMemberUUID(String memberUUID) {
        this.memberUUID = memberUUID;
    }
}

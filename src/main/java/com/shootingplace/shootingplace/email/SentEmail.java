package com.shootingplace.shootingplace.email;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setMemberUUID(String memberUUID) {
        this.memberUUID = memberUUID;
    }

    public void setMailType(String mailType) {
        this.mailType = mailType;
    }

    @Override
    public String toString() {
        return "SentEmail{" +
                "uuid='" + uuid + '\'' +
                ", recipient='" + recipient + '\'' +
                ", subject='" + subject + '\'' +
                ", sentAt=" + sentAt +
                ", success=" + success +
                ", errorMessage='" + errorMessage + '\'' +
                ", memberUUID='" + memberUUID + '\'' +
                ", mailType='" + mailType + '\'' +
                '}';
    }
}


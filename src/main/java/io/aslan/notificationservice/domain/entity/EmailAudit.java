package io.aslan.notificationservice.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class EmailAudit {

    @Id
    private UUID id;

    private String emailMessageId;
    private String toAddress;
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmailMessageId() {
        return emailMessageId;
    }

    public void setEmailMessageId(String emailMessageId) {
        this.emailMessageId = emailMessageId;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String to) {
        this.toAddress = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

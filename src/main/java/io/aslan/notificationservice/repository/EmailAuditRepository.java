package io.aslan.notificationservice.repository;

import io.aslan.notificationservice.domain.entity.EmailAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmailAuditRepository extends JpaRepository<EmailAudit, UUID> {
}

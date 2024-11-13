package io.aslan.notificationservice.service;

import io.aslan.notificationservice.domain.entity.EmailAudit;
import io.aslan.notificationservice.domain.message.AllowanceUpdateMessage;
import io.aslan.notificationservice.domain.entity.EmailContent;
import io.aslan.notificationservice.email.EmailContentBuilder;
import io.aslan.notificationservice.repository.EmailAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AllowanceNotificationService {

    private static final Logger log = LoggerFactory.getLogger(AllowanceNotificationService.class);
    private final ImageService imageService;
    private final EmailService emailService;
    private final EmailContentBuilder emailContentBuilder;
    private final EmailAuditRepository emailAuditRepository;

    public AllowanceNotificationService(ImageService imageService,
                                        EmailService emailService,
                                        EmailContentBuilder emailContentBuilder, EmailAuditRepository emailAuditRepository) {
        this.imageService = imageService;
        this.emailService = emailService;
        this.emailContentBuilder = emailContentBuilder;
        this.emailAuditRepository = emailAuditRepository;
    }

    public void handleAllowanceChange(UUID sqsMessageId, AllowanceUpdateMessage allowanceUpdateMessage) {
        boolean isPositiveChange = calculateAllowanceChange(allowanceUpdateMessage);
        String keyword = isPositiveChange ? "happy" : "calm";
        String imageUrl = imageService.getImageUrl(keyword);

        EmailContent emailContent = emailContentBuilder.buildAllowanceChangeEmailBody(allowanceUpdateMessage.email(),
                allowanceUpdateMessage.firstName(),
                allowanceUpdateMessage.lastName(),
                imageUrl,
                isPositiveChange);

        String emailMessageId = emailService.sendEmail(emailContent);

        auditEmail(sqsMessageId, emailMessageId, emailContent);
    }

    private void auditEmail(UUID sqsMessageId, String emailMessageId, EmailContent emailContent) {
        EmailAudit emailAudit = new EmailAudit();
        emailAudit.setId(sqsMessageId);
        emailAudit.setEmailMessageId(emailMessageId);
        emailAudit.setBody(emailContent.body());
        emailAudit.setSubject(emailContent.subject());
        emailAudit.setToAddress(emailContent.to());
        emailAuditRepository.save(emailAudit);
        log.info("Audited email delivery for emailMessageId={}", emailMessageId);
    }

    private boolean calculateAllowanceChange(AllowanceUpdateMessage allowanceUpdateMessage) {
        return allowanceUpdateMessage.newMonthlyAllowance().compareTo(allowanceUpdateMessage.currentMonthlyAllowance()) > 0;
    }
}

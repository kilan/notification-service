package io.aslan.notificationservice.service;

import io.aslan.notificationservice.domain.entity.EmailContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final String sourceEmail;
    private final SesClient sesClient;

    public EmailService(@Value("${source.email}") String sourceEmail, SesClient sesClient) {
        this.sourceEmail = sourceEmail;
        this.sesClient = sesClient;
    }

    public String sendEmail(EmailContent emailContent) {
        log.debug("About to send email for emailContent={}", emailContent);

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(Destination.builder().toAddresses(emailContent.to()).build())
                .message(Message.builder()
                        .subject(Content.builder().data(emailContent.subject()).build())
                        .body(Body.builder().text(Content.builder().data(emailContent.subject()).build()).build())
                        .build())
                .source(sourceEmail)
                .build();

        SendEmailResponse sendEmailResponse = sesClient.sendEmail(emailRequest);
        log.info("Sent email with id={}", sendEmailResponse.messageId());
        return sendEmailResponse.messageId();

    }
}
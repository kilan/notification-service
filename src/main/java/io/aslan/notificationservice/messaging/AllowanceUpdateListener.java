package io.aslan.notificationservice.messaging;

import io.aslan.notificationservice.domain.message.AllowanceUpdateMessage;
import io.aslan.notificationservice.service.AllowanceNotificationService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AllowanceUpdateListener {

    private static final Logger log = LoggerFactory.getLogger(AllowanceUpdateListener.class);

    private final AllowanceNotificationService allowanceNotificationService;

    public AllowanceUpdateListener(AllowanceNotificationService allowanceNotificationService) {
        this.allowanceNotificationService = allowanceNotificationService;
    }

    @SqsListener("${notification.service.queue.url}")
    public void processAllowanceUpdateMessage(Message<AllowanceUpdateMessage> message) {
        log.info("Received AllowanceUpdateMessage={}", message.getPayload());
        UUID messageId = message.getHeaders().get("id", UUID.class);
        allowanceNotificationService.handleAllowanceChange(messageId, message.getPayload());
        log.info("Processed AllowanceUpdateMessage");
    }
}

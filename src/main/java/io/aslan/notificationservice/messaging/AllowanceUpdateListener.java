package io.aslan.notificationservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aslan.notificationservice.domain.message.AllowanceUpdateMessage;
import io.aslan.notificationservice.service.AllowanceNotificationService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AllowanceUpdateListener {

    private static final Logger log = LoggerFactory.getLogger(AllowanceUpdateListener.class);

    private final AllowanceNotificationService allowanceNotificationService;
    private final ObjectMapper objectMapper;

    public AllowanceUpdateListener(AllowanceNotificationService allowanceNotificationService, ObjectMapper objectMapper) {
        this.allowanceNotificationService = allowanceNotificationService;
        this.objectMapper = new ObjectMapper();
    }

    @SqsListener("${notification.service.queue.url}")
    public void processAllowanceUpdateMessage(@Payload String messageBody, @Header("id") String messageIdHeader) throws JsonProcessingException {
            log.info("Received raw message body: {}", messageBody);

            AllowanceUpdateMessage messagePayload = objectMapper.readValue(messageBody, AllowanceUpdateMessage.class);
            UUID messageId = UUID.fromString(messageIdHeader);
            allowanceNotificationService.handleAllowanceChange(messageId, messagePayload);

            log.info("Processed AllowanceUpdateMessage with payload: {}", messagePayload);

    }
}

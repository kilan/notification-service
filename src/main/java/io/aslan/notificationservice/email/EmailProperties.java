package io.aslan.notificationservice.email;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "email.allowance.change")
public class EmailProperties {
    private String subject;
    private String messageHappy;
    private String messageCalm;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessageHappy() {
        return messageHappy;
    }

    public void setMessageHappy(String messageHappy) {
        this.messageHappy = messageHappy;
    }

    public String getMessageCalm() {
        return messageCalm;
    }

    public void setMessageCalm(String messageCalm) {
        this.messageCalm = messageCalm;
    }
}

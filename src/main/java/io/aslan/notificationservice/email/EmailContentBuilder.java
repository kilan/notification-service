package io.aslan.notificationservice.email;

import io.aslan.notificationservice.domain.entity.EmailContent;
import org.springframework.stereotype.Component;

@Component
public class EmailContentBuilder {

    private final EmailProperties emailProperties;

    public EmailContentBuilder(EmailProperties emailProperties) {
        this.emailProperties = emailProperties;
    }

    public EmailContent buildAllowanceChangeEmailBody(String to,
                                                      String firstName,
                                                      String lastName,
                                                      String imageUrl,
                                                      boolean isPositiveChange) {

        String bodyMessage = isPositiveChange ? emailProperties.getMessageHappy() : emailProperties.getMessageCalm();

        String body = String.format(
                "<html>" +
                        "<body>" +
                        "<h1>Hello, %s %s!</h1>" +
                        "<p>%s</p>" +
                        "<img src='%s'/>" +
                        "</body>" +
                        "</html>",
                firstName, lastName, bodyMessage, imageUrl
        );


        return new EmailContent(to, emailProperties.getSubject(), body);
    }
}

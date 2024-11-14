#!/bin/bash

export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/testdb
export PEXELS_API_KEY=5atfPzzmUGbpYAustsiGaDGb5SE2KOStrAV8fgPaGs5GH6f5Y85Sddbz
export SPRING_DATASOURCE_USERNAME=testuser
export SPRING_DATASOURCE_PASSWORD=testpassword
export SPRING_JPA_HIBERNATE_DDL_AUTO=update
export SPRING_CLOUD_AWS_REGION_STATIC=us-east-1
export SPRING_CLOUD_AWS_CREDENTIALS_ACCESS_KEY=test
export SPRING_CLOUD_AWS_CREDENTIALS_SECRET_KEY=test
export SPRING_CLOUD_AWS_SQS_ENDPOINT=http://localhost:4566
export SPRING_CLOUD_AWS_SES_ENDPOINT=http://localhost:4566
export SOURCE_EMAIL=notifications@email.com
export NOTIFICATION_SERVICE_QUEUE_URL=http://localhost:4566/000000000000/notification-service-queue

export SERVER_PORT=8081

mvn spring-boot:run

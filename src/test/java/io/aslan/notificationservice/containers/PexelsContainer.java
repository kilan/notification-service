package io.aslan.notificationservice.containers;

import org.testcontainers.containers.GenericContainer;

import java.time.Duration;

public class PexelsContainer extends GenericContainer<PexelsContainer> {

    public PexelsContainer() {
        super("rodolpheche/wiremock:2.27.1");
        withStartupTimeout(Duration.ofMinutes(2));
        withExposedPorts(8080);
    }

    public String getBaseUrl() {
        return this.getHost() + ":" + this.getMappedPort(8080);
    }

}

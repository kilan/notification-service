package io.aslan.notificationservice.client;

import java.util.List;

public record PexelsSearchResponse(List<Photos> photos) {

    public record Photos(Src src) {}

    public record Src(String medium) {}
}

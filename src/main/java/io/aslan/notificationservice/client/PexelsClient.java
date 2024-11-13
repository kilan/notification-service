package io.aslan.notificationservice.client;

import io.aslan.notificationservice.client.PexelsSearchResponse.Photos;
import io.aslan.notificationservice.client.PexelsSearchResponse.Src;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class PexelsClient {

    private final String baseUrl;
    private final String apiKey;
    private final RestTemplate restTemplate;

    public PexelsClient(@Value("${pexels.baseUrl}") String baseUrl,
                        @Value("${pexels.api.key}") String apiKey,
                        RestTemplate restTemplate) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.restTemplate = restTemplate;
    }

    public List<String> getImagesForCategory(String category) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .pathSegment("search")
                .queryParam("query", category)
                .queryParam("per_page", 5)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<PexelsSearchResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, PexelsSearchResponse.class);
        PexelsSearchResponse pexelsSearchResponse = response.getBody();

        return pexelsSearchResponse.photos().stream()
                .map(Photos::src)
                .map(Src::medium)
                .toList();
    }

}

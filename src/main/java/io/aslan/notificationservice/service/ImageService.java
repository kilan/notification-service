package io.aslan.notificationservice.service;

import io.aslan.notificationservice.client.PexelsClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class ImageService {

    private final PexelsClient pexelsClient;

    public ImageService(PexelsClient pexelsClient) {
        this.pexelsClient = pexelsClient;
    }

    public String getImageUrl(String keyword) {
        List<String> images = pexelsClient.getImagesForCategory(keyword);
        int randomIndex = new Random().nextInt(images.size());
        return images.get(randomIndex);
    }

}

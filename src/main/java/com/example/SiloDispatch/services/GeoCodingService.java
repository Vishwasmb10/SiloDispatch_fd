package com.example.SiloDispatch.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
public class GeoCodingService {

    @Value("${google.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public double[] getLatLonFromAddress(String addressOrLink) throws Exception {
        String extractedAddress = extractAddressFromLink(addressOrLink);

        URI uri = UriComponentsBuilder
                .fromUriString("https://maps.googleapis.com/maps/api/geocode/json")
                .queryParam("address", extractedAddress)
                .queryParam("key", apiKey)
                .build()
                .toUri();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = objectMapper.readTree(response.body());

        if (!root.has("results") || root.get("results").isEmpty()) {
            throw new RuntimeException("No geocoding results for: " + extractedAddress);
        }

        JsonNode location = root
                .get("results")
                .get(0)
                .get("geometry")
                .get("location");

        double lat = location.get("lat").asDouble();
        double lon = location.get("lng").asDouble();
        System.out.println(extractedAddress + " " + lat + " " + lon);
        return new double[]{lat, lon};
    }

    /**
     * Extracts address part if a full Google Maps link is provided.
     */
    private String extractAddressFromLink(String input) {
        if (input == null) return "";

        // Handle "https://www.google.com/maps/place/..." style links
        if (input.contains("google.com/maps/place/")) {
            input = input.substring(input.indexOf("/place/") + 7);
            input = input.replaceAll("/.*", ""); // remove anything after next /
            input = input.replace('+', ' ');
        }

        return input;
    }
}

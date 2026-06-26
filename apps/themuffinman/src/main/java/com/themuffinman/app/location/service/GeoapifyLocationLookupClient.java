package com.themuffinman.app.location.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.themuffinman.app.config.LocationProviderProperties;
import com.themuffinman.app.location.dto.LocationLookupCandidateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GeoapifyLocationLookupClient implements LocationLookupClient {
    private final LocationProviderProperties properties;
    private final RestClient restClient = RestClient.create();

    @Override
    public boolean isConfigured() {
        return "geoapify".equalsIgnoreCase(properties.getProvider())
                && properties.getGeoapifyApiKey() != null
                && !properties.getGeoapifyApiKey().isBlank();
    }

    @Override
    public String providerName() {
        return "geoapify";
    }

    @Override
    public List<LocationLookupCandidateDTO> lookup(String query) {
        if (!isConfigured()) {
            return List.of();
        }

        GeoapifyResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.geoapify.com")
                        .path("/v1/geocode/search")
                        .queryParam("text", query)
                        .queryParam("limit", 8)
                        .queryParam("apiKey", properties.getGeoapifyApiKey())
                        .build())
                .retrieve()
                .body(GeoapifyResponse.class);

        if (response == null || response.features == null) {
            return List.of();
        }

        return response.features.stream()
                .filter(feature -> feature.properties != null && feature.properties.lat != null && feature.properties.lon != null)
                .map(feature -> LocationLookupCandidateDTO.builder()
                        .provider(providerName())
                        .providerPlaceId(firstNonBlank(feature.properties.placeId, feature.properties.formatted))
                        .label(feature.properties.formatted)
                        .countryCode(feature.properties.countryCode)
                        .country(feature.properties.country)
                        .locality(firstNonBlank(feature.properties.city, feature.properties.town, feature.properties.village, feature.properties.suburb, feature.properties.county))
                        .postalCode(feature.properties.postcode)
                        .street(feature.properties.street)
                        .houseNumber(feature.properties.housenumber)
                        .latitude(BigDecimal.valueOf(feature.properties.lat))
                        .longitude(BigDecimal.valueOf(feature.properties.lon))
                        .resolvedAt(Instant.now())
                .build())
                .toList();
    }

    @Override
    public LocationLookupCandidateDTO reverseLookup(BigDecimal latitude, BigDecimal longitude) {
        if (!isConfigured()) {
            return null;
        }

        GeoapifyResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.geoapify.com")
                        .path("/v1/geocode/reverse")
                        .queryParam("lat", latitude)
                        .queryParam("lon", longitude)
                        .queryParam("format", "geojson")
                        .queryParam("apiKey", properties.getGeoapifyApiKey())
                        .build())
                .retrieve()
                .body(GeoapifyResponse.class);

        if (response == null || response.features == null || response.features.isEmpty()) {
            return null;
        }

        GeoapifyProperties properties = response.features.getFirst().properties;
        if (properties == null || properties.lat == null || properties.lon == null) {
            return null;
        }

        return LocationLookupCandidateDTO.builder()
                .provider(providerName())
                .providerPlaceId(firstNonBlank(properties.placeId, properties.formatted))
                .label(properties.formatted)
                .countryCode(properties.countryCode)
                .country(properties.country)
                .locality(firstNonBlank(properties.city, properties.town, properties.village, properties.suburb, properties.county))
                .postalCode(properties.postcode)
                .street(properties.street)
                .houseNumber(properties.housenumber)
                .latitude(BigDecimal.valueOf(properties.lat))
                .longitude(BigDecimal.valueOf(properties.lon))
                .resolvedAt(Instant.now())
                .build();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeoapifyResponse {
        public List<GeoapifyFeature> features;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeoapifyFeature {
        public GeoapifyProperties properties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeoapifyProperties {
        public String formatted;
        public String placeId;
        public String countryCode;
        public String country;
        public String city;
        public String town;
        public String village;
        public String suburb;
        public String county;
        public String postcode;
        public String street;
        public String housenumber;
        public Double lat;
        public Double lon;
    }
}

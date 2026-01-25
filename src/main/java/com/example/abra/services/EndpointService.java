package com.example.abra.services;

import com.example.abra.models.EndpointModel;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EndpointService {

    private final EndpointModelService endpointModelService;

    private final Map<String, AtomicInteger> roundRobinCounters =
        new ConcurrentHashMap<>();

    /**
     * Select a healthy endpoint for the given variant using round-robin load balancing
     */
    public EndpointModel selectEndpoint(String variantId) {
        List<EndpointModel> endpoints = endpointModelService.findByVariantId(variantId);
        
        List<EndpointModel> availableEndpoints = endpoints
            .stream()
            .filter(EndpointModel::isActive)
            .filter(EndpointModel::isAlive)
            .toList();

        if (availableEndpoints.isEmpty()) {
            return null;
        }

        AtomicInteger counter = roundRobinCounters.computeIfAbsent(
            variantId,
            k -> new AtomicInteger(0)
        );
        int index = counter.getAndIncrement() % availableEndpoints.size();
        return availableEndpoints.get(index);
    }

    @Scheduled(initialDelay = 60000, fixedRate = 30000)
    public void performHealthChecks() {
        log.info("Performing health checks...");
        List<EndpointModel> endpoints = endpointModelService.findAllEndpoints();

        for (EndpointModel endpoint : endpoints) {
            boolean isAlive = pingEndpoint(endpoint.getUrl());
            endpoint.setAlive(isAlive);
            if (!isAlive) {
                log.warn("Endpoint {} is not alive", endpoint.getUrl());
            }
            endpointModelService.updateEndpoint(endpoint);
        }
    }

    private boolean pingEndpoint(String urlString) {
        try {
            // Validate and fix URL if needed
            if (urlString == null || urlString.isEmpty()) {
                return false;
            }
            
            // Ensure URL is absolute (has scheme)
            if (!urlString.matches("^https?://.*")) {
                urlString = "http://" + urlString;
            }
            
            URL url = URI.create(urlString).toURL();
            HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(50);
            connection.setReadTimeout(50);
            connection.connect();

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException | IllegalArgumentException e) {
            log.debug("Failed to ping endpoint {}: {}", urlString, e.getMessage());
            return false;
        }
    }
}

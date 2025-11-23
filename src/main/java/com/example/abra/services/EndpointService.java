package com.example.abra.services;

import com.example.abra.models.EndpointModel;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.oauth2.server.servlet.OAuth2AuthorizationServerProperties.Endpoint;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EndpointService {

    private final EndpointModelService endpointModelService;

    private final Map<String, AtomicInteger> roundRobinCounters =
        new ConcurrentHashMap<>();

    public EndpointModel selectEndpoint(
        String variantId,
        List<EndpointModel> endpoints
    ) {
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

    @Scheduled(fixedRate = 30000) // Every 30 seconds
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
            URL url = new URL(urlString);
            HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            return false;
        }
    }
}

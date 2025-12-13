package com.example.abra.controller;

import com.example.abra.integration.ai.AiClientService;
import com.example.abra.integration.ai.dto.AiPredictionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/test-ai")
@RequiredArgsConstructor
public class AiTestController {

    private final AiClientService aiClientService;

    @GetMapping("/{serverId}")
    public ResponseEntity<AiPredictionResult> testPrediction(@PathVariable String serverId) {
        List<Double> mockRecentTraffic = Arrays.asList(45.2, 52.1, 48.9, 55.3, 60.7);
        AiPredictionResult prediction = aiClientService.getPredictionForServer(serverId, mockRecentTraffic);
        return ResponseEntity.ok(prediction);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        String status = String.format("AI service: %s", aiClientService.getAiServiceUrl());
        return ResponseEntity.ok(status);
    }
}

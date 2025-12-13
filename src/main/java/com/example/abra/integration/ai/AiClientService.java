package com.example.abra.integration.ai;

import com.example.abra.integration.ai.dto.AiTrafficPayload;
import com.example.abra.integration.ai.dto.AiPredictionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiClientService {

    private final RestTemplate restTemplate;

    @Value("${ABRA_AI_SERVICE_URL:http://localhost:8000}")
    private String aiServiceUrl;

    public AiPredictionResult getPredictionForServer(String serverId, List<Double> recentTraffic) {
        try {
            AiTrafficPayload request = AiTrafficPayload.builder()
                    .serverId(serverId)
                    .recentTraffic(recentTraffic)
                    .build();

            String predictEndpoint = aiServiceUrl + "/predict";
            
            AiPredictionResult result = restTemplate.postForObject(
                    predictEndpoint,
                    request,
                    AiPredictionResult.class
            );

            if (result != null) {
                log.info("{}: {} (load={})", serverId, result.getActionSuggested(), result.getPredictedLoad());
                return result;
            }

            log.warn("Null response from AI service for {}", serverId);
            return createFallbackResult(serverId);

        } catch (RestClientException e) {
            log.error("AI service unavailable: {}", e.getMessage());
            return createFallbackResult(serverId);

        } catch (Exception e) {
            log.error("Error requesting prediction for {}: {}", serverId, e.getMessage());
            return createFallbackResult(serverId);
        }
    }

    private AiPredictionResult createFallbackResult(String serverId) {
        return AiPredictionResult.builder()
                .serverId(serverId)
                .predictedLoad(0.0)
                .actionSuggested("MAINTAIN")
                .modelVersion("fallback")
                .build();
    }

    public String getAiServiceUrl() {
        return aiServiceUrl;
    }
}

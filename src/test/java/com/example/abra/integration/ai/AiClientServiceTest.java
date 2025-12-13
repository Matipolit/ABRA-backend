package com.example.abra.integration.ai;

import com.example.abra.integration.ai.dto.AiPredictionResult;
import com.example.abra.integration.ai.dto.AiTrafficPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AiClientService
 */
@ExtendWith(MockitoExtension.class)
class AiClientServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AiClientService aiClientService;

    private static final String TEST_SERVER_ID = "test-server-001";
    private static final String AI_SERVICE_URL = "http://localhost:8000";
    private static final List<Double> MOCK_TRAFFIC = Arrays.asList(45.0, 50.0, 55.0, 60.0, 65.0);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiClientService, "aiServiceUrl", AI_SERVICE_URL);
    }

    @Test
    void testGetPredictionForServer_Success() {
        // Given
        AiPredictionResult mockResult = AiPredictionResult.builder()
                .serverId(TEST_SERVER_ID)
                .predictedLoad(70.5)
                .actionSuggested("SCALE_UP")
                .modelVersion("1.0.0")
                .build();

        when(restTemplate.postForObject(
                eq(AI_SERVICE_URL + "/predict"),
                any(AiTrafficPayload.class),
                eq(AiPredictionResult.class)
        )).thenReturn(mockResult);

        // When
        AiPredictionResult result = aiClientService.getPredictionForServer(TEST_SERVER_ID, MOCK_TRAFFIC);

        // Then
        assertNotNull(result);
        assertEquals(TEST_SERVER_ID, result.getServerId());
        assertEquals(70.5, result.getPredictedLoad());
        assertEquals("SCALE_UP", result.getActionSuggested());
        assertEquals("1.0.0", result.getModelVersion());
    }

    @Test
    void testGetPredictionForServer_ConnectionError() {
        // Given
        when(restTemplate.postForObject(
                anyString(),
                any(AiTrafficPayload.class),
                eq(AiPredictionResult.class)
        )).thenThrow(new RestClientException("Connection refused"));

        // When
        AiPredictionResult result = aiClientService.getPredictionForServer(TEST_SERVER_ID, MOCK_TRAFFIC);

        // Then - should return fallback result
        assertNotNull(result);
        assertEquals(TEST_SERVER_ID, result.getServerId());
        assertEquals("MAINTAIN", result.getActionSuggested());
        assertEquals("fallback", result.getModelVersion());
    }

    @Test
    void testGetPredictionForServer_NullResponse() {
        // Given
        when(restTemplate.postForObject(
                anyString(),
                any(AiTrafficPayload.class),
                eq(AiPredictionResult.class)
        )).thenReturn(null);

        // When
        AiPredictionResult result = aiClientService.getPredictionForServer(TEST_SERVER_ID, MOCK_TRAFFIC);

        // Then - should return fallback result
        assertNotNull(result);
        assertEquals(TEST_SERVER_ID, result.getServerId());
        assertEquals("MAINTAIN", result.getActionSuggested());
        assertEquals("fallback", result.getModelVersion());
    }

    @Test
    void testGetAiServiceUrl() {
        // When
        String url = aiClientService.getAiServiceUrl();

        // Then
        assertEquals(AI_SERVICE_URL, url);
    }
}

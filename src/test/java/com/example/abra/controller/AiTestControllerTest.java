package com.example.abra.controller;

import com.example.abra.config.WebConfig;
import com.example.abra.filters.AbraRoutingFilter;
import com.example.abra.integration.ai.AiClientService;
import com.example.abra.integration.ai.dto.AiPredictionResult;
import com.example.abra.security.JwtService;
import com.example.abra.services.UserModelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AiTestController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = { AbraRoutingFilter.class, WebConfig.class }
        )
)
@WithMockUser
class AiTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiClientService aiClientService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserModelService userModelService;

    private static final String TEST_SERVER_ID = "test-server-01";

    @Test
    void testPrediction_Success() throws Exception {
        // Given
        AiPredictionResult mockResult = AiPredictionResult.builder()
                .serverId(TEST_SERVER_ID)
                .predictedLoad(72.5)
                .actionSuggested("SCALE_UP")
                .modelVersion("1.0.0")
                .build();

        when(aiClientService.getPredictionForServer(anyString(), anyList()))
                .thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/test-ai/" + TEST_SERVER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.server_id").value(TEST_SERVER_ID))
                .andExpect(jsonPath("$.predicted_load").value(72.5))
                .andExpect(jsonPath("$.action_suggested").value("SCALE_UP"))
                .andExpect(jsonPath("$.model_version").value("1.0.0"));
    }

    @Test
    void testHealthCheck() throws Exception {
        // Given
        when(aiClientService.getAiServiceUrl())
                .thenReturn("http://localhost:8000");

        // When & Then
        mockMvc.perform(get("/api/test-ai/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("AI Service URL: http://localhost:8000"));
    }
}

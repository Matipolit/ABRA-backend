package com.example.abra.integration.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiPredictionResult {
    @JsonProperty("server_id")
    private String serverId;

    @JsonProperty("predicted_load")
    private Double predictedLoad;

    @JsonProperty("action_suggested")
    private String actionSuggested;

    @JsonProperty("model_version")
    private String modelVersion;
}

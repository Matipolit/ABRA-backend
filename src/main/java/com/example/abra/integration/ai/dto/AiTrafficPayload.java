package com.example.abra.integration.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiTrafficPayload {
    @JsonProperty("server_id")
    private String serverId;

    @JsonProperty("recent_traffic")
    private List<Double> recentTraffic;
}

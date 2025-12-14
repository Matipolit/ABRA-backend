package com.example.abra.controller;

import com.example.abra.integration.ai.AiClientService;
import com.example.abra.integration.ai.dto.AiPredictionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequestMapping("/api/test-ai")
@RequiredArgsConstructor
@EnableScheduling
public class AiTestController {

    private final AiClientService aiClientService; // Serwis do komunikacji z AI
    private static final int HISTORY_WINDOW_SIZE = 20; // ZMIEŃ TĘ WARTOŚĆ, ABY DOSTOSOWAĆ DŁUGOŚĆ HISTORII W CAŁYM KODZIE musi być zgodne z AI (app/model.py) 

    private final Map<String, AtomicInteger> currentSecondRequests = new ConcurrentHashMap<>();
    private final Map<String, LinkedList<Double>> trafficHistory = new ConcurrentHashMap<>();

    @GetMapping("/{serverId}")
    public ResponseEntity<AiPredictionResult> testPrediction(@PathVariable String serverId) {
        
        // 1. Zliczamy uderzenie
        currentSecondRequests.computeIfAbsent(serverId, k -> new AtomicInteger(0)).incrementAndGet();

        // 2. Pobieramy historię. Używamy stałej do wypełnienia zerami.
        List<Double> history = trafficHistory.computeIfAbsent(serverId, k -> 
            new LinkedList<>(Collections.nCopies(HISTORY_WINDOW_SIZE, 0.0))
        );

        // 3. Tworzymy bezpieczną kopię do wysłania
        List<Double> payload = new ArrayList<>(history);

        // 4. ZABEZPIECZENIE: Gwarantujemy, że wysyłamy dokładnie tyle, ile wynosi stała.
        // Jeśli lista jest za krótka (np. błąd inicjalizacji) - dodajemy zera.
        while (payload.size() < HISTORY_WINDOW_SIZE) {
            payload.add(0, 0.0);
        }
        // Jeśli lista jest za długa - ucinamy.
        while (payload.size() > HISTORY_WINDOW_SIZE) {
            payload.remove(0);
        }

        // 5. Pytamy AI o radę
        AiPredictionResult prediction = aiClientService.getPredictionForServer(serverId, payload);
        
        // --- REAKCJA NA DECYZJĘ AI (DODANE) ---
        String action = prediction.getActionSuggested();
        
        if ("SCALE_UP".equals(action)) {
            // Tutaj normalnie byłby kod: dockerClient.startContainer(...)
            log.warn("🚨 [ACTION] SKALOWANIE W GÓRĘ! Serwer {} jest przeciążony (Predykcja: {}). Uruchamiam dodatkowe instancje.", 
                     serverId, prediction.getPredictedLoad());
        } 
        else if ("SCALE_DOWN".equals(action)) {
            // Tutaj normalnie byłby kod: dockerClient.stopContainer(...)
            log.info("💤 [ACTION] SKALOWANIE W DÓŁ. Serwer {} ma mały ruch. Zwalniam zasoby.", serverId);
        }
        // Dla MAINTAIN nic nie robimy (żeby nie śmiecić w logach)
        // ---------------------------------------

        return ResponseEntity.ok(prediction);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("AI Service URL: " + aiClientService.getAiServiceUrl());
    }

    /**
     * Zadanie w tle (Timer).
     */
    @Scheduled(fixedRate = 1000)
    public void updateTrafficMetrics() {
        Set<String> allServers = new HashSet<>();
        allServers.addAll(currentSecondRequests.keySet());
        allServers.addAll(trafficHistory.keySet());

        for (String serverId : allServers) {
            AtomicInteger counter = currentSecondRequests.get(serverId);
            double requestsInLastSecond = (counter != null) ? counter.getAndSet(0) : 0.0;

            trafficHistory.compute(serverId, (k, history) -> {
                // Inicjalizacja (jeśli null) z użyciem stałej
                if (history == null) {
                    history = new LinkedList<>(Collections.nCopies(HISTORY_WINDOW_SIZE, 0.0));
                }
                
                history.add(requestsInLastSecond);
                
                // Pilnowanie rozmiaru z użyciem stałej
                while (history.size() > HISTORY_WINDOW_SIZE) {
                    history.removeFirst();
                }
                
                return history;
            });
        }
    }
}
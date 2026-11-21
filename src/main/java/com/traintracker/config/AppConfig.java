package com.traintracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/** Application-level configuration beans. */
@Configuration
public class AppConfig {

    @Value("${app.provider.base-url}")
    private String providerBaseUrl;

    @Value("${app.provider.api-key}")
    private String apiKey;

    @Value("${app.provider.poll-interval-ms:15000}")
    private long pollIntervalMs;

    @Value("${app.tracking.stale-threshold-ms:120000}")
    private long staleThresholdMs;

    @Value("${app.tracking.snap-tolerance-metres:500}")
    private double snapToleranceMetres;

    @Value("${app.tracking.history-snapshot-every:5}")
    private int historySnapshotEvery;

    @Value("${app.simulation.enabled:false}")
    private boolean simulationEnabled;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getProviderBaseUrl() { return providerBaseUrl; }
    public String getApiKey() { return apiKey; }
    public long getPollIntervalMs() { return pollIntervalMs; }
    public long getStaleThresholdMs() { return staleThresholdMs; }
    public double getSnapToleranceMetres() { return snapToleranceMetres; }
    public int getHistorySnapshotEvery() { return historySnapshotEvery; }
    public boolean isSimulationEnabled() { return simulationEnabled; }
}

package com.traintracker.service;

import com.traintracker.config.AppConfig;
import com.traintracker.dto.ProviderTelemetry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Encapsulates communication with the external live-data provider (RailRadar).
 * Replace or extend this class to swap providers without changing tracking logic.
 */
@Service
public class LiveDataProviderService {

    private static final Logger log = LoggerFactory.getLogger(LiveDataProviderService.class);

    private final RestTemplate restTemplate;
    private final AppConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LiveDataProviderService(RestTemplate restTemplate, AppConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    /**
     * Fetches live running status for a given train number.
     * Returns null if provider is unavailable or data is unusable.
     */
    public ProviderTelemetry fetchLiveStatus(String trainNumber) {
        try {
            String baseUrl = config.getProviderBaseUrl();
            String url;
            if (baseUrl.contains("/v1") || baseUrl.contains("railradar.in")) {
                url = baseUrl.replaceAll("/+$", "") + "/trains/" + trainNumber + "/live";
            } else {
                String sep = baseUrl.contains("?") ? "&" : "?";
                url = baseUrl + "/api/live-train-status" + sep + "train=" + trainNumber;
            }

            HttpHeaders headers = new HttpHeaders();
            String apiKey = config.getApiKey();
            if (apiKey != null && !apiKey.isBlank() && !apiKey.equals("YOUR_API_KEY_HERE")) {
                headers.set("Authorization", "Bearer " + apiKey);
            }
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseTelemetry(response.getBody(), trainNumber);
            }
        } catch (Exception e) {
            log.warn("Provider fetch failed for train {}: {}", trainNumber, e.getMessage());
        }
        return null;
    }

    /**
     * Fetches GeoJSON route geometry for a given train number.
     * Returns null on failure.
     */
    public String fetchRouteGeometry(String trainNumber) {
        try {
            String baseUrl = config.getProviderBaseUrl();
            String url;
            if (baseUrl.contains("/v1") || baseUrl.contains("railradar.in")) {
                url = baseUrl.replaceAll("/+$", "") + "/trains/" + trainNumber + "/route?format=geojson&stops=true";
            } else {
                url = baseUrl + "/api/train-route-geometry?train=" + trainNumber;
            }

            HttpHeaders headers = new HttpHeaders();
            String apiKey = config.getApiKey();
            if (apiKey != null && !apiKey.isBlank() && !apiKey.equals("YOUR_API_KEY_HERE")) {
                headers.set("Authorization", "Bearer " + apiKey);
            }

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.has("data")) {
                    JsonNode data = root.path("data");
                    if (data.has("geojson") && data.has("stops") && data.path("stops").isArray()) {
                        com.fasterxml.jackson.databind.node.ObjectNode fc = objectMapper.createObjectNode();
                        fc.put("type", "FeatureCollection");
                        com.fasterxml.jackson.databind.node.ArrayNode features = fc.putArray("features");
                        features.add(data.path("geojson"));
                        for (JsonNode stop : data.path("stops")) {
                            double sLat = stop.path("lat").asDouble();
                            double sLng = stop.path("lng").asDouble();
                            if (sLat != 0 && sLng != 0) {
                                com.fasterxml.jackson.databind.node.ObjectNode pf = objectMapper.createObjectNode();
                                pf.put("type", "Feature");
                                com.fasterxml.jackson.databind.node.ObjectNode geom = pf.putObject("geometry");
                                geom.put("type", "Point");
                                com.fasterxml.jackson.databind.node.ArrayNode coords = geom.putArray("coordinates");
                                coords.add(sLng);
                                coords.add(sLat);
                                com.fasterxml.jackson.databind.node.ObjectNode props = pf.putObject("properties");
                                props.put("stationName", stop.path("name").asText("Station"));
                                props.put("stationCode", stop.path("code").asText(""));
                                features.add(pf);
                            }
                        }
                        return objectMapper.writeValueAsString(fc);
                    } else if (data.has("geojson")) {
                        return objectMapper.writeValueAsString(data.path("geojson"));
                    }
                    return objectMapper.writeValueAsString(data);
                }
                return response.getBody();
            }
        } catch (Exception e) {
            log.warn("Route geometry fetch failed for train {}: {}", trainNumber, e.getMessage());
        }
        return null;
    }

    private ProviderTelemetry parseTelemetry(String json, String trainNumber) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode dataNode = root.has("data") ? root.path("data") : root;
            JsonNode locNode = dataNode.has("currentLocation") ? dataNode.path("currentLocation") : dataNode;

            ProviderTelemetry t = new ProviderTelemetry();
            t.setTrainNumber(trainNumber);

            if (locNode.has("latitude")) {
                t.setLatitude(locNode.path("latitude").asDouble(Double.NaN));
            } else if (locNode.has("lat")) {
                t.setLatitude(locNode.path("lat").asDouble(Double.NaN));
            }

            if (locNode.has("longitude")) {
                t.setLongitude(locNode.path("longitude").asDouble(Double.NaN));
            } else if (locNode.has("lng")) {
                t.setLongitude(locNode.path("lng").asDouble(Double.NaN));
            }

            if (locNode.has("speedKmh")) {
                t.setSpeedKmh(locNode.path("speedKmh").asDouble());
            } else if (locNode.has("speed")) {
                t.setSpeedKmh(locNode.path("speed").asDouble());
            }

            if (locNode.has("bearingDegrees")) {
                t.setBearing(locNode.path("bearingDegrees").asDouble());
            } else if (locNode.has("bearing")) {
                t.setBearing(locNode.path("bearing").asDouble());
            }

            if (locNode.has("stationName") || locNode.has("stationCode")) {
                String stn = locNode.path("stationName").asText("");
                String code = locNode.path("stationCode").asText("");
                String stStatus = locNode.path("status").asText("");
                String prefix = stStatus.isBlank() ? "" : (stStatus.substring(0, 1).toUpperCase() + stStatus.substring(1) + " ");
                t.setCurrentStation(prefix + stn + (code.isBlank() ? "" : " (" + code + ")"));
            }
            if (dataNode.has("nextHalt")) {
                JsonNode nh = dataNode.path("nextHalt");
                t.setNextHalt(nh.path("stationName").asText("") + " (" + nh.path("stationCode").asText("") + ")");
            }

            if (locNode.has("distanceFromOriginKm") && dataNode.path("train").has("distance")) {
                double distOrigin = locNode.path("distanceFromOriginKm").asDouble();
                double totalDist = dataNode.path("train").path("distance").asDouble();
                t.setDistanceFromOriginKm(distOrigin);
                t.setTotalDistanceKm(totalDist);
                if (totalDist > 0 && distOrigin > 0) {
                    t.setSegmentProgress(distOrigin / totalDist);
                } else if (distOrigin == 0.0) {
                    t.setSegmentProgress(0.0);
                }
            } else if (locNode.has("segmentProgress")) {
                double sp = locNode.path("segmentProgress").asDouble();
                if (sp >= 0) {
                    t.setSegmentProgress(sp);
                }
            }

            if (t.getLatitude() == null || Double.isNaN(t.getLatitude())) {
                if (dataNode.path("train").path("source").has("lat")) {
                    t.setLatitude(dataNode.path("train").path("source").path("lat").asDouble());
                    t.setLongitude(dataNode.path("train").path("source").path("lng").asDouble());
                }
            }

            if (dataNode.has("delayMinutes")) {
                t.setDelayMinutes(dataNode.path("delayMinutes").asInt());
            } else if (dataNode.has("delay")) {
                t.setDelayMinutes(dataNode.path("delay").asInt());
            }

            if (dataNode.has("status")) {
                t.setStatus(dataNode.path("status").asText("UNKNOWN").toUpperCase());
            } else {
                t.setStatus("RUNNING");
            }

            long timestamp = System.currentTimeMillis();
            if (root.has("meta") && root.path("meta").has("timestamp")) {
                try {
                    timestamp = java.time.Instant.parse(root.path("meta").path("timestamp").asText()).toEpochMilli();
                } catch (Exception ignored) {}
            } else if (dataNode.has("timestamp")) {
                timestamp = dataNode.path("timestamp").asLong(timestamp);
            }
            t.setTimestampEpochMs(timestamp);

            return t;
        } catch (Exception e) {
            log.error("Failed to parse telemetry JSON: {}", e.getMessage());
            return null;
        }
    }
}

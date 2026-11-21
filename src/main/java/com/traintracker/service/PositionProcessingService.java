package com.traintracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Validates and processes telemetry to produce a coherent display position.
 *
 * Pipeline:
 *  1. Validate coordinate bounds and timestamp freshness.
 *  2. If segment-progress is provided, interpolate position along route.
 *  3. Optionally snap position to nearest route point (within tolerance).
 */
@Service
public class PositionProcessingService {

    private static final Logger log = LoggerFactory.getLogger(PositionProcessingService.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    /** Earth radius in metres. */
    private static final double EARTH_RADIUS_M = 6_371_000.0;

    /**
     * Validates that latitude and longitude are within legal bounds.
     */
    public boolean isValidCoordinate(double lat, double lon) {
        return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180
                && !Double.isNaN(lat) && !Double.isNaN(lon);
    }

    /**
     * Normalises bearing to [0, 360).
     */
    public double normaliseBearing(double bearing) {
        return ((bearing % 360) + 360) % 360;
    }

    public JsonNode extractCoordinatesNode(JsonNode root) {
        if (root.has("features") && root.path("features").isArray()) {
            for (JsonNode f : root.path("features")) {
                if ("LineString".equalsIgnoreCase(f.path("geometry").path("type").asText())) {
                    return f.path("geometry").path("coordinates");
                }
            }
        }
        if (root.has("geometry") && root.path("geometry").has("coordinates")) {
            return root.path("geometry").path("coordinates");
        }
        if (root.has("geojson")) {
            return extractCoordinatesNode(root.path("geojson"));
        }
        return root.path("coordinates");
    }

    /**
     * Given a GeoJSON LineString (or Feature) and an index + fractional progress [0,1],
     * returns [lat, lon] of the interpolated point.
     *
     * @param geoJson    GeoJSON LineString/Feature as a String
     * @param fromIndex  index of the "from" coordinate in the coordinates array
     * @param progress   fraction [0.0, 1.0] along the segment
     */
    public double[] interpolateOnRoute(String geoJson, int fromIndex, double progress) {
        try {
            JsonNode root = mapper.readTree(geoJson);
            JsonNode coords = extractCoordinatesNode(root);
            if (coords.isMissingNode() || coords.size() <= fromIndex + 1) {
                return null;
            }
            JsonNode from = coords.get(fromIndex);
            JsonNode to   = coords.get(fromIndex + 1);
            double lonA = from.get(0).asDouble(), latA = from.get(1).asDouble();
            double lonB = to.get(0).asDouble(),   latB = to.get(1).asDouble();
            double clampedProgress = Math.max(0.0, Math.min(1.0, progress));
            double lat = latA + clampedProgress * (latB - latA);
            double lon = lonA + clampedProgress * (lonB - lonA);
            return new double[]{lat, lon};
        } catch (Exception e) {
            log.error("Interpolation failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Given an overall fractional progress along the complete track (0.0 to 1.0),
     * computes the exact [lat, lon] along the route geometry.
     */
    public double[] interpolateAlongEntireRoute(String geoJson, double overallFraction) {
        try {
            JsonNode root = mapper.readTree(geoJson);
            JsonNode coords = extractCoordinatesNode(root);
            if (coords.isMissingNode() || coords.size() < 2) return null;

            double clampedFraction = Math.max(0.0, Math.min(1.0, overallFraction));
            double totalDist = 0.0;
            double[] segDists = new double[coords.size() - 1];
            for (int i = 0; i < coords.size() - 1; i++) {
                JsonNode p1 = coords.get(i);
                JsonNode p2 = coords.get(i + 1);
                segDists[i] = haversineMetres(p1.get(1).asDouble(), p1.get(0).asDouble(),
                                             p2.get(1).asDouble(), p2.get(0).asDouble());
                totalDist += segDists[i];
            }

            if (totalDist == 0) {
                return new double[]{coords.get(0).get(1).asDouble(), coords.get(0).get(0).asDouble()};
            }

            double targetDist = totalDist * clampedFraction;
            double covered = 0.0;
            for (int i = 0; i < segDists.length; i++) {
                if (covered + segDists[i] >= targetDist || i == segDists.length - 1) {
                    double remaining = targetDist - covered;
                    double segProgress = segDists[i] > 0 ? (remaining / segDists[i]) : 0.0;
                    JsonNode p1 = coords.get(i);
                    JsonNode p2 = coords.get(i + 1);
                    double lat = p1.get(1).asDouble() + segProgress * (p2.get(1).asDouble() - p1.get(1).asDouble());
                    double lon = p1.get(0).asDouble() + segProgress * (p2.get(0).asDouble() - p1.get(0).asDouble());
                    return new double[]{lat, lon};
                }
                covered += segDists[i];
            }
        } catch (Exception e) {
            log.warn("Interpolate along entire route failed: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Snaps [lat, lon] to the nearest point on the GeoJSON LineString
     * if the distance exceeds toleranceMetres.
     * Returns the (possibly unchanged) [lat, lon].
     */
    public double[] snapToRoute(String geoJson, double lat, double lon, double toleranceMetres) {
        try {
            JsonNode root = mapper.readTree(geoJson);
            JsonNode coords = extractCoordinatesNode(root);
            if (coords.isMissingNode() || coords.size() < 2) return new double[]{lat, lon};

            double bestLat = lat, bestLon = lon, bestDist = Double.MAX_VALUE;
            for (int i = 0; i < coords.size() - 1; i++) {
                JsonNode p1 = coords.get(i), p2 = coords.get(i + 1);
                double[] closest = closestPointOnSegment(
                    lat, lon,
                    p1.get(1).asDouble(), p1.get(0).asDouble(),
                    p2.get(1).asDouble(), p2.get(0).asDouble()
                );
                double dist = haversineMetres(lat, lon, closest[0], closest[1]);
                if (dist < bestDist) { bestDist = dist; bestLat = closest[0]; bestLon = closest[1]; }
            }
            if (bestDist <= toleranceMetres) {
                return new double[]{bestLat, bestLon};
            }
        } catch (Exception e) {
            log.warn("Snap to route failed: {}", e.getMessage());
        }
        return new double[]{lat, lon};
    }

    // ── Geometry helpers ──────────────────────────────────────────────────────

    private double[] closestPointOnSegment(double pLat, double pLon,
                                            double aLat, double aLon,
                                            double bLat, double bLon) {
        double dx = bLon - aLon, dy = bLat - aLat;
        if (dx == 0 && dy == 0) return new double[]{aLat, aLon};
        double t = ((pLon - aLon) * dx + (pLat - aLat) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        return new double[]{aLat + t * dy, aLon + t * dx};
    }

    public double haversineMetres(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_M * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}

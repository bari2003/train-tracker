package com.traintracker.model;

import jakarta.persistence.*;
import java.util.List;

/**
 * Stores route geometry (GeoJSON LineString) associated with a train.
 */
@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long routeId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false, unique = true)
    private Train train;

    /** Valid GeoJSON LineString; validated before use for route visualisation. */
    @Lob
    @Column(name = "geometry_geojson", columnDefinition = "LONGTEXT")
    private String geometryGeoJson;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "version")
    private Integer version = 1;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNo ASC")
    private List<RouteStop> stops;

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }

    public Train getTrain() { return train; }
    public void setTrain(Train train) { this.train = train; }

    public String getGeometryGeoJson() { return geometryGeoJson; }
    public void setGeometryGeoJson(String geometryGeoJson) { this.geometryGeoJson = geometryGeoJson; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public List<RouteStop> getStops() { return stops; }
    public void setStops(List<RouteStop> stops) { this.stops = stops; }
}

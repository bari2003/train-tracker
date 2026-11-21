package com.traintracker.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Stores the latest processed state of a tracked train.
 * Latitude must be -90..+90; longitude -180..+180.
 * Speed NULL = unavailable (never fabricated).
 * Status uses: RUNNING | AT_STATION | SCHEDULED | COMPLETED | CANCELLED | DIVERTED | STALE | UNKNOWN
 */
@Entity
@Table(name = "live_train_states")
public class LiveTrainState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stateId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false, unique = true)
    private Train train;

    private Double latitude;
    private Double longitude;

    @Column(name = "speed_kmh")
    private Double speedKmh;        // NULL when unavailable

    @Column(name = "bearing")
    private Double bearing;         // normalised 0-360

    @Column(name = "delay_minutes")
    private Integer delayMinutes;

    @Column(length = 20)
    private String status;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    @Column(name = "position_source", length = 30)
    private String positionSource;  // e.g. PROVIDER, INTERPOLATED, SNAPPED, SIMULATED

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getStateId() { return stateId; }
    public void setStateId(Long stateId) { this.stateId = stateId; }

    public Train getTrain() { return train; }
    public void setTrain(Train train) { this.train = train; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getSpeedKmh() { return speedKmh; }
    public void setSpeedKmh(Double speedKmh) { this.speedKmh = speedKmh; }

    public Double getBearing() { return bearing; }
    public void setBearing(Double bearing) { this.bearing = bearing; }

    public Integer getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(Integer delayMinutes) { this.delayMinutes = delayMinutes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getPositionSource() { return positionSource; }
    public void setPositionSource(String positionSource) { this.positionSource = positionSource; }
}

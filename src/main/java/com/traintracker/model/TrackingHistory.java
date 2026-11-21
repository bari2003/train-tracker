package com.traintracker.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Optional historical position snapshots for testing and analysis.
 * Not every animation frame is persisted — sampled at a controlled interval.
 */
@Entity
@Table(name = "tracking_history", indexes = {
    @Index(name = "idx_history_train", columnList = "train_id"),
    @Index(name = "idx_history_timestamp", columnList = "timestamp")
})
public class TrackingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    private Double latitude;
    private Double longitude;

    @Column(name = "speed_kmh")
    private Double speedKmh;

    @Column(nullable = false)
    private Instant timestamp;

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getHistoryId() { return historyId; }
    public void setHistoryId(Long historyId) { this.historyId = historyId; }

    public Train getTrain() { return train; }
    public void setTrain(Train train) { this.train = train; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getSpeedKmh() { return speedKmh; }
    public void setSpeedKmh(Double speedKmh) { this.speedKmh = speedKmh; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}

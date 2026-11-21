package com.traintracker.dto;

import java.time.Instant;

/** Payload sent to frontend via WebSocket on every processed update. */
public class TrainStateMessage {

    private Long trainId;
    private String trainNumber;
    private String trainName;
    private String sourceStation;
    private String destinationStation;
    private Double latitude;
    private Double longitude;
    private Double speedKmh;
    private Double bearing;
    private Integer delayMinutes;
    private String status;
    private Instant lastUpdated;
    private String positionSource;
    private boolean stale;
    private String currentStation;
    private String nextHalt;
    private Double distanceFromOriginKm;
    private Double totalDistanceKm;

    public String getCurrentStation() { return currentStation; }
    public void setCurrentStation(String currentStation) { this.currentStation = currentStation; }

    public String getNextHalt() { return nextHalt; }
    public void setNextHalt(String nextHalt) { this.nextHalt = nextHalt; }

    public Double getDistanceFromOriginKm() { return distanceFromOriginKm; }
    public void setDistanceFromOriginKm(Double distanceFromOriginKm) { this.distanceFromOriginKm = distanceFromOriginKm; }

    public Double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(Double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }

    // ── Builder-style setters ─────────────────────────────────────────────────

    public Long getTrainId() { return trainId; }
    public void setTrainId(Long trainId) { this.trainId = trainId; }

    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }

    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { this.trainName = trainName; }

    public String getSourceStation() { return sourceStation; }
    public void setSourceStation(String sourceStation) { this.sourceStation = sourceStation; }

    public String getDestinationStation() { return destinationStation; }
    public void setDestinationStation(String destinationStation) { this.destinationStation = destinationStation; }

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

    public boolean isStale() { return stale; }
    public void setStale(boolean stale) { this.stale = stale; }
}

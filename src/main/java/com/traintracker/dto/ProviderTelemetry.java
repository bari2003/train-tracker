package com.traintracker.dto;

/** Raw telemetry received from the external live-data provider. */
public class ProviderTelemetry {
    private String trainNumber;
    private Double latitude;
    private Double longitude;
    private Double speedKmh;
    private Double bearing;
    private Integer delayMinutes;
    private String status;
    private Long timestampEpochMs;
    // Optional segment progress (0.0–1.0) between two waypoints
    private Double segmentProgress;
    private Integer segmentFromIndex;
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

    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }

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

    public Long getTimestampEpochMs() { return timestampEpochMs; }
    public void setTimestampEpochMs(Long timestampEpochMs) { this.timestampEpochMs = timestampEpochMs; }

    public Double getSegmentProgress() { return segmentProgress; }
    public void setSegmentProgress(Double segmentProgress) { this.segmentProgress = segmentProgress; }

    public Integer getSegmentFromIndex() { return segmentFromIndex; }
    public void setSegmentFromIndex(Integer segmentFromIndex) { this.segmentFromIndex = segmentFromIndex; }
}

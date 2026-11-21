package com.traintracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

/**
 * Represents a train supported by the application.
 * train_number is unique within the supported reference data.
 */
@Entity
@Table(name = "trains", indexes = {
    @Index(name = "idx_train_number", columnList = "train_number")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trainId;

    @Column(name = "train_number", nullable = false, unique = true, length = 10)
    private String trainNumber;

    @Column(name = "train_name", nullable = false, length = 100)
    private String trainName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_station_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Station sourceStation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination_station_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Station destinationStation;

    @Column(name = "status", length = 20)
    private String status; // RUNNING, AT_STATION, SCHEDULED, COMPLETED, CANCELLED, DIVERTED, UNKNOWN

    @JsonIgnore
    @OneToOne(mappedBy = "train", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Route route;

    @JsonIgnore
    @OneToOne(mappedBy = "train", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private LiveTrainState liveTrainState;

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getTrainId() { return trainId; }
    public void setTrainId(Long trainId) { this.trainId = trainId; }

    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }

    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { this.trainName = trainName; }

    public Station getSourceStation() { return sourceStation; }
    public void setSourceStation(Station sourceStation) { this.sourceStation = sourceStation; }

    public Station getDestinationStation() { return destinationStation; }
    public void setDestinationStation(Station destinationStation) { this.destinationStation = destinationStation; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }

    public LiveTrainState getLiveTrainState() { return liveTrainState; }
    public void setLiveTrainState(LiveTrainState liveTrainState) { this.liveTrainState = liveTrainState; }
}

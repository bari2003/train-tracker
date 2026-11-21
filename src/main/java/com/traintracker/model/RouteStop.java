package com.traintracker.model;

import jakarta.persistence.*;
import java.time.LocalTime;

/**
 * Associates a station with a position in a train route.
 * sequence_no must be unique per route.
 */
@Entity
@Table(name = "route_stops", indexes = {
    @Index(name = "idx_route_stop_route", columnList = "route_id")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uq_route_sequence", columnNames = {"route_id", "sequence_no"})
})
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stopId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(name = "sequence_no", nullable = false)
    private Integer sequenceNo;

    @Column(name = "scheduled_arrival")
    private LocalTime scheduledArrival;

    @Column(name = "scheduled_departure")
    private LocalTime scheduledDeparture;

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getStopId() { return stopId; }
    public void setStopId(Long stopId) { this.stopId = stopId; }

    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }

    public Station getStation() { return station; }
    public void setStation(Station station) { this.station = station; }

    public Integer getSequenceNo() { return sequenceNo; }
    public void setSequenceNo(Integer sequenceNo) { this.sequenceNo = sequenceNo; }

    public LocalTime getScheduledArrival() { return scheduledArrival; }
    public void setScheduledArrival(LocalTime scheduledArrival) { this.scheduledArrival = scheduledArrival; }

    public LocalTime getScheduledDeparture() { return scheduledDeparture; }
    public void setScheduledDeparture(LocalTime scheduledDeparture) { this.scheduledDeparture = scheduledDeparture; }
}

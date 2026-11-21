package com.traintracker.config;

import com.traintracker.model.Route;
import com.traintracker.model.Station;
import com.traintracker.model.Train;
import com.traintracker.repository.RouteRepository;
import com.traintracker.repository.StationRepository;
import com.traintracker.repository.TrainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DatabaseSeeder {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final StationRepository stationRepo;
    private final TrainRepository trainRepo;
    private final RouteRepository routeRepo;

    public DatabaseSeeder(StationRepository stationRepo, TrainRepository trainRepo, RouteRepository routeRepo) {
        this.stationRepo = stationRepo;
        this.trainRepo = trainRepo;
        this.routeRepo = routeRepo;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (stationRepo.count() > 0) {
            log.info("Database already seeded, skipping.");
            return;
        }

        log.info("Seeding initial railway stations, trains, and routes...");

        Map<String, Station> stations = new HashMap<>();
        stations.put("NDLS", createStation("NDLS", "New Delhi", 28.6428, 77.2197));
        stations.put("BCT",  createStation("BCT",  "Mumbai Central", 18.9696, 72.8197));
        stations.put("MAS",  createStation("MAS",  "Chennai Central", 13.0827, 80.2707));
        stations.put("SBC",  createStation("SBC",  "Bengaluru City", 12.9784, 77.5720));
        stations.put("HWH",  createStation("HWH",  "Howrah", 22.5839, 88.3426));
        stations.put("PNBE", createStation("PNBE", "Patna Junction", 25.6103, 85.1727));
        stations.put("CNB",  createStation("CNB",  "Kanpur Central", 26.4499, 80.3319));
        stations.put("AGC",  createStation("AGC",  "Agra Cantt", 27.1591, 78.0120));
        stations.put("BPL",  createStation("BPL",  "Bhopal", 23.2599, 77.4126));
        stations.put("NGP",  createStation("NGP",  "Nagpur", 21.1458, 79.0882));

        for (Station s : stations.values()) {
            stationRepo.save(s);
        }

        // Trains
        Train t1 = createTrain("12951", "Mumbai Rajdhani Express", stations.get("NDLS"), stations.get("BCT"));
        Train t2 = createTrain("12001", "Bhopal Shatabdi Express", stations.get("NDLS"), stations.get("BPL"));
        Train t3 = createTrain("12259", "Sealdah Duronto Express", stations.get("HWH"), stations.get("NDLS"));

        t1 = trainRepo.save(t1);
        t2 = trainRepo.save(t2);
        t3 = trainRepo.save(t3);

        // Routes
        createRoute(t1, "{\"type\":\"LineString\",\"coordinates\":[[77.2197,28.6428],[78.0120,27.1591],[77.4126,23.2599],[79.0882,21.1458],[72.8197,18.9696]]}", 1388.0);
        createRoute(t2, "{\"type\":\"LineString\",\"coordinates\":[[77.2197,28.6428],[77.5020,27.8591],[78.0120,27.1591],[78.1820,26.2183],[77.4126,23.2599]]}", 707.0);
        createRoute(t3, "{\"type\":\"LineString\",\"coordinates\":[[88.3426,22.5839],[85.1727,25.6103],[80.3319,26.4499],[77.2197,28.6428]]}", 1450.0);

        log.info("Database seeding complete.");
    }

    private Station createStation(String code, String name, double lat, double lon) {
        Station s = new Station();
        s.setStationCode(code);
        s.setStationName(name);
        s.setLatitude(lat);
        s.setLongitude(lon);
        return s;
    }

    private Train createTrain(String number, String name, Station source, Station dest) {
        Train t = new Train();
        t.setTrainNumber(number);
        t.setTrainName(name);
        t.setSourceStation(source);
        t.setDestinationStation(dest);
        t.setStatus("RUNNING");
        return t;
    }

    private void createRoute(Train train, String geoJson, double distKm) {
        Route r = new Route();
        r.setTrain(train);
        r.setGeometryGeoJson(geoJson);
        r.setDistanceKm(distKm);
        r.setVersion(1);
        routeRepo.save(r);
    }
}

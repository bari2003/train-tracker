-- ============================================================
-- Real-Time Train Movement Tracker — MySQL Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS train_tracker
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE train_tracker;

-- ── Stations ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS stations (
    station_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    station_code VARCHAR(10)  NOT NULL UNIQUE,
    station_name VARCHAR(100) NOT NULL,
    latitude     DOUBLE       NOT NULL CHECK (latitude  BETWEEN -90  AND 90),
    longitude    DOUBLE       NOT NULL CHECK (longitude BETWEEN -180 AND 180),
    INDEX idx_station_code (station_code)
) ENGINE=InnoDB;

-- ── Trains ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS trains (
    train_id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_number           VARCHAR(10)  NOT NULL UNIQUE,
    train_name             VARCHAR(100) NOT NULL,
    source_station_id      BIGINT,
    destination_station_id BIGINT,
    status                 VARCHAR(20),
    INDEX idx_train_number (train_number),
    FOREIGN KEY (source_station_id)      REFERENCES stations(station_id),
    FOREIGN KEY (destination_station_id) REFERENCES stations(station_id)
) ENGINE=InnoDB;

-- ── Routes ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS routes (
    route_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_id         BIGINT NOT NULL UNIQUE,
    geometry_geojson LONGTEXT,
    distance_km      DOUBLE,
    version          INT DEFAULT 1,
    FOREIGN KEY (train_id) REFERENCES trains(train_id)
) ENGINE=InnoDB;

-- ── Route Stops ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS route_stops (
    stop_id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id             BIGINT NOT NULL,
    station_id           BIGINT NOT NULL,
    sequence_no          INT    NOT NULL,
    scheduled_arrival    TIME,
    scheduled_departure  TIME,
    UNIQUE KEY uq_route_sequence (route_id, sequence_no),
    INDEX idx_route_stop_route (route_id),
    FOREIGN KEY (route_id)   REFERENCES routes(route_id),
    FOREIGN KEY (station_id) REFERENCES stations(station_id)
) ENGINE=InnoDB;

-- ── Live Train States ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS live_train_states (
    state_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_id        BIGINT  NOT NULL UNIQUE,
    latitude        DOUBLE,
    longitude       DOUBLE,
    speed_kmh       DOUBLE,         -- NULL = unavailable
    bearing         DOUBLE,
    delay_minutes   INT,
    status          VARCHAR(20),
    last_updated    DATETIME(3),
    position_source VARCHAR(30),
    FOREIGN KEY (train_id) REFERENCES trains(train_id)
) ENGINE=InnoDB;

-- ── Tracking History (optional snapshots) ────────────────────
CREATE TABLE IF NOT EXISTS tracking_history (
    history_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_id    BIGINT  NOT NULL,
    latitude    DOUBLE,
    longitude   DOUBLE,
    speed_kmh   DOUBLE,
    timestamp   DATETIME(3) NOT NULL,
    INDEX idx_history_train     (train_id),
    INDEX idx_history_timestamp (timestamp),
    FOREIGN KEY (train_id) REFERENCES trains(train_id)
) ENGINE=InnoDB;

-- ============================================================
-- Sample data — a few Indian express trains for demonstration
-- ============================================================

INSERT IGNORE INTO stations (station_code, station_name, latitude, longitude) VALUES
('NDLS', 'New Delhi',       28.6428, 77.2197),
('BCT',  'Mumbai Central',  18.9696, 72.8197),
('MAS',  'Chennai Central', 13.0827, 80.2707),
('SBC',  'Bengaluru City',  12.9784, 77.5720),
('HWH',  'Howrah',          22.5839, 88.3426),
('PNBE', 'Patna Junction',  25.6103, 85.1727),
('CNB',  'Kanpur Central',  26.4499, 80.3319),
('AGC',  'Agra Cantt',      27.1591, 78.0120),
('BPL',  'Bhopal',          23.2599, 77.4126),
('NGP',  'Nagpur',          21.1458, 79.0882);

INSERT IGNORE INTO trains (train_number, train_name, source_station_id, destination_station_id, status)
SELECT '12951', 'Mumbai Rajdhani Express',
    (SELECT station_id FROM stations WHERE station_code='NDLS'),
    (SELECT station_id FROM stations WHERE station_code='BCT'),
    'SCHEDULED'
WHERE NOT EXISTS (SELECT 1 FROM trains WHERE train_number='12951');

INSERT IGNORE INTO trains (train_number, train_name, source_station_id, destination_station_id, status)
SELECT '12001', 'Bhopal Shatabdi Express',
    (SELECT station_id FROM stations WHERE station_code='NDLS'),
    (SELECT station_id FROM stations WHERE station_code='BPL'),
    'SCHEDULED'
WHERE NOT EXISTS (SELECT 1 FROM trains WHERE train_number='12001');

INSERT IGNORE INTO trains (train_number, train_name, source_station_id, destination_station_id, status)
SELECT '12259', 'Sealdah Duronto Express',
    (SELECT station_id FROM stations WHERE station_code='HWH'),
    (SELECT station_id FROM stations WHERE station_code='NDLS'),
    'SCHEDULED'
WHERE NOT EXISTS (SELECT 1 FROM trains WHERE train_number='12259');

-- ── Sample GeoJSON route for 12951 (simplified NDLS→BCT) ─────
INSERT IGNORE INTO routes (train_id, geometry_geojson, distance_km, version)
SELECT t.train_id,
    '{"type":"LineString","coordinates":[[77.2197,28.6428],[78.0120,27.1591],[77.4126,23.2599],[79.0882,21.1458],[72.8197,18.9696]]}',
    1388.0, 1
FROM trains t WHERE t.train_number = '12951'
  AND NOT EXISTS (SELECT 1 FROM routes r WHERE r.train_id = t.train_id);

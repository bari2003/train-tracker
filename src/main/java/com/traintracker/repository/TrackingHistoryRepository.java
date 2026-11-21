package com.traintracker.repository;

import com.traintracker.model.TrackingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackingHistoryRepository extends JpaRepository<TrackingHistory, Long> {
    List<TrackingHistory> findByTrain_TrainIdOrderByTimestampDesc(Long trainId);
}

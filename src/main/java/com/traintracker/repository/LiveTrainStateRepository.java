package com.traintracker.repository;

import com.traintracker.model.LiveTrainState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LiveTrainStateRepository extends JpaRepository<LiveTrainState, Long> {
    Optional<LiveTrainState> findByTrain_TrainId(Long trainId);
}

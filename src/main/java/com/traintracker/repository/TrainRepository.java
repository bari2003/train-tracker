package com.traintracker.repository;

import com.traintracker.model.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {
    Optional<Train> findByTrainNumber(String trainNumber);
    List<Train> findByTrainNameContainingIgnoreCase(String name);

    @Query("SELECT t FROM Train t WHERE LOWER(t.trainNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(t.trainName) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Train> searchTrains(@Param("q") String query);
}

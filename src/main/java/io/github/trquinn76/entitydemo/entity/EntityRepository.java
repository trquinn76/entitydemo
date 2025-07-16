package io.github.trquinn76.entitydemo.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EntityRepository extends JpaRepository<Entity, Long> {

    @Query("SELECT e FROM Entity e WHERE e.latitudeDegrees BETWEEN :south AND :north AND e.longitudeDegrees BETWEEN :west AND :east")
    List<Entity> findByBounds(double north, double west, double south, double east);
}

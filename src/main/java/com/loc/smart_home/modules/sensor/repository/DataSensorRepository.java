package com.loc.smart_home.modules.sensor.repository;

import com.loc.smart_home.modules.sensor.entity.DataSensor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DataSensorRepository
        extends JpaRepository<DataSensor, Long> {

    @EntityGraph(attributePaths = "sensor")
    @Query(
            value = DataSensorQueries.SEARCH,
            countQuery = DataSensorQueries.COUNT_SEARCH
    )
    Page<DataSensor> search(
            @Param("field") String field,
            @Param("keywordPattern") String keywordPattern,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "sensor")
    Optional<DataSensor> findFirstBySensor_NameOrderByTimeDescIdDesc(
            String sensorName
    );
}
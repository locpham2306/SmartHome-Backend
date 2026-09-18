package com.loc.smart_home.modules.sensor.repository;

import com.loc.smart_home.modules.sensor.entity.DataSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataSensorRepository extends JpaRepository<DataSensor, Long> {
}

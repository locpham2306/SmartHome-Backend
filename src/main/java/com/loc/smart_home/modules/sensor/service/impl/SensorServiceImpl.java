package com.loc.smart_home.modules.sensor.service.impl;

import com.loc.smart_home.exception.BusinessException;
import com.loc.smart_home.integration.mqtt.dto.SensorMessage;
import com.loc.smart_home.modules.sensor.entity.DataSensor;
import com.loc.smart_home.modules.sensor.entity.Sensor;
import com.loc.smart_home.modules.sensor.repository.DataSensorRepository;
import com.loc.smart_home.modules.sensor.repository.SensorRepository;
import com.loc.smart_home.modules.sensor.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {

    private final SensorRepository sensorRepository;
    private final DataSensorRepository dataSensorRepository;

    @Override
    @Transactional
    public void saveSensorData(SensorMessage message) {
        validateMessage(message);

        Sensor temperature = getSensorByName("Temperature");
        Sensor humidity = getSensorByName("Humidity");
        Sensor light = getSensorByName("Light");

        LocalDateTime time = LocalDateTime.now();

        DataSensor temperatureData = createDataSensor(temperature, message.getTemperature(), time);
        DataSensor humidityData = createDataSensor(humidity, message.getHumidity(), time);
        DataSensor lightData = createDataSensor(light, message.getLight(), time);

        this.dataSensorRepository.saveAll(List.of(temperatureData, humidityData, lightData));
    }

    private void validateMessage(SensorMessage message) {
        if (message == null || message.getTemperature() == null || message.getHumidity() == null || message.getLight() == null) {
            throw new BusinessException("INVALID_SENSOR_DATA", "Temperature, Humidity, Light are required");
        }
        if (message.getHumidity().compareTo(BigDecimal.ZERO) < 0 || message.getHumidity().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException("INVALID_HUMIDITY", "Humidity must be between 0 and 100");
        }
        if (message.getLight().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("INVALID_HUMIDITY", "Humidity must be greater than 0");
        }
    }

    private Sensor getSensorByName(String name) {
        return this.sensorRepository.findByName(name).orElseThrow(() -> new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "SENSOR_CONFIGURATION_MISSING",
                "Sensor configuration not found: " + name));
    }

    private DataSensor createDataSensor(Sensor sensor, BigDecimal value, LocalDateTime time){
        BigDecimal roundedValue = value.setScale(2, RoundingMode.HALF_UP);
        if (roundedValue.abs().compareTo(BigDecimal.valueOf(99999999.99)) > 0){
            throw new BusinessException("SENSOR_VALUE_OUT_OF_RANGE", "Sensor value exceeds database precision");
        }
        DataSensor dataSensor = new DataSensor();
        dataSensor.setSensor(sensor);
        dataSensor.setValue(roundedValue);
        dataSensor.setTime(time);
        return dataSensor;
    }
}

package com.loc.smart_home.modules.sensor.service.impl;

import com.loc.smart_home.common.dto.response.PageResponse;
import com.loc.smart_home.exception.BusinessException;
import com.loc.smart_home.integration.mqtt.dto.SensorMessage;
import com.loc.smart_home.modules.sensor.dto.request.SensorChartRequest;
import com.loc.smart_home.modules.sensor.dto.request.SensorPageableSearchRequestDTO;
import com.loc.smart_home.modules.sensor.dto.request.SensorSearchRequest;
import com.loc.smart_home.modules.sensor.dto.response.DataSensorResponse;
import com.loc.smart_home.modules.sensor.dto.response.SensorChartPointResponse;
import com.loc.smart_home.modules.sensor.dto.response.SensorLatestResponse;
import com.loc.smart_home.modules.sensor.entity.DataSensor;
import com.loc.smart_home.modules.sensor.entity.Sensor;
import com.loc.smart_home.modules.sensor.mapper.DataSensorMapper;
import com.loc.smart_home.modules.sensor.repository.DataSensorRepository;
import com.loc.smart_home.modules.sensor.repository.SensorRepository;
import com.loc.smart_home.modules.sensor.service.SensorService;
import com.loc.smart_home.utils.PageableSearchUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {

    private final SensorRepository sensorRepository;
    private final DataSensorRepository dataSensorRepository;
    private final DataSensorMapper dataSensorMapper;

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

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DataSensorResponse> search(
            SensorPageableSearchRequestDTO request
    ) {
        SensorSearchRequest searchRequest = request.getSearchRequest();

        String field = searchRequest.getField();
        String keywordPattern = buildKeywordPattern(
                searchRequest.getKeywords()
        );

        return PageableSearchUtils.<DataSensor, DataSensorResponse>search(
                pageable -> dataSensorRepository.search(
                        field,
                        keywordPattern,
                        pageable
                ),
                request.getPageRequest(),
                ALLOWED_SORT_FIELDS,
                dataSensorMapper::toResponse
        );
    }

    @Override
    public SensorLatestResponse getLatest() {
        DataSensorResponse temperature = getLatestByName("Temperature");
        DataSensorResponse humidity = getLatestByName("Humidity");
        DataSensorResponse light = getLatestByName("Light");

        return new SensorLatestResponse(temperature,humidity, light);
    }

    @Override
    public List<SensorChartPointResponse> getChart(SensorChartRequest request) {
        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = request.getEndTime();
        if(startTime == null && endTime == null){
            endTime = LocalDateTime.now();
            startTime = endTime.minusHours(1);
        }
        else if(startTime == null || endTime == null){
            throw new BusinessException("INVALID_TIME_RANGE", "Start time and end time must be provided together");
        }
        if(startTime.isAfter(endTime)){
            throw new BusinessException("INVALID_TIME_RANGE", "End time must be greater than start time");
        }
        Pageable pageable = PageRequest.of(0, request.getLimit());
        List<DataSensor> entities = this.dataSensorRepository.findChartData(request.getType(),startTime ,endTime, pageable);
        List<SensorChartPointResponse> points = new ArrayList<>();
        for(DataSensor entity : entities){
            points.add(dataSensorMapper.toChartPointResponse(entity));
        }
        Collections.reverse(points);
        return points;
    }

    private String buildKeywordPattern(String keywords) {
        if (keywords == null) {
            return null;
        }

        String keyword = keywords.trim().toLowerCase(Locale.ROOT);

        // Cho phép nhập ID dạng #123
        if (keyword.startsWith("#")) {
            keyword = keyword.substring(1);
        }

        if (keyword.isEmpty()) {
            return null;
        }

        // Tìm đúng ký tự người dùng nhập,
        // không coi % và _ là ký tự đại diện của LIKE.
        keyword = keyword
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");

        return "%" + keyword + "%";
    }

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("id", "time", "value");
    private DataSensorResponse getLatestByName(String name){
        return this.dataSensorRepository.findFirstBySensor_NameOrderByTimeDescIdDesc(name).map(dataSensorMapper::toResponse).orElse(null);
    }
}


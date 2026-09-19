package com.loc.smart_home.modules.sensor.service;

import com.loc.smart_home.integration.mqtt.dto.SensorMessage;

public interface SensorService {
    void saveSensorData(SensorMessage message);
}

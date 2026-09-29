package com.loc.smart_home.modules.sensor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SensorLatestResponse {
    DataSensorResponse temperature;
    DataSensorResponse humidity;
    DataSensorResponse light;

}

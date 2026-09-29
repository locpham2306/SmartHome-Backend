package com.loc.smart_home.modules.sensor.controller;

import com.loc.smart_home.common.dto.response.BaseResponse;
import com.loc.smart_home.common.dto.response.PageResponse;
import com.loc.smart_home.modules.sensor.dto.request.SensorPageableSearchRequestDTO;
import com.loc.smart_home.modules.sensor.dto.response.DataSensorResponse;
import com.loc.smart_home.modules.sensor.dto.response.SensorLatestResponse;
import com.loc.smart_home.modules.sensor.service.SensorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SensorController {
    private final SensorService sensorService;

    @GetMapping("/api/sensors")
    public ResponseEntity<BaseResponse<PageResponse<DataSensorResponse>>> search(@Valid @ParameterObject SensorPageableSearchRequestDTO request){
        PageResponse<DataSensorResponse> response = this.sensorService.search(request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/api/sensors/latest")
    public ResponseEntity<BaseResponse<SensorLatestResponse>> getLatest(){
        SensorLatestResponse response = this.sensorService.getLatest();
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}

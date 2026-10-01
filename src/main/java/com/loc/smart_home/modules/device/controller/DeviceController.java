package com.loc.smart_home.modules.device.controller;

import com.loc.smart_home.common.dto.response.BaseResponse;
import com.loc.smart_home.modules.device.dto.response.DeviceResponse;
import com.loc.smart_home.modules.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @GetMapping("api/devices")
    public ResponseEntity<BaseResponse<List<DeviceResponse>>> getDevices() {
        List<DeviceResponse> response = this.deviceService.getDevices();
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}

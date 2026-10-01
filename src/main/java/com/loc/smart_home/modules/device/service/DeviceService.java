package com.loc.smart_home.modules.device.service;

import com.loc.smart_home.modules.device.dto.request.DeviceControlRequest;
import com.loc.smart_home.modules.device.dto.response.DeviceControlResponse;
import com.loc.smart_home.modules.device.dto.response.DeviceResponse;

import java.util.List;

public interface DeviceService {
    List<DeviceResponse> getDevices();
    DeviceControlResponse control(Integer deviceId, DeviceControlRequest request);
}

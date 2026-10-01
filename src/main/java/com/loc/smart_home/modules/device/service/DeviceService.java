package com.loc.smart_home.modules.device.service;

import com.loc.smart_home.modules.device.dto.response.DeviceResponse;

import java.util.List;

public interface DeviceService {
    List<DeviceResponse> getDevices();
}

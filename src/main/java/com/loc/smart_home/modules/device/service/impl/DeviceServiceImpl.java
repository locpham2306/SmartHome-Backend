package com.loc.smart_home.modules.device.service.impl;

import com.loc.smart_home.modules.device.dto.response.DeviceResponse;
import com.loc.smart_home.modules.device.entity.Device;
import com.loc.smart_home.modules.device.mapper.DeviceMapper;
import com.loc.smart_home.modules.device.repository.DeviceRepository;
import com.loc.smart_home.modules.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {
    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;
    @Override
    public List<DeviceResponse> getDevices() {
        List<Device> devices = this.deviceRepository.findAll();
        return deviceMapper.toResponseList(devices);
    }
}

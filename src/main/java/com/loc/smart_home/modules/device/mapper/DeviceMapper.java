package com.loc.smart_home.modules.device.mapper;

import com.loc.smart_home.common.base.BaseMapper;
import com.loc.smart_home.modules.device.dto.response.DeviceResponse;
import com.loc.smart_home.modules.device.entity.Device;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DeviceMapper extends BaseMapper<Device, DeviceResponse> {
}

package com.loc.smart_home.modules.device.service.impl;

import com.loc.smart_home.exception.BusinessException;
import com.loc.smart_home.modules.actionhistory.entity.ActionHistory;
import com.loc.smart_home.modules.actionhistory.enums.Action;
import com.loc.smart_home.modules.actionhistory.enums.ActionStatus;
import com.loc.smart_home.modules.actionhistory.repository.ActionHistoryRepository;
import com.loc.smart_home.modules.device.dto.request.DeviceControlRequest;
import com.loc.smart_home.modules.device.dto.response.DeviceControlResponse;
import com.loc.smart_home.modules.device.dto.response.DeviceResponse;
import com.loc.smart_home.modules.device.entity.Device;
import com.loc.smart_home.modules.device.mapper.DeviceMapper;
import com.loc.smart_home.modules.device.repository.DeviceRepository;
import com.loc.smart_home.modules.device.service.DeviceService;
import com.loc.smart_home.modules.user.entity.User;
import com.loc.smart_home.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {
    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;
    private final ActionHistoryRepository actionHistoryRepository;
    private final MqttClient mqttClient;
    private final UserRepository userRepository;

    @Value("${user-id}")
    private Long userId;
    @Override
    public List<DeviceResponse> getDevices() {
        List<Device> devices = this.deviceRepository.findAll();
        return deviceMapper.toResponseList(devices);
    }

    @Override
    public DeviceControlResponse control(Integer deviceId, DeviceControlRequest request) {
        if (actionHistoryRepository.existsByDevice_IdAndStatus(
                deviceId, ActionStatus.PENDING
        )) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "DEVICE_COMMAND_PENDING",
                    "Device already has a pending command"
            );
        }
        Action action = Action.valueOf(request.getAction());
        Device device = this.deviceRepository.findById(deviceId).
                orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "DEVICE_NPT_FOUND",
                        "Device not found"));
        if (device.getStatus().name().equals(action.name())) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "DEVICE_ALREADY_IN_STATE",
                    "Device is already " + action.name()
            );
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND",
                        "User not found"
                ));
        ActionHistory actionHistory = new ActionHistory();
        actionHistory.setDevice(device);
        actionHistory.setUser(user);
        actionHistory.setAction(action);
        actionHistory.setStatus(ActionStatus.PENDING);
        ActionHistory savedHistory = this.actionHistoryRepository.save(actionHistory);
        String topic = "device/control/" + device.getId();

        String payload = """
        {
            "historyId": %d,
            "action": "%s"
        }
        """.formatted(savedHistory.getId(), action.name());

        MqttMessage message = new MqttMessage(
                payload.getBytes(StandardCharsets.UTF_8)
        );
        message.setQos(1);
        message.setRetained(false);

        try {
            mqttClient.publish(topic, message);
        } catch (MqttException exception) {
            savedHistory.setStatus(ActionStatus.ERROR);
            actionHistoryRepository.save(savedHistory);

            throw new BusinessException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "MQTT_PUBLISH_FAILED",
                    "Could not send device command"
            );
        }

        DeviceControlResponse response = new DeviceControlResponse();
        response.setHistoryId(savedHistory.getId());
        response.setDeviceId(device.getId());
        response.setAction(action.name());
        response.setStatus(savedHistory.getStatus().name());

        return response;

    }
}

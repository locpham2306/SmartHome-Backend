package com.loc.smart_home.integration.mqtt.listener;

import com.loc.smart_home.config.MqttProperties;
import com.loc.smart_home.exception.BusinessException;
import com.loc.smart_home.integration.mqtt.dto.SensorMessage;
import com.loc.smart_home.modules.sensor.service.SensorService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import java.nio.charset.StandardCharsets;


@Component
@RequiredArgsConstructor
@Slf4j
@Setter
public class MqttMessageListener implements MqttCallbackExtended  {
    private final MqttProperties mqttProperties;
    private final JsonMapper jsonMapper;
    private final SensorService sensorService;

    private MqttClient client;
    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.info(
                "MQTT connected: server={}, reconnect={}",
                serverURI,
                reconnect
        );

        try {
            client.subscribe(
                    mqttProperties.getSensorTopic(),
                    mqttProperties.getQos()
            );

            log.info(
                    "Subscribed to topic {} with QoS {}",
                    mqttProperties.getSensorTopic(),
                    mqttProperties.getQos()
            );
        } catch (MqttException exception) {
            log.error(
                    "Failed to subscribe to topic "
                            + mqttProperties.getSensorTopic(),
                    exception
            );
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT connection lost", cause);;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if (!mqttProperties.getSensorTopic().equals(topic)){
            return;
        }

        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        SensorMessage sensorMessage;
        try {
            sensorMessage = jsonMapper.readValue(payload, SensorMessage.class);
        }
        catch (JacksonException exception){
            log.warn("Discarding invalid JSON on topic {}: {}",
                    topic,
                    exception.getMessage());
            return;
        }
        try {
            sensorService.saveSensorData(sensorMessage);

            log.debug("Sensor data saved from topic {}", topic);
        } catch (BusinessException exception) {
            if (exception.getStatus().is4xxClientError()) {
                log.warn(
                        "Discarding invalid sensor data: code={}, message={}",
                        exception.getStrCode(),
                        exception.getMessage()
                );
                return;
            }

            log.error(
                    "Sensor processing failed on topic " + topic,
                    exception
            );
            throw exception;
        } catch (Exception exception) {
            log.error(
                    "Failed to save sensor data from topic " + topic,
                    exception
            );
            throw exception;
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}

package com.example.mqmonitor.config;

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class MQConfig {

    @Autowired
    private MQInfoProperties mqInfo;

    @Bean
    public MQQueueManager mqQueueManager() {
        try {
            Hashtable<String, Object> properties = new Hashtable<>();
            properties.put(MQConstants.HOST_NAME_PROPERTY, mqInfo.getConnName().split("\\(")[0]);
            properties.put(MQConstants.PORT_PROPERTY, Integer.parseInt(mqInfo.getConnName().split("\\(|\\)")[1]));
            properties.put(MQConstants.CHANNEL_PROPERTY, mqInfo.getChannel());
            properties.put(MQConstants.USER_ID_PROPERTY, mqInfo.getUser());
            if (StringUtils.isNotBlank(mqInfo.getPassword())) {
                properties.put(MQConstants.PASSWORD_PROPERTY, mqInfo.getPassword());
            }
            return new MQQueueManager(mqInfo.getQueueManager(), properties);
        } catch (Exception e) {
            log.error("無法連接到 MQ Queue Manager: {}", e.getMessage(), e);
            return null;
        }
    }
}

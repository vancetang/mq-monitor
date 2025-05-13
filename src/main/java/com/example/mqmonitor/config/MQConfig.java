package com.example.mqmonitor.config;

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class MQConfig {

    @Value("${mq-info.queueManager}")
    private String queueManager;

    @Value("${mq-info.channel}")
    private String channel;

    @Value("${mq-info.connName}")
    private String connName;

    @Value("${mq-info.user}")
    private String user;

    @Value("${mq-info.password:}")
    private String password;

    @Bean
    public MQQueueManager mqQueueManager() {
        try {
            Hashtable<String, Object> properties = new Hashtable<>();
            properties.put(MQConstants.HOST_NAME_PROPERTY, connName.split("\\(")[0]);
            properties.put(MQConstants.PORT_PROPERTY, Integer.parseInt(connName.split("\\(|\\)")[1]));
            properties.put(MQConstants.CHANNEL_PROPERTY, channel);
            properties.put(MQConstants.USER_ID_PROPERTY, user);
            if (StringUtils.isNotBlank(password)) {
                properties.put(MQConstants.PASSWORD_PROPERTY, password);
            }
            return new MQQueueManager(queueManager, properties);
        } catch (Exception e) {
            log.error("無法連接到 MQ Queue Manager: {}", e.getMessage(), e);
            return null;
        }
    }
}

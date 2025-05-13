package com.example.mqmonitor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "mq-info")
public class MQInfoProperties {
    private String queueManager;
    private String channel;
    private String connName;
    private String user;
    private String password;
}
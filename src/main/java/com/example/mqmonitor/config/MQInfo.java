package com.example.mqmonitor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * IBM MQ 連接資訊
 */
@Data
@Component
@ConfigurationProperties(prefix = "mq-info")
public class MQInfo {
    /**
     * IBM MQ Queue Manager 名稱
     */
    private String queueManager;
    /**
     * IBM MQ 連接通道
     */
    private String channel;
    /**
     * IBM MQ 連接名稱，格式為 host(port)
     */
    private String connName;
    /**
     * IBM MQ 帳號
     */
    private String user;
    /**
     * IBM MQ 密碼
     */
    private String password;
}
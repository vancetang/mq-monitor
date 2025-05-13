package com.example.mqmonitor.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.example.mqmonitor.service.MQConnectionService;
import com.ibm.mq.MQQueueManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableScheduling
public class MQConfig {

    @Autowired
    private MQConnectionService mqConnectionService;

    @Bean
    public MQQueueManager mqQueueManager() {
        // 嘗試連接到 MQ
        mqConnectionService.connect();
        // 返回 MQQueueManager 實例，可能為 null
        return mqConnectionService.getMQQueueManager();
    }
}

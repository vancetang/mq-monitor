package com.example.mqmonitor.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.mqmonitor.service.MQConnectionService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MQConnectionScheduler {

    @Autowired
    private MQConnectionService mqConnectionService;

    /**
     * 定期檢查 MQ 連線狀態，如果斷開則嘗試重新連線
     * 每 15 秒執行一次
     */
    @Scheduled(fixedRate = 15000)
    public void checkMQConnection() {
        // 主動檢查連線狀態
        if (!mqConnectionService.isConnected()) {
            log.info("檢測到 MQ 連線已斷開，嘗試重新連線");
            boolean reconnected = mqConnectionService.reconnect();
            if (reconnected) {
                log.info("MQ 重新連線成功");
            } else {
                log.error("MQ 重新連線失敗");
            }
        }
    }
}

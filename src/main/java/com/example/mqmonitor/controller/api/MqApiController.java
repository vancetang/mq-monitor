package com.example.mqmonitor.controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mqmonitor.service.MQConnectionService;
import com.example.mqmonitor.service.MQPCFService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/mq")
public class MqApiController {

    @Autowired
    private MQPCFService mqpcfService;

    @Autowired
    private MQConnectionService mqConnectionService;

    @GetMapping("/queuemanager")
    public ResponseEntity<Map<String, Object>> getQueueManagerStatus() {
        log.debug("REST 請求獲取 Queue Manager 狀態");
        return ResponseEntity.ok(mqpcfService.getQueueManagerStatus());
    }

    @GetMapping("/queues")
    public ResponseEntity<List<Map<String, Object>>> getQueuesStatus() {
        log.debug("REST 請求獲取所有隊列狀態");
        return ResponseEntity.ok(mqpcfService.getQueuesStatus());
    }

    @GetMapping("/channels")
    public ResponseEntity<List<Map<String, Object>>> getChannelsStatus() {
        log.debug("REST 請求獲取所有通道狀態");
        return ResponseEntity.ok(mqpcfService.getChannelsStatus());
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAllStatus() {
        log.debug("REST 請求獲取所有 MQ 狀態");

        Map<String, Object> allStatus = new HashMap<>();
        allStatus.put("queueManager", mqpcfService.getQueueManagerStatus());
        allStatus.put("queues", mqpcfService.getQueuesStatus());
        allStatus.put("channels", mqpcfService.getChannelsStatus());

        return ResponseEntity.ok(allStatus);
    }

    /**
     * 手動重新連接到 MQ
     *
     * @return 連接結果
     */
    @PostMapping("/reconnect")
    public ResponseEntity<Map<String, Object>> reconnect() {
        log.info("REST 請求手動重新連接到 MQ");

        Map<String, Object> result = new HashMap<>();
        boolean reconnected = mqConnectionService.reconnect();

        if (reconnected) {
            result.put("success", true);
            result.put("message", "已成功重新連接到 MQ");
            log.info("手動重新連接到 MQ 成功");
        } else {
            result.put("success", false);
            result.put("message", "重新連接到 MQ 失敗");
            log.error("手動重新連接到 MQ 失敗");
        }

        return ResponseEntity.ok(result);
    }
}

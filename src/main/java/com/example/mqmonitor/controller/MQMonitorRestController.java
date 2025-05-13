package com.example.mqmonitor.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mqmonitor.service.MQPCFService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/mq")
public class MQMonitorRestController {

    @Autowired
    MQPCFService mqpcfService;

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
}

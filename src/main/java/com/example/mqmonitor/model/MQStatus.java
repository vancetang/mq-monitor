package com.example.mqmonitor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MQStatus {
    private Map<String, Object> queueManager;
    private List<Map<String, Object>> queues;
    private List<Map<String, Object>> channels;
}

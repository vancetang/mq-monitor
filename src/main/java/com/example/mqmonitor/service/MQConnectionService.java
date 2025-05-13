package com.example.mqmonitor.service;

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;

import com.example.mqmonitor.config.MQInfoProperties;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MQConnectionService {

    @Autowired
    private MQInfoProperties mqInfo;

    private MQQueueManager mqQueueManager;
    private boolean isConnecting = false;

    /**
     * 獲取當前的 MQQueueManager 實例
     * 
     * @return MQQueueManager 實例，如果未連接則返回 null
     */
    public synchronized MQQueueManager getMQQueueManager() {
        return mqQueueManager;
    }

    /**
     * 檢查 MQ 連線是否可用
     * 
     * @return 如果連線可用返回 true，否則返回 false
     */
    public synchronized boolean isConnected() {
        if (mqQueueManager == null) {
            return false;
        }
        
        try {
            // 嘗試獲取連線名稱來檢查連線是否仍然有效
            mqQueueManager.getName();
            return true;
        } catch (MQException e) {
            log.error("MQ 連線已斷開: {}", e.getMessage());
            // 連線已斷開，將 mqQueueManager 設為 null
            mqQueueManager = null;
            return false;
        }
    }

    /**
     * 連接到 MQ Queue Manager
     * 
     * @return 如果連接成功返回 true，否則返回 false
     */
    public synchronized boolean connect() {
        // 如果已經在連接中，則返回
        if (isConnecting) {
            log.info("已有另一個連接請求正在處理中");
            return false;
        }
        
        // 如果已經連接，則返回
        if (isConnected()) {
            log.info("MQ 已經連接");
            return true;
        }
        
        isConnecting = true;
        
        try {
            log.info("嘗試連接到 MQ Queue Manager: {}", mqInfo.getQueueManager());
            
            Hashtable<String, Object> properties = new Hashtable<>();
            properties.put(MQConstants.HOST_NAME_PROPERTY, mqInfo.getConnName().split("\\(")[0]);
            properties.put(MQConstants.PORT_PROPERTY, Integer.parseInt(mqInfo.getConnName().split("\\(|\\)")[1]));
            properties.put(MQConstants.CHANNEL_PROPERTY, mqInfo.getChannel());
            properties.put(MQConstants.USER_ID_PROPERTY, mqInfo.getUser());
            if (StringUtils.isNotBlank(mqInfo.getPassword())) {
                properties.put(MQConstants.PASSWORD_PROPERTY, mqInfo.getPassword());
            }
            
            mqQueueManager = new MQQueueManager(mqInfo.getQueueManager(), properties);
            log.info("成功連接到 MQ Queue Manager: {}", mqInfo.getQueueManager());
            return true;
        } catch (Exception e) {
            log.error("無法連接到 MQ Queue Manager: {}", e.getMessage(), e);
            mqQueueManager = null;
            return false;
        } finally {
            isConnecting = false;
        }
    }

    /**
     * 斷開與 MQ Queue Manager 的連接
     */
    public synchronized void disconnect() {
        if (mqQueueManager != null) {
            try {
                mqQueueManager.disconnect();
                log.info("已斷開與 MQ Queue Manager 的連接");
            } catch (MQException e) {
                log.error("斷開 MQ 連接時發生錯誤: {}", e.getMessage(), e);
            } finally {
                mqQueueManager = null;
            }
        }
    }

    /**
     * 重新連接到 MQ Queue Manager
     * 
     * @return 如果重新連接成功返回 true，否則返回 false
     */
    public synchronized boolean reconnect() {
        log.info("嘗試重新連接到 MQ Queue Manager");
        disconnect();
        return connect();
    }
}

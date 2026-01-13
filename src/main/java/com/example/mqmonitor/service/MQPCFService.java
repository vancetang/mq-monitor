package com.example.mqmonitor.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.CMQCFC;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;

import lombok.extern.slf4j.Slf4j;

/**
 * MQ PCF (Programmable Command Formats) 服務類別。
 * 負責透過 PCF 訊息與 IBM MQ 進行互動，獲取 Queue Manager、Queue 及 Channel 的狀態資訊。
 */
@Slf4j
@Service
public class MQPCFService {

    @Autowired
    private MQConnectionService mqConnectionService;

    /**
     * 獲取 Queue Manager 的詳細狀態。
     * 包含名稱、連線狀態、啟動日期與時間等資訊。
     *
     * @return 包含 Queue Manager 狀態資訊的 Map
     */
    public Map<String, Object> getQueueManagerStatus() {
        Map<String, Object> status = new HashMap<>();
        MQQueueManager qm = mqConnectionService.getMQQueueManager();

        if (Objects.isNull(qm)) {
            status.put("status", "未連接");
            status.put("connected", false);
            return status;
        }

        PCFMessageAgent agent = null;
        try {
            status.put("name", qm.getName());

            // 建立 PCF 代理並發送查詢 Queue Manager 狀態的指令
            agent = new PCFMessageAgent(qm);
            PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q_MGR_STATUS);
            PCFMessage[] responses = agent.send(request);

            if (responses.length > 0) {
                PCFMessage resp = responses[0];
                status.put("status", "正常運行");
                status.put("connected", true);

                // 解析啟動日期與時間
                String startDate = getStringParam(resp, CMQCFC.MQCACF_Q_MGR_START_DATE);
                String startTime = getStringParam(resp, CMQCFC.MQCACF_Q_MGR_START_TIME);

                status.put("startDate", startDate);
                status.put("startTime", startTime != null ? startTime.replace(".", ":") : "");
            }
        } catch (MQDataException | IOException | MQException e) {
            handleError(e, "獲取 Queue Manager 狀態時發生錯誤");
            status.put("status", "錯誤: " + e.getMessage());
            status.put("connected", false);
        } finally {
            disconnectAgent(agent);
        }

        return status;
    }

    /**
     * 獲取所有本地隊列 (Local Queues) 的狀態列表。
     * 自動過濾系統隊列 (SYSTEM.*, AMQ.*)，並獲取每個隊列的深度及開啟計數。
     *
     * @return 包含所有隊列狀態資訊的列表
     */
    public List<Map<String, Object>> getQueuesStatus() {
        List<Map<String, Object>> queuesStatus = new ArrayList<>();
        MQQueueManager qm = mqConnectionService.getMQQueueManager();

        if (Objects.isNull(qm)) {
            return queuesStatus;
        }

        PCFMessageAgent agent = null;
        try {
            agent = new PCFMessageAgent(qm);
            // 查詢所有本地隊列
            PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q);
            request.addParameter(CMQC.MQCA_Q_NAME, "*");
            request.addParameter(CMQC.MQIA_Q_TYPE, CMQC.MQQT_LOCAL);

            PCFMessage[] responses = agent.send(request);

            for (PCFMessage resp : responses) {
                String queueName = getStringParam(resp, CMQC.MQCA_Q_NAME);

                // 忽略系統隊列
                if (isSystemResource(queueName)) {
                    continue;
                }

                Map<String, Object> queueInfo = new HashMap<>();
                queueInfo.put("name", queueName);
                queueInfo.put("maxDepth", getIntParam(resp, CMQC.MQIA_MAX_Q_DEPTH, -1));

                // 獲取個別隊列的即時深度狀態
                fetchQueueDepthStatus(agent, queueName, queueInfo);
                queuesStatus.add(queueInfo);
            }
        } catch (MQDataException | IOException e) {
            handleError(e, "獲取隊列列表時發生錯誤");
        } finally {
            disconnectAgent(agent);
        }

        return queuesStatus;
    }

    /**
     * 獲取所有通道 (Channels) 的狀態列表。
     * 自動過濾系統通道，並獲取每個通道的即時運行狀態。
     *
     * @return 包含所有通道狀態資訊的列表
     */
    public List<Map<String, Object>> getChannelsStatus() {
        List<Map<String, Object>> channelsStatus = new ArrayList<>();
        MQQueueManager qm = mqConnectionService.getMQQueueManager();

        if (Objects.isNull(qm)) {
            return channelsStatus;
        }

        PCFMessageAgent agent = null;
        try {
            agent = new PCFMessageAgent(qm);
            // 查詢所有通道
            PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_CHANNEL);
            request.addParameter(CMQCFC.MQCACH_CHANNEL_NAME, "*");

            PCFMessage[] responses = agent.send(request);

            for (PCFMessage resp : responses) {
                String channelName = getStringParam(resp, CMQCFC.MQCACH_CHANNEL_NAME);

                // 忽略系統通道
                if (isSystemResource(channelName)) {
                    continue;
                }

                Map<String, Object> channelInfo = new HashMap<>();
                channelInfo.put("name", channelName);

                // 獲取個別通道的即時狀態
                fetchChannelStatus(agent, channelName, channelInfo);
                channelsStatus.add(channelInfo);
            }
        } catch (MQDataException | IOException e) {
            handleError(e, "獲取通道列表時發生錯誤");
        } finally {
            disconnectAgent(agent);
        }

        return channelsStatus;
    }

    // ================= 私有輔助方法 (Private Helper Methods) =================

    /**
     * 獲取指定隊列的即時深度及開啟計數狀態。
     *
     * @param agent     PCF 代理
     * @param queueName 隊列名稱
     * @param queueInfo 用於存儲結果的 Map
     */
    private void fetchQueueDepthStatus(PCFMessageAgent agent, String queueName, Map<String, Object> queueInfo) {
        try {
            PCFMessage depthRequest = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q_STATUS);
            depthRequest.addParameter(CMQC.MQCA_Q_NAME, queueName);

            PCFMessage[] responses = agent.send(depthRequest);

            if (responses.length > 0) {
                PCFMessage resp = responses[0];
                queueInfo.put("depth", getIntParam(resp, CMQC.MQIA_CURRENT_Q_DEPTH, -1));
                queueInfo.put("openInputCount", getIntParam(resp, CMQC.MQIA_OPEN_INPUT_COUNT, 0));
                queueInfo.put("openOutputCount", getIntParam(resp, CMQC.MQIA_OPEN_OUTPUT_COUNT, 0));
                queueInfo.put("status", "正常");
            }
        } catch (Exception e) {
            handleError(e, null); // 僅檢查連線，不記錄詳細錯誤日誌
            queueInfo.put("status", e instanceof IOException ? "IO 錯誤: " + e.getMessage() : "錯誤: " + e.getMessage());
            queueInfo.put("depth", -1);
        }
    }

    /**
     * 獲取指定通道的即時運行狀態。
     *
     * @param agent       PCF 代理
     * @param channelName 通道名稱
     * @param channelInfo 用於存儲結果的 Map
     */
    private void fetchChannelStatus(PCFMessageAgent agent, String channelName, Map<String, Object> channelInfo) {
        try {
            PCFMessage statusRequest = new PCFMessage(CMQCFC.MQCMD_INQUIRE_CHANNEL_STATUS);
            statusRequest.addParameter(CMQCFC.MQCACH_CHANNEL_NAME, channelName);

            PCFMessage[] responses = agent.send(statusRequest);

            if (responses.length > 0) {
                int status = getIntParam(responses[0], CMQCFC.MQIACH_CHANNEL_STATUS, -1);
                channelInfo.put("status", getChannelStatusText(status));
                channelInfo.put("active", status == CMQCFC.MQCHS_RUNNING);
            } else {
                setChannelInactive(channelInfo);
            }
        } catch (PCFException e) {
            // 如果找不到通道狀態 (通常表示通道未運行)，設定為非作用中
            if (e.getReason() == CMQCFC.MQRCCF_CHL_STATUS_NOT_FOUND) {
                setChannelInactive(channelInfo);
            } else {
                handleError(e, null);
                channelInfo.put("status", "錯誤: " + e.getMessage());
                channelInfo.put("active", false);
            }
        } catch (Exception e) {
            handleError(e, null);
            channelInfo.put("status", "錯誤: " + e.getMessage());
            channelInfo.put("active", false);
        }
    }

    /**
     * 設定通道狀態為非作用中。
     *
     * @param channelInfo 通道資訊 Map
     */
    private void setChannelInactive(Map<String, Object> channelInfo) {
        channelInfo.put("status", "非作用中");
        channelInfo.put("active", false);
    }

    /**
     * 判斷是否為系統保留資源 (以 SYSTEM. 或 AMQ. 開頭)。
     *
     * @param name 資源名稱
     * @return 若為系統資源則回傳 true
     */
    private boolean isSystemResource(String name) {
        return name != null && (name.startsWith("SYSTEM.") || name.startsWith("AMQ."));
    }

    /**
     * 安全地從 PCF 訊息中獲取字串參數。
     *
     * @param msg     PCF 訊息
     * @param paramId 參數 ID
     * @return 參數值，若發生錯誤則回傳 null
     */
    private String getStringParam(PCFMessage msg, int paramId) {
        try {
            return (String) msg.getParameter(paramId).getValue();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 安全地從 PCF 訊息中獲取整數參數。
     *
     * @param msg          PCF 訊息
     * @param paramId      參數 ID
     * @param defaultValue 預設值
     * @return 參數值或預設值
     */
    private int getIntParam(PCFMessage msg, int paramId, int defaultValue) {
        try {
            Object val = msg.getParameter(paramId).getValue();
            return val instanceof Integer ? (Integer) val : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 統一錯誤處理邏輯。
     * 記錄錯誤日誌並檢查是否為連線層級的錯誤。
     *
     * @param e      發生的例外
     * @param logMsg 錯誤日誌訊息 (若為 null 則不記錄)
     */
    private void handleError(Exception e, String logMsg) {
        if (logMsg != null) {
            log.error("{}: {}", logMsg, e.getMessage(), e);
        }

        Throwable cause = e;
        // 如果是 MQDataException，嘗試獲取底層原因
        if (e instanceof MQDataException && e.getCause() != null) {
            cause = e.getCause();
        }

        // 檢查是否需要觸發連線錯誤處理
        if (cause instanceof MQException mqe) {
            mqConnectionService.checkConnectionError(mqe);
        }
    }

    /**
     * 安全關閉 PCF 代理連線。
     *
     * @param agent PCF 代理實例
     */
    private void disconnectAgent(PCFMessageAgent agent) {
        if (agent != null) {
            try {
                agent.disconnect();
            } catch (MQDataException e) {
                log.error("關閉 PCF 代理時發生錯誤: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 將 MQ 通道狀態代碼轉換為易讀的中文描述。
     *
     * @param status 通道狀態代碼
     * @return 狀態描述字串
     */
    private String getChannelStatusText(int status) {
        return switch (status) {
            case CMQCFC.MQCHS_BINDING -> "正在綁定";
            case CMQCFC.MQCHS_STARTING -> "正在啟動";
            case CMQCFC.MQCHS_RUNNING -> "運行中";
            case CMQCFC.MQCHS_STOPPING -> "正在停止";
            case CMQCFC.MQCHS_RETRYING -> "重試中";
            case CMQCFC.MQCHS_STOPPED -> "已停止";
            case CMQCFC.MQCHS_PAUSED -> "已暫停";
            case CMQCFC.MQCHS_INITIALIZING -> "初始化中";
            default -> "未知狀態(" + status + ")";
        };
    }
}

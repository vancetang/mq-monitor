package com.example.mqmonitor.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.Strings;
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

@Slf4j
@Service
public class MQPCFService {

    @Autowired
    private MQConnectionService mqConnectionService;

    /**
     * 獲取 Queue Manager 狀態
     */
    public Map<String, Object> getQueueManagerStatus() {
        Map<String, Object> status = new HashMap<>();
        PCFMessageAgent agent = null;

        // 獲取 MQQueueManager 實例
        MQQueueManager mqQueueManager = mqConnectionService.getMQQueueManager();

        try {
            if (Objects.isNull(mqQueueManager)) {
                status.put("status", "未連接");
                status.put("connected", false);
                return status;
            }

            // 可能拋出 MQException
            status.put("name", mqQueueManager.getName());

            // 可能拋出 MQException
            agent = new PCFMessageAgent(mqQueueManager);

            PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q_MGR_STATUS);

            // 可能拋出 MQDataException, IOException
            PCFMessage[] responses = agent.send(request);

            if (responses.length > 0) {
                PCFMessage response = responses[0];
                status.put("status", "正常運行");
                status.put("connected", true);
                // 使用 getParameter 方法替代已棄用的 getStringParameterValue
                status.put("startDate", (String) response.getParameter(CMQCFC.MQCACF_Q_MGR_START_DATE).getValue());
                String startTime = (String) response.getParameter(CMQCFC.MQCACF_Q_MGR_START_TIME).getValue();
                status.put("startTime", Strings.CS.replace(startTime, ".", ":"));
            }
        } catch (MQException e) {
            log.error("獲取 Queue Manager 狀態時發生 MQ 錯誤: {}", e.getMessage(), e);
            mqConnectionService.checkConnectionError(e);
            status.put("status", "錯誤: " + e.getMessage());
            status.put("connected", false);
        } catch (MQDataException e) {
            log.error("獲取 Queue Manager 狀態時發生 PCF 資料錯誤: {}", e.getMessage(), e);
            // MQDataException 可能包裝了底層的 MQException
            if (e.getCause() instanceof MQException mqe) {
                mqConnectionService.checkConnectionError(mqe);
            }
            status.put("status", "錯誤: " + e.getMessage());
            status.put("connected", false);
        } catch (IOException e) {
            log.error("獲取 Queue Manager 狀態時發生 IO 錯誤: {}", e.getMessage(), e);
            status.put("status", "錯誤: " + e.getMessage());
            status.put("connected", false);
        } finally {
            disconnectAgent(agent);
        }

        return status;
    }

    /**
     * 獲取所有隊列的狀態
     */
    public List<Map<String, Object>> getQueuesStatus() {
        List<Map<String, Object>> queuesStatus = new ArrayList<>();
        PCFMessageAgent agent = null;

        MQQueueManager mqQueueManager = mqConnectionService.getMQQueueManager();

        try {
            if (Objects.isNull(mqQueueManager)) {
                return queuesStatus;
            }

            // 可能拋出 MQException
            agent = new PCFMessageAgent(mqQueueManager);

            PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q);
            request.addParameter(CMQC.MQCA_Q_NAME, "*");
            request.addParameter(CMQC.MQIA_Q_TYPE, CMQC.MQQT_LOCAL);

            // 可能拋出 MQDataException, IOException
            PCFMessage[] responses = agent.send(request);

            for (PCFMessage response : responses) {
                Map<String, Object> queueInfo = new HashMap<>();
                String queueName = (String) response.getParameter(CMQC.MQCA_Q_NAME).getValue();

                if (Strings.CS.startsWithAny(queueName, "SYSTEM.", "AMQ.")) {
                    continue;
                }

                queueInfo.put("name", queueName);

                // 獲取最大深度 (安全獲取)
                try {
                    int maxQueueDepth = ((Integer) response.getParameter(CMQC.MQIA_MAX_Q_DEPTH).getValue());
                    queueInfo.put("maxDepth", maxQueueDepth);
                } catch (Exception e) {
                    log.warn("無法獲取隊列 {} 的最大深度: {}", queueName, e.getMessage());
                    queueInfo.put("maxDepth", -1);
                }

                // 獲取隊列即時深度狀態
                fetchQueueDepthStatus(agent, queueName, queueInfo);

                queuesStatus.add(queueInfo);
            }
        } catch (MQDataException e) {
            log.error("獲取隊列列表時發生 PCF 資料錯誤: {}", e.getMessage(), e);
            if (e.getCause() instanceof MQException mqe) {
                mqConnectionService.checkConnectionError(mqe);
            }
        } catch (IOException e) {
            log.error("獲取隊列列表時發生 IO 錯誤: {}", e.getMessage(), e);
        } finally {
            disconnectAgent(agent);
        }

        return queuesStatus;
    }

    /**
     * 輔助方法：獲取單個隊列的深度狀態
     */
    private void fetchQueueDepthStatus(PCFMessageAgent agent, String queueName, Map<String, Object> queueInfo) {
        try {
            PCFMessage depthRequest = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q_STATUS);
            depthRequest.addParameter(CMQC.MQCA_Q_NAME, queueName);

            PCFMessage[] depthResponses = agent.send(depthRequest);

            if (depthResponses.length > 0) {
                PCFMessage depthResponse = depthResponses[0];
                int currentDepth = ((Integer) depthResponse.getParameter(CMQC.MQIA_CURRENT_Q_DEPTH).getValue());
                int openInputCount = ((Integer) depthResponse.getParameter(CMQC.MQIA_OPEN_INPUT_COUNT).getValue());
                int openOutputCount = ((Integer) depthResponse.getParameter(CMQC.MQIA_OPEN_OUTPUT_COUNT).getValue());

                queueInfo.put("depth", currentDepth);
                queueInfo.put("openInputCount", openInputCount);
                queueInfo.put("openOutputCount", openOutputCount);
                queueInfo.put("status", "正常");
            }
        } catch (MQDataException e) {
            // PCF 錯誤，可能是隊列狀態無法查詢
            if (e.getCause() instanceof MQException mqe) {
                mqConnectionService.checkConnectionError(mqe);
            }
            queueInfo.put("status", "錯誤: " + e.getMessage());
            queueInfo.put("depth", -1);
        } catch (IOException e) {
            queueInfo.put("status", "IO 錯誤: " + e.getMessage());
            queueInfo.put("depth", -1);
        }
    }

    /**
     * 獲取所有通道的狀態
     */
    public List<Map<String, Object>> getChannelsStatus() {
        List<Map<String, Object>> channelsStatus = new ArrayList<>();
        PCFMessageAgent agent = null;

        MQQueueManager mqQueueManager = mqConnectionService.getMQQueueManager();

        try {
            if (Objects.isNull(mqQueueManager)) {
                return channelsStatus;
            }

            // 可能拋出 MQException
            agent = new PCFMessageAgent(mqQueueManager);

            PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_CHANNEL);
            request.addParameter(CMQCFC.MQCACH_CHANNEL_NAME, "*");

            // 可能拋出 MQDataException, IOException
            PCFMessage[] responses = agent.send(request);

            for (PCFMessage response : responses) {
                Map<String, Object> channelInfo = new HashMap<>();
                String channelName = (String) response.getParameter(CMQCFC.MQCACH_CHANNEL_NAME).getValue();

                if (Strings.CS.startsWithAny(channelName, "SYSTEM.", "AMQ.")) {
                    continue;
                }

                channelInfo.put("name", channelName);

                // 獲取通道即時狀態
                fetchChannelStatus(agent, channelName, channelInfo);

                channelsStatus.add(channelInfo);
            }
        } catch (MQDataException e) {
            log.error("獲取通道列表時發生 PCF 資料錯誤: {}", e.getMessage(), e);
            if (e.getCause() instanceof MQException mqe) {
                mqConnectionService.checkConnectionError(mqe);
            }
        } catch (IOException e) {
            log.error("獲取通道列表時發生 IO 錯誤: {}", e.getMessage(), e);
        } finally {
            disconnectAgent(agent);
        }

        return channelsStatus;
    }

    /**
     * 輔助方法：獲取單個通道的狀態
     */
    private void fetchChannelStatus(PCFMessageAgent agent, String channelName, Map<String, Object> channelInfo) {
        try {
            PCFMessage statusRequest = new PCFMessage(CMQCFC.MQCMD_INQUIRE_CHANNEL_STATUS);
            statusRequest.addParameter(CMQCFC.MQCACH_CHANNEL_NAME, channelName);

            PCFMessage[] statusResponses = agent.send(statusRequest);

            if (statusResponses.length > 0) {
                PCFMessage statusResponse = statusResponses[0];
                int status = ((Integer) statusResponse.getParameter(CMQCFC.MQIACH_CHANNEL_STATUS).getValue());
                String statusText = getChannelStatusText(status);

                channelInfo.put("status", statusText);
                channelInfo.put("active", status == CMQCFC.MQCHS_RUNNING);
            } else {
                channelInfo.put("status", "非作用中");
                channelInfo.put("active", false);
            }
        } catch (PCFException e) {
            // 特殊處理：通道狀態未找到 (通常表示通道未運行)
            if (e.getReason() == CMQCFC.MQRCCF_CHL_STATUS_NOT_FOUND) {
                channelInfo.put("status", "非作用中");
                channelInfo.put("active", false);
            } else {
                if (e.getCause() instanceof MQException mqe) {
                    mqConnectionService.checkConnectionError(mqe);
                }
                channelInfo.put("status", "錯誤: " + e.getMessage());
                channelInfo.put("active", false);
            }
        } catch (MQDataException e) {
            // 其他 PCF 資料錯誤
            if (e.getCause() instanceof MQException mqe) {
                mqConnectionService.checkConnectionError(mqe);
            }
            channelInfo.put("status", "錯誤: " + e.getMessage());
            channelInfo.put("active", false);
        } catch (IOException e) {
            channelInfo.put("status", "IO 錯誤: " + e.getMessage());
            channelInfo.put("active", false);
        }
    }

    /**
     * 輔助方法：安全關閉 PCF Agent
     */
    private void disconnectAgent(PCFMessageAgent agent) {
        if (Objects.nonNull(agent)) {
            try {
                agent.disconnect();
            } catch (MQDataException e) {
                log.error("關閉 PCF 代理時發生錯誤: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 將通道狀態代碼轉換為文字描述
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

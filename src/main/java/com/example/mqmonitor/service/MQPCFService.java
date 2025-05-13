package com.example.mqmonitor.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.CMQCFC;
import com.ibm.mq.pcf.PCFMessage;
import com.ibm.mq.pcf.PCFMessageAgent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@SuppressWarnings("deprecation")
public class MQPCFService {

    @Autowired
    MQQueueManager mqQueueManager;

    /**
     * 獲取 Queue Manager 狀態
     */
    public Map<String, Object> getQueueManagerStatus() {
        Map<String, Object> status = new HashMap<>();
        PCFMessageAgent agent = null;

        try {
            if (mqQueueManager == null) {
                status.put("status", "未連接");
                status.put("connected", false);
                return status;
            }

            agent = new PCFMessageAgent(mqQueueManager);
            PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q_MGR_STATUS);

            PCFMessage[] responses = agent.send(request);
            if (responses.length > 0) {
                PCFMessage response = responses[0];

                status.put("name", mqQueueManager.getName());
                status.put("status", "正常運行");
                status.put("connected", true);
                // 使用 getParameter 方法替代已棄用的 getStringParameterValue
                status.put("startDate", (String) response.getParameter(CMQCFC.MQCACF_Q_MGR_START_DATE).getValue());
                status.put("startTime", (String) response.getParameter(CMQCFC.MQCACF_Q_MGR_START_TIME).getValue());
            }
        } catch (MQException | IOException e) {
            log.error("獲取 Queue Manager 狀態時發生錯誤: {}", e.getMessage(), e);
            status.put("status", "錯誤: " + e.getMessage());
            status.put("connected", false);
        } finally {
            if (agent != null) {
                try {
                    agent.disconnect();
                } catch (MQException e) {
                    log.error("關閉 PCF 代理時發生錯誤: {}", e.getMessage(), e);
                }
            }
        }

        return status;
    }

    /**
     * 獲取所有隊列的狀態
     */
    public List<Map<String, Object>> getQueuesStatus() {
        List<Map<String, Object>> queuesStatus = new ArrayList<>();
        PCFMessageAgent agent = null;

        try {
            if (mqQueueManager == null) {
                return queuesStatus;
            }

            agent = new PCFMessageAgent(mqQueueManager);
            PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q);
            request.addParameter(CMQC.MQCA_Q_NAME, "*");
            request.addParameter(CMQC.MQIA_Q_TYPE, CMQC.MQQT_LOCAL);

            PCFMessage[] responses = agent.send(request);

            for (PCFMessage response : responses) {
                Map<String, Object> queueInfo = new HashMap<>();
                // 使用 getParameter 方法替代已棄用的 getStringParameterValue
                String queueName = (String) response.getParameter(CMQC.MQCA_Q_NAME).getValue();

                // 排除清單
                if (StringUtils.startsWithAny(queueName, "SYSTEM.", "AMQ.")) {
                    continue;
                }

                queueInfo.put("name", queueName);

                // 獲取隊列的最大深度
                try {
                    int maxQueueDepth = ((Integer) response.getParameter(CMQC.MQIA_MAX_Q_DEPTH).getValue());
                    queueInfo.put("maxDepth", maxQueueDepth);
                } catch (Exception e) {
                    log.warn("無法獲取隊列 {} 的最大深度: {}", queueName, e.getMessage());
                    queueInfo.put("maxDepth", -1);
                }

                // 獲取隊列深度
                PCFMessage depthRequest = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q_STATUS);
                depthRequest.addParameter(CMQC.MQCA_Q_NAME, queueName);

                try {
                    PCFMessage[] depthResponses = agent.send(depthRequest);
                    if (depthResponses.length > 0) {
                        PCFMessage depthResponse = depthResponses[0];
                        // 使用 getParameter 方法替代已棄用的 getIntParameterValue
                        int currentDepth = ((Integer) depthResponse.getParameter(CMQC.MQIA_CURRENT_Q_DEPTH)
                                .getValue());
                        int openInputCount = ((Integer) depthResponse.getParameter(CMQC.MQIA_OPEN_INPUT_COUNT)
                                .getValue());
                        int openOutputCount = ((Integer) depthResponse.getParameter(CMQC.MQIA_OPEN_OUTPUT_COUNT)
                                .getValue());

                        queueInfo.put("depth", currentDepth);
                        queueInfo.put("openInputCount", openInputCount);
                        queueInfo.put("openOutputCount", openOutputCount);
                        queueInfo.put("status", "正常");
                    }
                } catch (MQException e) {
                    queueInfo.put("status", "錯誤: " + e.getMessage());
                    queueInfo.put("depth", -1);
                }

                queuesStatus.add(queueInfo);
            }
        } catch (MQException | IOException e) {
            log.error("獲取隊列狀態時發生錯誤: {}", e.getMessage(), e);
        } finally {
            if (agent != null) {
                try {
                    agent.disconnect();
                } catch (MQException e) {
                    log.error("關閉 PCF 代理時發生錯誤: {}", e.getMessage(), e);
                }
            }
        }

        return queuesStatus;
    }

    /**
     * 獲取所有通道的狀態
     */
    public List<Map<String, Object>> getChannelsStatus() {
        List<Map<String, Object>> channelsStatus = new ArrayList<>();
        PCFMessageAgent agent = null;

        try {
            if (mqQueueManager == null) {
                return channelsStatus;
            }

            agent = new PCFMessageAgent(mqQueueManager);
            PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_CHANNEL);
            request.addParameter(CMQCFC.MQCACH_CHANNEL_NAME, "*");

            PCFMessage[] responses = agent.send(request);

            for (PCFMessage response : responses) {
                Map<String, Object> channelInfo = new HashMap<>();
                // 使用 getParameter 方法替代已棄用的 getStringParameterValue
                String channelName = (String) response.getParameter(CMQCFC.MQCACH_CHANNEL_NAME).getValue();

                // 排除清單
                if (StringUtils.startsWithAny(channelName, "SYSTEM.", "AMQ.")) {
                    continue;
                }

                channelInfo.put("name", channelName);

                // 獲取通道狀態
                PCFMessage statusRequest = new PCFMessage(CMQCFC.MQCMD_INQUIRE_CHANNEL_STATUS);
                statusRequest.addParameter(CMQCFC.MQCACH_CHANNEL_NAME, channelName);

                try {
                    PCFMessage[] statusResponses = agent.send(statusRequest);
                    if (statusResponses.length > 0) {
                        PCFMessage statusResponse = statusResponses[0];
                        // 使用 getParameter 方法替代已棄用的 getIntParameterValue
                        int status = ((Integer) statusResponse.getParameter(CMQCFC.MQIACH_CHANNEL_STATUS).getValue());
                        String statusText = getChannelStatusText(status);

                        channelInfo.put("status", statusText);
                        channelInfo.put("active", status == CMQCFC.MQCHS_RUNNING);
                    } else {
                        channelInfo.put("status", "非作用中");
                        channelInfo.put("active", false);
                    }
                } catch (MQException e) {
                    channelInfo.put("status", "非作用中");
                    channelInfo.put("active", false);
                }

                channelsStatus.add(channelInfo);
            }
        } catch (MQException |

                IOException e) {
            log.error("獲取通道狀態時發生錯誤: {}", e.getMessage(), e);
        } finally {
            if (agent != null) {
                try {
                    agent.disconnect();
                } catch (MQException e) {
                    log.error("關閉 PCF 代理時發生錯誤: {}", e.getMessage(), e);
                }
            }
        }

        return channelsStatus;
    }

    /**
     * 將通道狀態代碼轉換為文字描述
     */
    private String getChannelStatusText(int status) {
        switch (status) {
            case CMQCFC.MQCHS_BINDING:
                return "正在綁定";
            case CMQCFC.MQCHS_STARTING:
                return "正在啟動";
            case CMQCFC.MQCHS_RUNNING:
                return "運行中";
            case CMQCFC.MQCHS_STOPPING:
                return "正在停止";
            case CMQCFC.MQCHS_RETRYING:
                return "重試中";
            case CMQCFC.MQCHS_STOPPED:
                return "已停止";
            case CMQCFC.MQCHS_PAUSED:
                return "已暫停";
            case CMQCFC.MQCHS_INITIALIZING:
                return "初始化中";
            default:
                return "未知狀態(" + status + ")";
        }
    }
}

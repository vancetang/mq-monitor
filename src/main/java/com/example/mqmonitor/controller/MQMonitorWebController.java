package com.example.mqmonitor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mqmonitor.service.MQConnectionService;
import com.example.mqmonitor.service.MQPCFService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class MQMonitorWebController {

    @Autowired
    private MQPCFService mqpcfService;

    @Autowired
    private MQConnectionService mqConnectionService;

    @GetMapping("/")
    public String index(Model model) {
        log.debug("請求獲取首頁");

        model.addAttribute("queueManager", mqpcfService.getQueueManagerStatus());
        model.addAttribute("queues", mqpcfService.getQueuesStatus());
        model.addAttribute("channels", mqpcfService.getChannelsStatus());

        return "index";
    }

    /**
     * 手動重新連接到 MQ
     *
     * @param model              模型
     * @param redirectAttributes 重定向屬性
     * @return 首頁或重定向到首頁
     */
    @PostMapping("/reconnect")
    public String reconnect(Model model, RedirectAttributes redirectAttributes) {
        log.info("Web 請求手動重新連接到 MQ");

        boolean reconnected = mqConnectionService.reconnect();

        if (reconnected) {
            // 直接渲染頁面而不是重定向，避免在重定向過程中出現錯誤
            model.addAttribute("successMessage", "已成功重新連接到 MQ");
            log.info("手動重新連接到 MQ 成功");

            // 立即獲取最新的 MQ 狀態
            model.addAttribute("queueManager", mqpcfService.getQueueManagerStatus());
            model.addAttribute("queues", mqpcfService.getQueuesStatus());
            model.addAttribute("channels", mqpcfService.getChannelsStatus());

            return "index";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "重新連接到 MQ 失敗");
            log.error("手動重新連接到 MQ 失敗");
            return "redirect:/";
        }
    }
}

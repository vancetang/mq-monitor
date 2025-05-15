package com.example.mqmonitor.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.mqmonitor.service.MQPCFService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/")
public class HomeController {

    @Autowired
    private MQPCFService mqpcfService;

    @GetMapping({ "", "index" })
    public String index(Model model) {
        log.debug("請求獲取首頁");

        model.addAttribute("queueManager", mqpcfService.getQueueManagerStatus());
        model.addAttribute("queues", mqpcfService.getQueuesStatus());
        model.addAttribute("channels", mqpcfService.getChannelsStatus());

        return "index";
    }
}

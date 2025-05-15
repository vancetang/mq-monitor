package com.example.mqmonitor.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.mqmonitor.service.PdfReportService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private PdfReportService pdfReportService;

    /**
     * 顯示 PDF 預覽頁面
     */
    @GetMapping("/view")
    public String viewReport(Model model) {
        log.info("請求顯示 PDF 預覽頁面");
        return "pdf-viewer";
    }

    /**
     * 生成 PDF 報表並返回
     */
    @GetMapping("/generate")
    public ResponseEntity<byte[]> generateReport() {
        log.info("請求生成 PDF 報表");

        try {
            byte[] pdfContent = pdfReportService.generateMQStatusReport();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "mq-status-report.pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("生成 PDF 報表時發生錯誤: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

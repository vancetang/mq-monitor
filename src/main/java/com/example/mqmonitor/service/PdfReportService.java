package com.example.mqmonitor.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PdfReportService {

    @Autowired
    private MQPCFService mqpcfService;

    // 字型定義
    private PdfFont kaiFont;
    private PdfFont mingFont;

    private static final int DEFAULT_MAX_DEPTH = 5000;

    /**
     * 生成 MQ 狀態報表 PDF
     *
     * @return PDF 檔案的位元組陣列
     */
    public byte[] generateMQStatusReport() {
        log.info("開始生成 MQ 狀態報表 PDF");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            // 初始化字型
            initFonts();

            // 創建 PDF 文件
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(36, 36, 36, 36);

            // 添加標題
            addTitle(document);

            // 添加報表生成時間
            addReportTime(document);

            // 添加 Queue Manager 狀態
            addQueueManagerStatus(document);

            // 添加隊列狀態
            addQueuesStatus(document);

            // 添加通道狀態
            addChannelsStatus(document);

            // 關閉文件
            document.close();

            log.info("MQ 狀態報表 PDF 生成完成");
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("生成 MQ 狀態報表 PDF 時發生錯誤: {}", e.getMessage(), e);
            throw new RuntimeException("生成 PDF 報表失敗", e);
        }
    }

    /**
     * 初始化字型
     */
    private void initFonts() throws IOException {
        // 使用 iText 內建的中文字型
        kaiFont = PdfFontFactory.createFont("C:\\Windows\\Fonts\\kaiu.ttf", PdfEncodings.IDENTITY_H,
                PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
        mingFont = PdfFontFactory.createFont("C:\\Windows\\Fonts\\mingliu.ttc,0", PdfEncodings.IDENTITY_H,
                PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
    }

    /**
     * 添加標題
     */
    private void addTitle(Document document) {
        Paragraph title = new Paragraph("IBM MQ 監控狀態報表")
                .setFont(kaiFont)
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold();
        document.add(title);
        document.add(new Paragraph("\n"));
    }

    /**
     * 添加報表生成時間
     */
    private void addReportTime(Document document) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        Paragraph reportTime = new Paragraph("報表生成時間: " + formattedDateTime)
                .setFont(kaiFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT);
        document.add(reportTime);
        document.add(new Paragraph("\n"));
    }

    /**
     * 添加 Queue Manager 狀態
     */
    private void addQueueManagerStatus(Document document) {
        Map<String, Object> queueManager = mqpcfService.getQueueManagerStatus();

        Paragraph sectionTitle = new Paragraph("Queue Manager 狀態")
                .setFont(kaiFont)
                .setFontSize(16)
                .setBold();
        document.add(sectionTitle);
        document.add(new Paragraph("\n"));

        Table table = new Table(UnitValue.createPercentArray(new float[] { 30, 70 }))
                .setWidth(UnitValue.createPercentValue(100));

        // 表頭
        table.addHeaderCell(createHeaderCell("項目"));
        table.addHeaderCell(createHeaderCell("狀態"));

        // 表格內容
        table.addCell(createCell("名稱"));
        table.addCell(createCell(queueManager.getOrDefault("name", "").toString()));

        table.addCell(createCell("狀態"));
        boolean connected = (boolean) queueManager.getOrDefault("connected", false);
        table.addCell(createCell(connected ? "正常運行" : "未連接"));

        if (connected) {
            table.addCell(createCell("啟動日期"));
            table.addCell(createCell(queueManager.getOrDefault("startDate", "").toString()));

            table.addCell(createCell("啟動時間"));
            table.addCell(createCell(queueManager.getOrDefault("startTime", "").toString()));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * 添加隊列狀態
     */
    private void addQueuesStatus(Document document) {
        List<Map<String, Object>> queues = mqpcfService.getQueuesStatus();

        Paragraph sectionTitle = new Paragraph("隊列狀態")
                .setFont(kaiFont)
                .setFontSize(16)
                .setBold();
        document.add(sectionTitle);
        document.add(new Paragraph("\n"));

        if (queues.isEmpty()) {
            Paragraph noData = new Paragraph("沒有可用的隊列資訊")
                    .setFont(kaiFont)
                    .setFontSize(12)
                    .setItalic();
            document.add(noData);
            document.add(new Paragraph("\n"));
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[] { 30, 15, 15, 20, 20 }))
                .setWidth(UnitValue.createPercentValue(100));

        // 表頭
        table.addHeaderCell(createHeaderCell("名稱"));
        table.addHeaderCell(createHeaderCell("類型"));
        table.addHeaderCell(createHeaderCell("深度 %"));
        table.addHeaderCell(createHeaderCell("深度上限"));
        table.addHeaderCell(createHeaderCell("現行連線"));

        // 表格內容
        for (Map<String, Object> queue : queues) {
            table.addCell(createCell(queue.getOrDefault("name", "").toString()));
            table.addCell(createCell("本地"));

            // 計算使用百分比
            int depth = (int) queue.getOrDefault("depth", 0);
            int maxDepth = (int) queue.getOrDefault("maxDepth", DEFAULT_MAX_DEPTH);

            int usagePercent = maxDepth > 0 ? (depth * 100 / maxDepth) : 0;
            table.addCell(createCell(usagePercent + "%"));

            table.addCell(createCell(depth + "/" + maxDepth));

            int openInputCount = (int) queue.getOrDefault("openInputCount", 0);
            int openOutputCount = (int) queue.getOrDefault("openOutputCount", 0);
            table.addCell(createCell("輸入 " + openInputCount + " ; 輸出 " + openOutputCount));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * 添加通道狀態
     */
    private void addChannelsStatus(Document document) {
        List<Map<String, Object>> channels = mqpcfService.getChannelsStatus();

        Paragraph sectionTitle = new Paragraph("通道狀態")
                .setFont(kaiFont)
                .setFontSize(16)
                .setBold();
        document.add(sectionTitle);
        document.add(new Paragraph("\n"));

        if (channels.isEmpty()) {
            Paragraph noData = new Paragraph("沒有可用的通道資訊")
                    .setFont(kaiFont)
                    .setFontSize(12)
                    .setItalic();
            document.add(noData);
            document.add(new Paragraph("\n"));
            return;
        }

        Paragraph channelCount = new Paragraph("可用通道總數: " + channels.size())
                .setFont(kaiFont)
                .setFontSize(12);
        document.add(channelCount);
        document.add(new Paragraph("\n"));

        Table table = new Table(UnitValue.createPercentArray(new float[] { 50, 50 }))
                .setWidth(UnitValue.createPercentValue(100));

        // 表頭
        table.addHeaderCell(createHeaderCell("通道名稱"));
        table.addHeaderCell(createHeaderCell("狀態"));

        // 表格內容
        for (Map<String, Object> channel : channels) {
            table.addCell(createCell(channel.getOrDefault("name", "").toString()));

            boolean active = (boolean) channel.getOrDefault("active", false);
            String status = channel.getOrDefault("status", "未知").toString();
            table.addCell(createCell(active ? "✓ " + status : status));
        }

        document.add(table);
    }

    /**
     * 創建表頭單元格
     */
    private Cell createHeaderCell(String text) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setFont(kaiFont).setFontSize(12))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBorder(new SolidBorder(ColorConstants.BLACK, 1))
                .setTextAlignment(TextAlignment.CENTER);
        return cell;
    }

    /**
     * 創建表格內容單元格
     */
    private Cell createCell(String text) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setFont(mingFont).setFontSize(10))
                .setBorder(new SolidBorder(ColorConstants.BLACK, 1))
                .setTextAlignment(TextAlignment.LEFT);
        return cell;
    }
}

<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MQ 狀態報表 - PDF 預覽</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <link href="/css/pdf-viewer.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/pdfjs-dist@3.11.174/build/pdf.min.js"></script>
</head>
<body>
    <div class="toolbar">
        <div class="toolbar-left">
            <button id="previous-page" title="上一頁">
                <i class="bi bi-chevron-left"></i> 上一頁
            </button>
            <div class="page-info">
                第 <input type="text" id="page-number" min="1" value="1"> 頁，共 <span id="page-count">0</span> 頁
            </div>
            <button id="next-page" title="下一頁">
                下一頁 <i class="bi bi-chevron-right"></i>
            </button>
        </div>

        <div class="toolbar-center">
            <div class="zoom-controls">
                <button id="zoom-out" title="縮小">-</button>
                <span id="zoom-level">100%</span>
                <button id="zoom-in" title="放大">+</button>
            </div>
        </div>

        <div class="toolbar-right">
            <button id="print-button" title="列印">
                <i class="bi bi-printer"></i> 列印
            </button>
            <button id="back-button" title="返回">
                <i class="bi bi-arrow-left"></i> 返回
            </button>
        </div>
    </div>

    <div class="pdf-container">
        <canvas id="pdf-viewer"></canvas>
    </div>

    <div id="loading" class="loading">載入中，請稍候...</div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
    <script src="/js/pdf-viewer.js"></script>
</body>
</html>

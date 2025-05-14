<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MQ 狀態報表 - PDF 預覽</title>

    <!-- PDF.js 官方 CSS -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/pdfjs-dist@3.11.174/web/pdf_viewer.css">

    <!-- 自定義樣式 -->
    <style>
        :root {
            --main-color: #0078d7;
            --button-hover-color: #005a9e;
            --button-active-color: #004275;
        }

        html, body {
            height: 100%;
            width: 100%;
            margin: 0;
            padding: 0;
        }

        #viewerContainer {
            position: absolute;
            top: 32px;
            bottom: 0;
            left: 0;
            right: 0;
            overflow: auto;
            background-color: #525659;
        }

        #viewer {
            position: relative;
        }

        .toolbar {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            height: 32px;
            background-color: #474747;
            color: white;
            display: flex;
            align-items: center;
            padding: 0 10px;
            z-index: 1000;
            box-shadow: 0 2px 5px rgba(0, 0, 0, 0.3);
        }

        .toolbar-left, .toolbar-center, .toolbar-right {
            display: flex;
            align-items: center;
        }

        .toolbar-left {
            flex: 1;
        }

        .toolbar-center {
            flex: 1;
            justify-content: center;
        }

        .toolbar-right {
            flex: 1;
            justify-content: flex-end;
        }

        .toolbar button {
            background-color: transparent;
            border: none;
            color: white;
            padding: 4px 8px;
            margin: 0 2px;
            cursor: pointer;
            border-radius: 2px;
        }

        .toolbar button:hover {
            background-color: var(--button-hover-color);
        }

        .toolbar button:active {
            background-color: var(--button-active-color);
        }

        .page-controls {
            display: flex;
            align-items: center;
        }

        #pageNumber {
            width: 40px;
            text-align: center;
            background-color: #333;
            color: white;
            border: 1px solid #555;
            border-radius: 2px;
            padding: 2px;
            margin: 0 5px;
        }

        .zoom-controls {
            display: flex;
            align-items: center;
            margin: 0 10px;
        }

        #zoomValue {
            margin: 0 5px;
            min-width: 40px;
            text-align: center;
        }

        #loadingBar {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 4px;
            background-color: rgba(0, 0, 0, 0.3);
            z-index: 1001;
            display: none;
        }

        #loadingBar .progress {
            width: 0%;
            height: 100%;
            background-color: var(--main-color);
            transition: width 0.2s;
        }

        /* 禁用右鍵選單 */
        #viewerContainer {
            -webkit-user-select: none;
            -moz-user-select: none;
            -ms-user-select: none;
            user-select: none;
        }

        /* 列印樣式 */
        @media print {
            .toolbar, #loadingBar {
                display: none !important;
            }

            #viewerContainer {
                position: static;
                overflow: visible;
                background-color: white;
            }

            .page {
                page-break-after: always;
                margin: 0;
                border: none;
                box-shadow: none;
            }

            .page:last-child {
                page-break-after: auto;
            }
        }
    </style>
</head>
<body>
    <div class="toolbar">
        <div class="toolbar-left">
            <div class="page-controls">
                <button id="previous" title="上一頁">
                    <span>上一頁</span>
                </button>
                <input type="number" id="pageNumber" min="1" value="1">
                <span>/</span>
                <span id="numPages">0</span>
                <button id="next" title="下一頁">
                    <span>下一頁</span>
                </button>
            </div>
        </div>

        <div class="toolbar-center">
            <div class="zoom-controls">
                <button id="zoomOut" title="縮小">-</button>
                <span id="zoomValue">100%</span>
                <button id="zoomIn" title="放大">+</button>
            </div>
        </div>

        <div class="toolbar-right">
            <button id="print" title="列印">
                <span>列印</span>
            </button>
            <button id="download" title="下載">
                <span>下載</span>
            </button>
            <button id="back" title="返回">
                <span>返回</span>
            </button>
        </div>
    </div>

    <div id="loadingBar">
        <div class="progress"></div>
    </div>

    <div id="viewerContainer">
        <div id="viewer" class="pdfViewer"></div>
    </div>

    <!-- PDF.js 官方 JS -->
    <script src="https://cdn.jsdelivr.net/npm/pdfjs-dist@3.11.174/build/pdf.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/pdfjs-dist@3.11.174/web/pdf_viewer.js"></script>

    <script>
        // 設定 PDF.js 的 worker 路徑
        pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdn.jsdelivr.net/npm/pdfjs-dist@3.11.174/build/pdf.worker.min.js';

        // 變數定義
        let pdfDoc = null;
        let pageNum = 1;
        let pageRendering = false;
        let pageNumPending = null;
        let scale = 1.0;
        let pdfViewer = null;
        let pdfLinkService = null;

        // DOM 元素
        const container = document.getElementById('viewerContainer');
        const viewer = document.getElementById('viewer');
        const pageNumberInput = document.getElementById('pageNumber');
        const numPages = document.getElementById('numPages');
        const previousButton = document.getElementById('previous');
        const nextButton = document.getElementById('next');
        const zoomInButton = document.getElementById('zoomIn');
        const zoomOutButton = document.getElementById('zoomOut');
        const zoomValueSpan = document.getElementById('zoomValue');
        const printButton = document.getElementById('print');
        const downloadButton = document.getElementById('download');
        const backButton = document.getElementById('back');
        const loadingBar = document.getElementById('loadingBar');
        const loadingBarProgress = loadingBar.querySelector('.progress');

        // 初始化 PDF 查看器
        function initPdfViewer() {
            // 創建 EventBus
            const eventBus = new pdfjsViewer.EventBus();

            // 創建 LinkService
            pdfLinkService = new pdfjsViewer.PDFLinkService({
                eventBus: eventBus,
            });

            // 創建 FindController
            const findController = new pdfjsViewer.PDFFindController({
                eventBus: eventBus,
                linkService: pdfLinkService,
            });

            // 創建 PdfViewer
            pdfViewer = new pdfjsViewer.PDFViewer({
                container: container,
                eventBus: eventBus,
                linkService: pdfLinkService,
                findController: findController,
                renderer: 'canvas',
                textLayerMode: 2, // 啟用文本層
                renderInteractiveForms: true,
            });

            pdfLinkService.setViewer(pdfViewer);

            // 監聽頁面變化事件
            eventBus.on('pagechanging', function(evt) {
                pageNum = evt.pageNumber;
                updateUI();
            });

            // 監聽縮放變化事件
            eventBus.on('scalechanging', function(evt) {
                scale = evt.scale;
                updateZoomValue();
            });

            // 監聽文檔載入進度
            eventBus.on('progress', function(evt) {
                const percent = Math.round(evt.loaded / evt.total * 100);
                loadingBarProgress.style.width = percent + '%';
            });

            // 監聽文檔載入完成
            eventBus.on('documentloaded', function() {
                loadingBar.style.display = 'none';
            });
        }

        // 載入 PDF 文件
        function loadPdf() {
            loadingBar.style.display = 'block';
            loadingBarProgress.style.width = '0%';

            // 從後端獲取 PDF 文件
            const loadingTask = pdfjsLib.getDocument('/report/generate');

            loadingTask.promise.then(function(pdf) {
                pdfDoc = pdf;
                numPages.textContent = pdf.numPages;

                // 設置文檔
                pdfViewer.setDocument(pdf);
                pdfLinkService.setDocument(pdf);

                // 更新 UI
                updateUI();
            }).catch(function(error) {
                console.error('載入 PDF 時發生錯誤:', error);
                alert('載入 PDF 失敗: ' + error.message);
                loadingBar.style.display = 'none';
            });
        }

        // 更新 UI
        function updateUI() {
            pageNumberInput.value = pageNum;

            // 更新按鈕狀態
            previousButton.disabled = pageNum <= 1;
            nextButton.disabled = pageNum >= pdfDoc.numPages;
        }

        // 更新縮放值顯示
        function updateZoomValue() {
            zoomValueSpan.textContent = Math.round(scale * 100) + '%';
        }

        // 切換到上一頁
        function onPrevPage() {
            if (pageNum <= 1) {
                return;
            }
            pageNum--;
            pdfViewer.currentPageNumber = pageNum;
        }

        // 切換到下一頁
        function onNextPage() {
            if (pageNum >= pdfDoc.numPages) {
                return;
            }
            pageNum++;
            pdfViewer.currentPageNumber = pageNum;
        }

        // 放大
        function onZoomIn() {
            scale = Math.min(5, scale * 1.1);
            pdfViewer.currentScale = scale;
            updateZoomValue();
        }

        // 縮小
        function onZoomOut() {
            scale = Math.max(0.1, scale / 1.1);
            pdfViewer.currentScale = scale;
            updateZoomValue();
        }

        // 列印 PDF
        function onPrint() {
            window.print();
        }

        // 下載 PDF
        function onDownload() {
            // 禁用下載功能
            alert('下載功能已被禁用');
        }

        // 返回上一頁
        function onBack() {
            // 嘗試關閉當前視窗，如果是通過 window.open() 打開的
            if (window.opener && !window.opener.closed) {
                window.close();
            } else {
                // 如果不是通過 window.open() 打開的，則導航到首頁
                window.location.href = '/';
            }
        }

        // 事件監聽器
        previousButton.addEventListener('click', onPrevPage);
        nextButton.addEventListener('click', onNextPage);
        zoomInButton.addEventListener('click', onZoomIn);
        zoomOutButton.addEventListener('click', onZoomOut);
        printButton.addEventListener('click', onPrint);
        downloadButton.addEventListener('click', onDownload);
        backButton.addEventListener('click', onBack);

        // 頁碼輸入框事件
        pageNumberInput.addEventListener('change', function() {
            const pageNumber = parseInt(this.value);
            if (pageNumber >= 1 && pageNumber <= pdfDoc.numPages) {
                pageNum = pageNumber;
                pdfViewer.currentPageNumber = pageNum;
            } else {
                this.value = pageNum;
            }
        });

        // 禁用右鍵選單
        document.addEventListener('contextmenu', function(e) {
            e.preventDefault();
            return false;
        });

        // 初始化
        initPdfViewer();
        loadPdf();
    </script>
</body>
</html>

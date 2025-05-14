// 設定 PDF.js 的 worker 路徑
pdfjsLib.GlobalWorkerOptions.workerSrc =
  "https://cdn.jsdelivr.net/npm/pdfjs-dist@3.11.174/build/pdf.worker.min.js";

// 變數定義
let pdfDoc = null;
let pageNum = 1;
let pageRendering = false;
let pageNumPending = null;
let scale = 2.0;
const canvas = document.getElementById("pdf-viewer");
const ctx = canvas.getContext("2d");
const loadingElement = document.getElementById("loading");

// 載入 PDF 文件
function loadPDF() {
  loadingElement.style.display = "block";

  // 從後端獲取 PDF 文件
  pdfjsLib
    .getDocument("/report/generate")
    .promise.then(function (pdf) {
      pdfDoc = pdf;
      document.getElementById("page-count").textContent = pdf.numPages;

      // 初始渲染第一頁
      renderPage(pageNum);
    })
    .catch(function (error) {
      console.error("載入 PDF 時發生錯誤:", error);
      loadingElement.textContent = "載入 PDF 失敗: " + error.message;
    });
}

// 渲染指定頁面
function renderPage(num) {
  pageRendering = true;

  // 獲取頁面
  pdfDoc.getPage(num).then(function (page) {
    // 根據視窗大小調整比例
    const viewport = page.getViewport({ scale: scale });
    canvas.height = viewport.height;
    canvas.width = viewport.width;

    // 渲染 PDF 頁面
    const renderContext = {
      canvasContext: ctx,
      viewport: viewport,
    };

    const renderTask = page.render(renderContext);

    // 等待渲染完成
    renderTask.promise.then(function () {
      pageRendering = false;
      loadingElement.style.display = "none";

      if (pageNumPending !== null) {
        // 如果有待處理的頁面，渲染它
        renderPage(pageNumPending);
        pageNumPending = null;
      }
    });
  });

  // 更新頁碼輸入框
  document.getElementById("page-number").value = num;
}

// 切換到上一頁
function previousPage() {
  if (pageNum <= 1) {
    return;
  }
  pageNum--;
  queueRenderPage(pageNum);
}

// 切換到下一頁
function nextPage() {
  if (pageNum >= pdfDoc.numPages) {
    return;
  }
  pageNum++;
  queueRenderPage(pageNum);
}

// 將頁面渲染請求加入隊列
function queueRenderPage(num) {
  if (pageRendering) {
    pageNumPending = num;
  } else {
    renderPage(num);
  }
}

// 放大
function zoomIn() {
  scale += 0.1;
  updateZoomLevel();
  queueRenderPage(pageNum);
}

// 縮小
function zoomOut() {
  if (scale <= 0.2) return;
  scale -= 0.1;
  updateZoomLevel();
  queueRenderPage(pageNum);
}

// 更新縮放比例顯示
function updateZoomLevel() {
  document.getElementById("zoom-level").textContent =
    Math.round(scale * 100) + "%";
}

// 列印 PDF
function printPDF() {
  window.print();
}

// 返回上一頁
function goBack() {
  // 嘗試關閉當前視窗，如果是通過 window.open() 打開的
  if (window.opener && !window.opener.closed) {
    window.close();
  } else {
    // 如果不是通過 window.open() 打開的，則導航到首頁
    window.location.href = "/";
  }
}

// 事件監聽器
document
  .getElementById("previous-page")
  .addEventListener("click", previousPage);
document.getElementById("next-page").addEventListener("click", nextPage);
document.getElementById("zoom-in").addEventListener("click", zoomIn);
document.getElementById("zoom-out").addEventListener("click", zoomOut);
document.getElementById("print-button").addEventListener("click", printPDF);
document.getElementById("back-button").addEventListener("click", goBack);

// 頁碼輸入框事件
document.getElementById("page-number").addEventListener("change", function () {
  const pageNumber = parseInt(this.value);
  if (pageNumber >= 1 && pageNumber <= pdfDoc.numPages) {
    pageNum = pageNumber;
    queueRenderPage(pageNum);
  } else {
    this.value = pageNum;
  }
});

// 禁用右鍵選單
document.addEventListener("contextmenu", function (e) {
  e.preventDefault();
  return false;
});

// 初始化
loadPDF();

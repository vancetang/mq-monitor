<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>IBM MQ 監控儀表板</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .card {
            margin-bottom: 20px;
        }
        .status-ok {
            color: green;
        }
        .status-warning {
            color: orange;
        }
        .status-error {
            color: red;
        }
        .refresh-btn {
            margin-bottom: 20px;
        }
        .refresh-controls {
            display: flex;
            align-items: center;
            margin-bottom: 20px;
            gap: 10px;
        }
        .countdown {
            margin-left: 15px;
            font-weight: bold;
        }
    </style>
</head>
<body>
    <div class="container mt-4">
        <h1 class="mb-4">IBM MQ 監控儀表板</h1>

        <div class="refresh-controls mb-2">
            <button id="refreshBtn" class="btn btn-primary">立即重新整理</button>

            <button id="reconnectBtn" class="btn btn-warning ms-2">重新連接 MQ</button>

            <button id="generateReportBtn" class="btn btn-success ms-2">產生報表</button>

            <div class="form-check form-switch ms-3">
                <input class="form-check-input" type="checkbox" id="autoRefreshToggle" checked>
                <label class="form-check-label" for="autoRefreshToggle">自動重新整理</label>
            </div>

            <select id="refreshInterval" class="form-select" style="width: auto;">
                <option value="10000">10 秒</option>
                <option value="30000" selected>30 秒</option>
                <option value="60000">1 分鐘</option>
                <option value="300000">5 分鐘</option>
            </select>

            <div id="countdown" class="countdown">剩餘時間: 30 秒</div>
        </div>

        <div class="d-flex justify-content-end mb-3">
            <div id="lastRefreshTime" class="text-muted small">最後刷新時間: <span id="lastRefreshTimeValue">-</span></div>
        </div>

        <#if successMessage??>
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                ${successMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </#if>

        <#if errorMessage??>
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                ${errorMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </#if>

        <!-- Queue Manager 狀態 -->
        <div class="card">
            <div class="card-header">
                <h2>Queue Manager 狀態</h2>
            </div>
            <div class="card-body">
                <#if queueManager?? && queueManager.connected?? && queueManager.connected>
                    <div class="alert alert-success">
                        <h4><span class="status-ok">✓</span> Queue Manager: ${queueManager.name!} 正常運行中</h4>
                        <p>啟動時間: ${queueManager.startDate!} ${queueManager.startTime!}</p>
                    </div>
                <#else>
                    <div class="alert alert-danger">
                        <h4><span class="status-error">✗</span> Queue Manager 未連接</h4>
                        <p>狀態: ${(queueManager.status)!"連線中斷"}</p>
                    </div>
                </#if>
            </div>
        </div>

        <!-- 隊列狀態 -->
        <div class="card">
            <div class="card-header">
                <h2>隊列狀態</h2>
            </div>
            <div class="card-body">
                <#if queues?? && queues?size gt 0>
                    <div class="table-responsive">
                        <table class="table table-striped table-hover">
                            <thead>
                                <tr>
                                    <th>名稱</th>
                                    <th>類型</th>
                                    <th>深度 %</th>
                                    <th>深度上限</th>
                                    <th>現行連線</th>
                                </tr>
                            </thead>
                            <tbody>
                                <#list queues as queue>
                                    <tr>
                                        <td>${queue.name!}</td>
                                        <td>本地</td>
                                        <td>
                                            <#if queue?? && queue.depth?? && queue.maxDepth?? && queue.maxDepth gt 0>
                                                <#assign usagePercent = (queue.depth / queue.maxDepth) * 100>
                                                ${usagePercent?string["0"]}%
                                            <#else>
                                                0%
                                            </#if>
                                        </td>
                                        <td>
                                            <#if queue?? && queue.maxDepth??>
                                                ${queue.depth!}/${queue.maxDepth!}
                                            <#else>
                                                0/5000
                                            </#if>
                                        </td>
                                        <td>
                                            輸入 ${(queue.openInputCount)!0} ; 輸出 ${(queue.openOutputCount)!0}
                                        </td>
                                    </tr>
                                </#list>
                            </tbody>
                        </table>
                    </div>
                <#else>
                    <div class="alert alert-warning">
                        <p>沒有可用的隊列資訊</p>
                    </div>
                </#if>
            </div>
        </div>

        <!-- 通道狀態 -->
        <div class="card">
            <div class="card-header">
                <h2>通道狀態</h2>
            </div>
            <div class="card-body">
                <#if channels?? && channels?size gt 0>
                    <div class="alert alert-info">
                        <p>可用通道總數: ${channels?size}</p>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-striped table-hover">
                            <thead>
                                <tr>
                                    <th>通道名稱</th>
                                    <th>狀態</th>
                                </tr>
                            </thead>
                            <tbody>
                                <#list channels as channel>
                                    <tr>
                                        <td>${channel.name!}</td>
                                        <td>
                                            <#if channel?? && channel.active?? && channel.active>
                                                <span class="status-ok">✓ ${channel.status!}</span>
                                            <#else>
                                                <span class="status-warning">${(channel.status)!"未知"}</span>
                                            </#if>
                                        </td>
                                    </tr>
                                </#list>
                            </tbody>
                        </table>
                    </div>
                <#else>
                    <div class="alert alert-warning">
                        <p>沒有可用的通道資訊</p>
                    </div>
                </#if>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
    <script>
        // 變數定義
        let autoRefreshEnabled = true;
        let refreshInterval = 30000; // 預設 30 秒
        let countdownTimer;
        let remainingTime;
        let lastRefreshTime = new Date();

        // DOM 元素
        const refreshBtn = document.getElementById('refreshBtn');
        const reconnectBtn = document.getElementById('reconnectBtn');
        const generateReportBtn = document.getElementById('generateReportBtn');
        const autoRefreshToggle = document.getElementById('autoRefreshToggle');
        const refreshIntervalSelect = document.getElementById('refreshInterval');
        const countdownElement = document.getElementById('countdown');
        const lastRefreshTimeValue = document.getElementById('lastRefreshTimeValue');

        // 初始化
        function init() {
            // 設定初始值
            refreshInterval = parseInt(refreshIntervalSelect.value);

            // 事件監聽器
            refreshBtn.addEventListener('click', refreshPage);
            reconnectBtn.addEventListener('click', reconnectMQ);
            generateReportBtn.addEventListener('click', generateReport);
            autoRefreshToggle.addEventListener('change', toggleAutoRefresh);
            refreshIntervalSelect.addEventListener('change', changeRefreshInterval);

            // 更新最後刷新時間
            updateLastRefreshTime();

            // 開始自動刷新
            startAutoRefresh();
        }

        // 更新最後刷新時間
        function updateLastRefreshTime() {
            lastRefreshTime = new Date();
            const options = {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit',
                hour12: false
            };
            lastRefreshTimeValue.textContent = lastRefreshTime.toLocaleString('zh-TW', options);
        }

        // 重新連接 MQ
        function reconnectMQ() {
            // 禁用按鈕，避免重複點擊
            reconnectBtn.disabled = true;
            reconnectBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 連接中...';

            // 發送 AJAX 請求
            axios.post('/api/mq/reconnect')
                .then(function (response) {
                    // 處理成功回應
                    if (response.data.success) {
                        showAlert('success', response.data.message);
                        // 重新載入 MQ 狀態
                        updateMQStatus();
                    } else {
                        showAlert('danger', response.data.message);
                    }
                })
                .catch(function (error) {
                    // 處理錯誤
                    showAlert('danger', '重新連接 MQ 時發生錯誤: ' + error.message);
                })
                .finally(function () {
                    // 恢復按鈕狀態
                    reconnectBtn.disabled = false;
                    reconnectBtn.innerHTML = '重新連接 MQ';
                });
        }

        // 更新 MQ 狀態
        function updateMQStatus() {
            axios.get('/api/mq/status')
                .then(function (response) {
                    // 更新 Queue Manager 狀態
                    updateQueueManagerStatus(response.data.queueManager);
                    // 更新隊列狀態
                    updateQueuesStatus(response.data.queues);
                    // 更新通道狀態
                    updateChannelsStatus(response.data.channels);
                    // 更新最後刷新時間
                    updateLastRefreshTime();
                })
                .catch(function (error) {
                    console.error('獲取 MQ 狀態時發生錯誤:', error);
                });
        }

        // 更新 Queue Manager 狀態
        function updateQueueManagerStatus(queueManager) {
            const qmStatusContainer = document.querySelector('.card:nth-child(1) .card-body');
            if (!queueManager || !queueManager.connected) {
                qmStatusContainer.innerHTML =
                    '<div class="alert alert-danger">' +
                        '<h4><span class="status-error">✗</span> Queue Manager 未連接</h4>' +
                        '<p>狀態: ' + (queueManager ? queueManager.status : '連線中斷') + '</p>' +
                    '</div>';
            } else {
                qmStatusContainer.innerHTML =
                    '<div class="alert alert-success">' +
                        '<h4><span class="status-ok">✓</span> Queue Manager: ' + (queueManager.name || '') + ' 正常運行中</h4>' +
                        '<p>啟動日期: ' + (queueManager.startDate || '')  + (queueManager.startTime || '') + '</p>' +
                    '</div>';
            }
        }

        // 更新隊列狀態
        function updateQueuesStatus(queues) {
            const queuesContainer = document.querySelector('.card:nth-child(2) .card-body');
            if (!queues || queues.length === 0) {
                queuesContainer.innerHTML =
                    '<div class="alert alert-warning">' +
                        '<p>沒有可用的隊列資訊</p>' +
                    '</div>';
                return;
            }

            let tableHtml =
                '<div class="table-responsive">' +
                    '<table class="table table-striped table-hover">' +
                        '<thead>' +
                            '<tr>' +
                                '<th>名稱</th>' +
                                '<th>類型</th>' +
                                '<th>深度 %</th>' +
                                '<th>深度上限</th>' +
                                '<th>現行連線</th>' +
                            '</tr>' +
                        '</thead>' +
                        '<tbody>';

            queues.forEach(queue => {
                let usagePercent = 0;
                if (queue.depth != null && queue.maxDepth != null && queue.maxDepth > 0) {
                    usagePercent = Math.floor((queue.depth / queue.maxDepth) * 100);
                }

                tableHtml +=
                    '<tr>' +
                        '<td>' + (queue.name || '') + '</td>' +
                        '<td>本地</td>' +
                        '<td>' + usagePercent + '%</td>' +
                        '<td>' + (queue.depth != null ? queue.depth : 0) + '/' + (queue.maxDepth != null ? queue.maxDepth : 5000) + '</td>' +
                        '<td>輸入 ' + (queue.openInputCount || 0) + ' ; 輸出 ' + (queue.openOutputCount || 0) + '</td>' +
                    '</tr>';
            });

            tableHtml +=
                        '</tbody>' +
                    '</table>' +
                '</div>';

            queuesContainer.innerHTML = tableHtml;
        }

        // 更新通道狀態
        function updateChannelsStatus(channels) {
            const channelsContainer = document.querySelector('.card:nth-child(3) .card-body');
            if (!channels || channels.length === 0) {
                channelsContainer.innerHTML =
                    '<div class="alert alert-warning">' +
                        '<p>沒有可用的通道資訊</p>' +
                    '</div>';
                return;
            }

            let html =
                '<div class="alert alert-info">' +
                    '<p>可用通道總數: ' + channels.length + '</p>' +
                '</div>' +
                '<div class="table-responsive">' +
                    '<table class="table table-striped table-hover">' +
                        '<thead>' +
                            '<tr>' +
                                '<th>通道名稱</th>' +
                                '<th>狀態</th>' +
                            '</tr>' +
                        '</thead>' +
                        '<tbody>';

            channels.forEach(channel => {
                html +=
                    '<tr>' +
                        '<td>' + (channel.name || '') + '</td>' +
                        '<td>' +
                            (channel.active ?
                                '<span class="status-ok">✓ ' + (channel.status || '') + '</span>' :
                                '<span class="status-warning">' + (channel.status || '未知') + '</span>') +
                        '</td>' +
                    '</tr>';
            });

            html +=
                        '</tbody>' +
                    '</table>' +
                '</div>';

            channelsContainer.innerHTML = html;
        }

        // 顯示提示訊息
        function showAlert(type, message) {
            // 創建提示元素
            const alertDiv = document.createElement('div');
            alertDiv.className = 'alert alert-' + type + ' alert-dismissible fade show';
            alertDiv.role = 'alert';
            alertDiv.innerHTML =
                message +
                '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>';

            // 插入到頁面中
            const container = document.querySelector('.container');
            container.insertBefore(alertDiv, document.querySelector('.card'));

            // 5秒後自動關閉
            setTimeout(() => {
                alertDiv.classList.remove('show');
                setTimeout(() => alertDiv.remove(), 150);
            }, 5000);
        }

        // 刷新頁面
        function refreshPage() {
            // 更新最後刷新時間
            updateLastRefreshTime();
            location.reload();
        }

        // 產生報表
        function generateReport() {
            // 打開 PDF 預覽頁面
            window.open('/report/view', '_blank');
        }

        // 切換自動刷新
        function toggleAutoRefresh() {
            autoRefreshEnabled = autoRefreshToggle.checked;

            if (autoRefreshEnabled) {
                startAutoRefresh();
            } else {
                stopAutoRefresh();
            }
        }

        // 更改刷新間隔
        function changeRefreshInterval() {
            refreshInterval = parseInt(refreshIntervalSelect.value);

            if (autoRefreshEnabled) {
                stopAutoRefresh();
                startAutoRefresh();
            }
        }

        // 開始自動刷新
        function startAutoRefresh() {
            if (!autoRefreshEnabled) return;

            // 清除現有計時器
            if (countdownTimer) {
                clearInterval(countdownTimer);
            }

            // 設定新的刷新計時器
            remainingTime = refreshInterval;
            updateCountdown();

            // 設定倒數計時器
            countdownTimer = setInterval(function() {
                remainingTime -= 1000;

                if (remainingTime <= 0) {
                    refreshPage();
                } else {
                    updateCountdown();
                }
            }, 1000);
        }

        // 停止自動刷新
        function stopAutoRefresh() {
            if (countdownTimer) {
                clearInterval(countdownTimer);
                countdownTimer = null;
            }
            countdownElement.textContent = '自動刷新已停用';
        }

        // 更新倒數計時顯示
        function updateCountdown() {
            let seconds = Math.floor(remainingTime / 1000);
            let minutes = Math.floor(seconds / 60);
            seconds = seconds % 60;

            let displayText = '剩餘時間: ';
            if (minutes > 0) {
                displayText += minutes + ' 分 ';
            }
            displayText += seconds + ' 秒';

            countdownElement.textContent = displayText;
        }

        // 初始化頁面
        init();
    </script>
</body>
</html>

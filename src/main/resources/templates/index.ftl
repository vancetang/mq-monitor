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

        <div class="refresh-controls">
            <button id="refreshBtn" class="btn btn-primary">立即重新整理</button>

            <form action="/reconnect" method="post" class="d-inline ms-2">
                <button type="submit" class="btn btn-warning">重新連接 MQ</button>
            </form>

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
                        <p>啟動日期: ${queueManager.startDate!}</p>
                        <p>啟動時間: ${queueManager.startTime!}</p>
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
    <script>
        // 變數定義
        let autoRefreshEnabled = true;
        let refreshInterval = 30000; // 預設 30 秒
        let countdownTimer;
        let remainingTime;

        // DOM 元素
        const refreshBtn = document.getElementById('refreshBtn');
        const autoRefreshToggle = document.getElementById('autoRefreshToggle');
        const refreshIntervalSelect = document.getElementById('refreshInterval');
        const countdownElement = document.getElementById('countdown');

        // 初始化
        function init() {
            // 設定初始值
            refreshInterval = parseInt(refreshIntervalSelect.value);

            // 事件監聽器
            refreshBtn.addEventListener('click', refreshPage);
            autoRefreshToggle.addEventListener('change', toggleAutoRefresh);
            refreshIntervalSelect.addEventListener('change', changeRefreshInterval);

            // 開始自動刷新
            startAutoRefresh();
        }

        // 刷新頁面
        function refreshPage() {
            location.reload();
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

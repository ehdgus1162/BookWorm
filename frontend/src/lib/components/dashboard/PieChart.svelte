<!-- src/lib/components/dashboard/PieChart.svelte -->
<script>
    export let data = { borrowed: 340, returned: 1160 };
    export let size = 200;
    export let strokeWidth = 20;
    export let showPercentages = true;
    export let showLegend = true;
    export let centerIcon = '📊';
    export let colors = {
        borrowed: '#374151',
        returned: '#e5e7eb'
    };

    // 계산된 값들
    $: total = data.borrowed + data.returned;
    $: borrowedPercentage = total > 0 ? (data.borrowed / total * 100).toFixed(1) : 0;
    $: returnedPercentage = total > 0 ? (data.returned / total * 100).toFixed(1) : 0;

    // SVG 원 속성
    $: radius = (size - strokeWidth) / 2;
    $: circumference = radius * 2 * Math.PI;
    $: borrowedDashArray = total > 0 ? (data.borrowed / total) * circumference : 0;
    $: borrowedDashOffset = 0;
    $: returnedDashArray = total > 0 ? (data.returned / total) * circumference : 0;
    $: returnedDashOffset = -borrowedDashArray;

    // 중심점
    $: center = size / 2;
</script>

<div class="pie-chart-container">
    <div class="chart-wrapper" style="width: {size}px; height: {size}px;">
        <svg
                viewBox="0 0 {size} {size}"
                class="pie-chart-svg"
                role="img"
                aria-label="Pie chart showing borrowed vs returned books"
        >
            <!-- 배경 원 -->
            <circle
                    cx={center}
                    cy={center}
                    r={radius}
                    fill="none"
                    stroke="#f1f5f9"
                    stroke-width={strokeWidth}
                    class="background-circle"
            />

            <!-- 반납된 책 (회색) -->
            <circle
                    cx={center}
                    cy={center}
                    r={radius}
                    fill="none"
                    stroke={colors.returned}
                    stroke-width={strokeWidth}
                    stroke-dasharray="{returnedDashArray} {circumference}"
                    stroke-dashoffset={returnedDashOffset}
                    transform="rotate(-90 {center} {center})"
                    class="chart-segment returned"
                    stroke-linecap="round"
            />

            <!-- 대출된 책 (어두운 색) -->
            <circle
                    cx={center}
                    cy={center}
                    r={radius}
                    fill="none"
                    stroke={colors.borrowed}
                    stroke-width={strokeWidth}
                    stroke-dasharray="{borrowedDashArray} {circumference}"
                    stroke-dashoffset={borrowedDashOffset}
                    transform="rotate(-90 {center} {center})"
                    class="chart-segment borrowed"
                    stroke-linecap="round"
            />
        </svg>

        <!-- 중앙 아이콘 -->
        <div class="chart-center">
            <div class="center-icon" aria-hidden="true">{centerIcon}</div>
            {#if showPercentages}
                <div class="center-percentage">
                    <span class="percentage-value">{borrowedPercentage}%</span>
                    <span class="percentage-label">Borrowed</span>
                </div>
            {/if}
        </div>
    </div>

    {#if showLegend}
        <div class="chart-legend">
            <div class="legend-item">
                <div class="legend-dot borrowed" style="background-color: {colors.borrowed}"></div>
                <div class="legend-content">
                    <span class="legend-label">Total Borrowed Books</span>
                    <span class="legend-value">{data.borrowed.toLocaleString()}</span>
                </div>
            </div>

            <div class="legend-item">
                <div class="legend-dot returned" style="background-color: {colors.returned}"></div>
                <div class="legend-content">
                    <span class="legend-label">Total Returned Books</span>
                    <span class="legend-value">{data.returned.toLocaleString()}</span>
                </div>
            </div>
        </div>
    {/if}
</div>

<style>
    .pie-chart-container {
        display: flex;
        align-items: center;
        gap: 2rem;
        background: white;
        border-radius: 12px;
        padding: 2rem;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        border: 1px solid #f1f5f9;
    }

    .chart-wrapper {
        position: relative;
        flex-shrink: 0;
    }

    .pie-chart-svg {
        width: 100%;
        height: 100%;
        transform: scale(1);
        transition: transform 0.3s ease;
    }

    .pie-chart-svg:hover {
        transform: scale(1.02);
    }

    .chart-segment {
        transition: all 0.3s ease;
    }

    .chart-segment.borrowed {
        filter: drop-shadow(0 2px 4px rgba(55, 65, 81, 0.3));
    }

    .chart-center {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        text-align: center;
        pointer-events: none;
    }

    .center-icon {
        font-size: 2rem;
        margin-bottom: 0.5rem;
        display: block;
    }

    .center-percentage {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 0.125rem;
    }

    .percentage-value {
        font-size: 1.25rem;
        font-weight: 700;
        color: #1e293b;
        line-height: 1;
    }

    .percentage-label {
        font-size: 0.75rem;
        color: #64748b;
        font-weight: 500;
        text-transform: uppercase;
        letter-spacing: 0.05em;
    }

    .chart-legend {
        display: flex;
        flex-direction: column;
        gap: 1rem;
        flex: 1;
    }

    .legend-item {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        padding: 0.75rem;
        border-radius: 8px;
        background: #f8fafc;
        border: 1px solid #f1f5f9;
        transition: all 0.2s ease;
    }

    .legend-item:hover {
        background: #f1f5f9;
        border-color: #e2e8f0;
    }

    .legend-dot {
        width: 16px;
        height: 16px;
        border-radius: 50%;
        flex-shrink: 0;
    }

    .legend-content {
        display: flex;
        flex-direction: column;
        gap: 0.125rem;
        flex: 1;
    }

    .legend-label {
        font-size: 0.875rem;
        color: #374151;
        font-weight: 500;
        line-height: 1.3;
    }

    .legend-value {
        font-size: 1.125rem;
        font-weight: 700;
        color: #1e293b;
        line-height: 1;
    }

    /* 반응형 */
    @media (max-width: 1024px) {
        .pie-chart-container {
            flex-direction: column;
            gap: 1.5rem;
            text-align: center;
        }

        .chart-legend {
            flex-direction: row;
            justify-content: center;
            max-width: 100%;
        }
    }

    @media (max-width: 768px) {
        .pie-chart-container {
            padding: 1.5rem;
            gap: 1rem;
        }

        .chart-wrapper {
            width: 160px !important;
            height: 160px !important;
        }

        .center-icon {
            font-size: 1.5rem;
        }

        .percentage-value {
            font-size: 1rem;
        }

        .percentage-label {
            font-size: 0.7rem;
        }

        .chart-legend {
            flex-direction: column;
            gap: 0.75rem;
        }

        .legend-item {
            padding: 0.5rem;
        }

        .legend-label {
            font-size: 0.8rem;
        }

        .legend-value {
            font-size: 1rem;
        }
    }

    @media (max-width: 480px) {
        .chart-wrapper {
            width: 140px !important;
            height: 140px !important;
        }

        .center-icon {
            font-size: 1.25rem;
        }

        .chart-legend {
            gap: 0.5rem;
        }

        .legend-dot {
            width: 12px;
            height: 12px;
        }
    }

    /* 접근성 */
    @media (prefers-reduced-motion: reduce) {
        .pie-chart-svg,
        .chart-segment,
        .legend-item {
            transition: none;
        }

        .pie-chart-svg:hover {
            transform: scale(1);
        }
    }

    /* 다크모드 */
    @media (prefers-color-scheme: dark) {
        .pie-chart-container {
            background: #1e293b;
            border-color: #334155;
        }

        .percentage-value {
            color: #f1f5f9;
        }

        .percentage-label {
            color: #94a3b8;
        }

        .legend-item {
            background: #334155;
            border-color: #475569;
        }

        .legend-item:hover {
            background: #475569;
            border-color: #64748b;
        }

        .legend-label {
            color: #e2e8f0;
        }

        .legend-value {
            color: #f1f5f9;
        }
    }

    /* 프린트 */
    @media print {
        .pie-chart-container {
            box-shadow: none;
            border: 1px solid #000;
            break-inside: avoid;
        }

        .chart-segment {
            filter: none;
        }
    }

    /* 고해상도 */
    @media (-webkit-min-device-pixel-ratio: 2), (min-resolution: 192dpi) {
        .pie-chart-svg {
            image-rendering: -webkit-optimize-contrast;
            image-rendering: crisp-edges;
        }
    }
</style>
<!-- src/lib/components/dashboard/StatCard.svelte -->
<script>
    export let icon = '';
    export let number = '';
    export let label = '';
    export let color = '#3b82f6';
    export let loading = false;
    export let trend = null; // { value: 12, direction: 'up' } or null
</script>

<div class="stat-card" class:loading>
    <div class="stat-icon" style="background: {color}">
        {#if loading}
            <div class="icon-loading"></div>
        {:else}
            <span class="icon" aria-hidden="true">{icon}</span>
        {/if}
    </div>

    <div class="stat-content">
        <div class="stat-number">
            {#if loading}
                <div class="number-skeleton"></div>
            {:else}
                {number}
            {/if}
        </div>

        <div class="stat-label">{label}</div>

        {#if trend && !loading}
            <div class="stat-trend" class:trend-up={trend.direction === 'up'} class:trend-down={trend.direction === 'down'}>
                <span class="trend-icon" aria-hidden="true">
                    {trend.direction === 'up' ? '↗' : '↘'}
                </span>
                <span class="trend-value">{trend.value}%</span>
                <span class="trend-text">vs last month</span>
            </div>
        {/if}
    </div>
</div>

<style>
    .stat-card {
        background: white;
        border-radius: 12px;
        padding: 1.5rem;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        border: 1px solid #f1f5f9;
        display: flex;
        align-items: center;
        gap: 1rem;
        transition: all 0.2s ease;
        position: relative;
        overflow: hidden;
    }

    .stat-card:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    }

    .stat-card.loading {
        pointer-events: none;
    }

    .stat-icon {
        width: 64px;
        height: 64px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.75rem;
        color: white;
        flex-shrink: 0;
        position: relative;
        overflow: hidden;
    }

    .icon {
        z-index: 1;
    }

    .icon-loading {
        width: 24px;
        height: 24px;
        border: 2px solid rgba(255, 255, 255, 0.3);
        border-top: 2px solid white;
        border-radius: 50%;
        animation: spin 1s linear infinite;
    }

    @keyframes spin {
        to {
            transform: rotate(360deg);
        }
    }

    .stat-content {
        flex: 1;
        min-width: 0;
    }

    .stat-number {
        font-size: 2rem;
        font-weight: 700;
        color: #1e293b;
        line-height: 1.1;
        margin-bottom: 0.25rem;
    }

    .number-skeleton {
        height: 2rem;
        background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
        background-size: 200% 100%;
        animation: shimmer 1.5s infinite;
        border-radius: 4px;
        width: 80%;
    }

    @keyframes shimmer {
        0% {
            background-position: -200% 0;
        }
        100% {
            background-position: 200% 0;
        }
    }

    .stat-label {
        font-size: 0.875rem;
        color: #64748b;
        font-weight: 500;
        line-height: 1.3;
    }

    .stat-trend {
        display: flex;
        align-items: center;
        gap: 0.25rem;
        margin-top: 0.5rem;
        font-size: 0.75rem;
        font-weight: 500;
    }

    .stat-trend.trend-up {
        color: #059669;
    }

    .stat-trend.trend-down {
        color: #dc2626;
    }

    .trend-icon {
        font-size: 0.875rem;
    }

    .trend-value {
        font-weight: 600;
    }

    .trend-text {
        color: #64748b;
        font-weight: 400;
    }

    /* 반응형 */
    @media (max-width: 768px) {
        .stat-card {
            padding: 1rem;
            gap: 0.75rem;
        }

        .stat-icon {
            width: 48px;
            height: 48px;
            font-size: 1.25rem;
        }

        .stat-number {
            font-size: 1.5rem;
        }

        .stat-label {
            font-size: 0.8rem;
        }

        .icon-loading {
            width: 20px;
            height: 20px;
        }
    }

    @media (max-width: 480px) {
        .stat-card {
            flex-direction: column;
            text-align: center;
            padding: 1rem;
        }

        .stat-icon {
            width: 56px;
            height: 56px;
            font-size: 1.5rem;
        }

        .stat-trend {
            justify-content: center;
        }
    }

    /* 접근성 */
    @media (prefers-reduced-motion: reduce) {
        .stat-card {
            transition: none;
        }

        .icon-loading {
            animation: none;
        }

        .number-skeleton {
            animation: none;
        }
    }

    /* 다크모드 */
    @media (prefers-color-scheme: dark) {
        .stat-card {
            background: #1e293b;
            border-color: #334155;
        }

        .stat-number {
            color: #f1f5f9;
        }

        .stat-label {
            color: #94a3b8;
        }

        .trend-text {
            color: #94a3b8;
        }

        .number-skeleton {
            background: linear-gradient(90deg, #334155 25%, #475569 50%, #334155 75%);
            background-size: 200% 100%;
        }
    }

    /* 프린트 */
    @media print {
        .stat-card {
            box-shadow: none;
            border: 1px solid #000;
            break-inside: avoid;
        }

        .stat-trend {
            display: none;
        }
    }
</style>
<!-- src/lib/components/layout/PageHeader.svelte -->
<script>
    import { onMount, onDestroy } from 'svelte';

    export let title = '';
    export let subtitle = '';
    export let showTime = true;
    export let customTime = null; // 커스텀 시간 (옵션)

    // 현재 시간
    let currentTime = customTime || new Date();
    let timeInterval;

    // 반응형 변수들
    $: timeDisplay = currentTime.toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: true
    });

    $: dateDisplay = currentTime.toLocaleDateString('en-US', {
        month: 'short',
        day: '2-digit',
        year: 'numeric'
    });

    // 컴포넌트 마운트
    onMount(() => {
        if (showTime && !customTime) {
            // 시간 업데이트 인터벌 설정 (커스텀 시간이 없을 때만)
            timeInterval = setInterval(() => {
                currentTime = new Date();
            }, 1000);
        }
    });

    onDestroy(() => {
        if (timeInterval) {
            clearInterval(timeInterval);
        }
    });

    // 커스텀 시간이 변경될 때 업데이트
    $: if (customTime) {
        currentTime = customTime;
    }
</script>

<div class="page-header">
    <div class="header-left">
        <h1 class="page-title">{title}</h1>
        {#if subtitle}
            <p class="page-subtitle">{subtitle}</p>
        {/if}
    </div>

    {#if showTime}
        <div class="header-right">
            <span class="current-time">{timeDisplay}</span>
            <span class="current-date">{dateDisplay}</span>
        </div>
    {/if}

    <!-- 추가 액션 슬롯 -->
    {#if $$slots.actions}
        <div class="header-actions">
            <slot name="actions" />
        </div>
    {/if}
</div>

<style>
    /* 페이지 헤더 */
    .page-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 2rem;
        padding-bottom: 1rem;
        border-bottom: 1px solid #e2e8f0;
        position: relative;
    }

    .header-left {
        flex: 1;
        min-width: 0;
    }

    .page-title {
        font-size: 1.75rem;
        font-weight: 600;
        color: #1e293b;
        margin: 0;
        line-height: 1.2;
    }

    .page-subtitle {
        font-size: 1rem;
        color: #64748b;
        margin: 0.5rem 0 0 0;
        line-height: 1.4;
    }

    .header-right {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        gap: 0.25rem;
        flex-shrink: 0;
    }

    .current-time {
        font-size: 1rem;
        font-weight: 600;
        color: #1e293b;
        line-height: 1;
    }

    .current-date {
        font-size: 0.875rem;
        color: #64748b;
        line-height: 1;
    }

    .header-actions {
        display: flex;
        align-items: center;
        gap: 1rem;
        margin-left: 1rem;
        flex-shrink: 0;
    }

    /* 액션이 있을 때의 레이아웃 조정 */
    .page-header:has(.header-actions) {
        align-items: flex-start;
    }

    .page-header:has(.header-actions) .header-right {
        order: 2;
    }

    .page-header:has(.header-actions) .header-actions {
        order: 3;
    }

    /* 반응형 디자인 */
    @media (max-width: 768px) {
        .page-header {
            flex-direction: column;
            align-items: flex-start;
            gap: 1rem;
        }

        .header-right {
            align-items: flex-start;
            align-self: stretch;
        }

        .header-actions {
            margin-left: 0;
            align-self: stretch;
            justify-content: flex-start;
        }

        .page-title {
            font-size: 1.5rem;
        }

        .page-header:has(.header-actions) {
            align-items: stretch;
        }

        .page-header:has(.header-actions) .header-right,
        .page-header:has(.header-actions) .header-actions {
            order: unset;
        }
    }

    @media (max-width: 480px) {
        .page-header {
            margin-bottom: 1.5rem;
            padding-bottom: 0.75rem;
        }

        .page-title {
            font-size: 1.25rem;
        }

        .page-subtitle {
            font-size: 0.875rem;
        }

        .current-time {
            font-size: 0.9rem;
        }

        .current-date {
            font-size: 0.8rem;
        }
    }

    /* 접근성 */
    @media (prefers-reduced-motion: reduce) {
        /* 애니메이션이 있을 경우 제거 */
    }
</style>
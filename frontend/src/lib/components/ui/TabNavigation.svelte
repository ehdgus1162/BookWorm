<!-- src/lib/components/ui/TabNavigation.svelte -->
<script>
    import { createEventDispatcher } from 'svelte';

    export let tabs = []; // [{ key: 'tab1', label: 'Tab 1', disabled: false, badge: 5 }]
    export let activeTab = '';
    export let variant = 'default'; // 'default', 'pills', 'underline'
    export let size = 'medium'; // 'small', 'medium', 'large'
    export let fullWidth = false;
    export let vertical = false;
    export let scrollable = false;

    const dispatch = createEventDispatcher();

    function handleTabClick(tab) {
        if (tab.disabled) return;

        activeTab = tab.key;
        dispatch('change', {
            activeTab: tab.key,
            tab
        });
    }

    function handleKeydown(event, tab) {
        if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            handleTabClick(tab);
        } else if (event.key === 'ArrowRight' || event.key === 'ArrowLeft') {
            event.preventDefault();
            const currentIndex = tabs.findIndex(t => t.key === activeTab);
            const direction = event.key === 'ArrowRight' ? 1 : -1;
            const enabledTabs = tabs.filter(t => !t.disabled);
            const currentEnabledIndex = enabledTabs.findIndex(t => t.key === activeTab);
            const nextIndex = (currentEnabledIndex + direction + enabledTabs.length) % enabledTabs.length;
            const nextTab = enabledTabs[nextIndex];

            if (nextTab) {
                handleTabClick(nextTab);
                // Focus next tab
                const tabElements = document.querySelectorAll('.tab-item');
                const nextTabElement = Array.from(tabElements).find(el =>
                    el.getAttribute('data-tab-key') === nextTab.key
                );
                nextTabElement?.focus();
            }
        }
    }

    $: tabContainerClasses = [
        'tab-navigation',
        `tab-navigation--${variant}`,
        `tab-navigation--${size}`,
        fullWidth ? 'tab-navigation--full-width' : '',
        vertical ? 'tab-navigation--vertical' : '',
        scrollable ? 'tab-navigation--scrollable' : ''
    ].filter(Boolean).join(' ');

    // 활성 탭이 설정되지 않았으면 첫 번째 활성화된 탭을 기본으로 설정
    $: if (!activeTab && tabs.length > 0) {
        const firstActiveTab = tabs.find(tab => !tab.disabled);
        if (firstActiveTab) {
            activeTab = firstActiveTab.key;
        }
    }
</script>

<div class={tabContainerClasses} role="tablist" aria-orientation={vertical ? 'vertical' : 'horizontal'}>
    <div class="tab-list" class:scrollable>
        {#each tabs as tab (tab.key)}
            <button
                    class="tab-item"
                    class:active={activeTab === tab.key}
                    class:disabled={tab.disabled}
                    data-tab-key={tab.key}
                    role="tab"
                    tabindex={activeTab === tab.key ? 0 : -1}
                    aria-selected={activeTab === tab.key}
                    aria-disabled={tab.disabled}
                    disabled={tab.disabled}
                    on:click={() => handleTabClick(tab)}
                    on:keydown={(e) => handleKeydown(e, tab)}
            >
                {#if tab.icon}
                    <span class="tab-icon" aria-hidden="true">{tab.icon}</span>
                {/if}

                <span class="tab-label">{tab.label}</span>

                {#if tab.badge !== undefined && tab.badge !== null}
                    <span class="tab-badge" aria-label="{tab.badge} items">
                        {tab.badge}
                    </span>
                {/if}
            </button>
        {/each}
    </div>

    {#if variant === 'underline'}
        <div class="tab-indicator" style="display: none;"></div>
    {/if}
</div>

<!-- Tab Content Slot -->
<div class="tab-content" role="tabpanel" aria-labelledby="active-tab">
    <slot />
</div>

<style>
    .tab-navigation {
        display: flex;
        flex-direction: column;
        width: 100%;
    }

    .tab-navigation--vertical {
        flex-direction: row;
    }

    .tab-list {
        display: flex;
        background: white;
        border-radius: 8px;
        overflow: hidden;
        position: relative;
    }

    .tab-navigation--vertical .tab-list {
        flex-direction: column;
        width: 200px;
        border-radius: 8px;
        border: 1px solid #e2e8f0;
    }

    .tab-navigation--scrollable .tab-list {
        overflow-x: auto;
        scroll-behavior: smooth;
    }

    .tab-navigation--scrollable .tab-list::-webkit-scrollbar {
        height: 2px;
    }

    .tab-navigation--scrollable .tab-list::-webkit-scrollbar-track {
        background: #f1f5f9;
    }

    .tab-navigation--scrollable .tab-list::-webkit-scrollbar-thumb {
        background: #cbd5e1;
        border-radius: 1px;
    }

    .tab-item {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        padding: 0.75rem 1.5rem;
        background: transparent;
        border: none;
        color: #64748b;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s ease;
        white-space: nowrap;
        position: relative;
        font-family: inherit;
        font-size: 0.875rem;
        outline: none;
        flex-shrink: 0;
    }

    .tab-navigation--full-width .tab-item {
        flex: 1;
        justify-content: center;
    }

    /* Size variants */
    .tab-navigation--small .tab-item {
        padding: 0.5rem 1rem;
        font-size: 0.8rem;
    }

    .tab-navigation--medium .tab-item {
        padding: 0.75rem 1.5rem;
        font-size: 0.875rem;
    }

    .tab-navigation--large .tab-item {
        padding: 1rem 2rem;
        font-size: 1rem;
    }

    /* Default variant */
    .tab-navigation--default .tab-list {
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        border: 1px solid #f1f5f9;
    }

    .tab-navigation--default .tab-item.active {
        background: #1e293b;
        color: white;
    }

    .tab-navigation--default .tab-item:hover:not(.active):not(.disabled) {
        background: #f8fafc;
        color: #374151;
    }

    /* Pills variant */
    .tab-navigation--pills .tab-list {
        background: #f1f5f9;
        padding: 0.25rem;
        gap: 0.25rem;
    }

    .tab-navigation--pills .tab-item {
        border-radius: 6px;
        background: transparent;
    }

    .tab-navigation--pills .tab-item.active {
        background: white;
        color: #1e293b;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    }

    .tab-navigation--pills .tab-item:hover:not(.active):not(.disabled) {
        background: rgba(255, 255, 255, 0.5);
        color: #374151;
    }

    /* Underline variant */
    .tab-navigation--underline .tab-list {
        border-bottom: 1px solid #e2e8f0;
        background: transparent;
        border-radius: 0;
    }

    .tab-navigation--underline .tab-item {
        border-bottom: 2px solid transparent;
        border-radius: 0;
        background: transparent;
    }

    .tab-navigation--underline .tab-item.active {
        color: #3b82f6;
        border-bottom-color: #3b82f6;
        background: transparent;
    }

    .tab-navigation--underline .tab-item:hover:not(.active):not(.disabled) {
        color: #374151;
        border-bottom-color: #cbd5e1;
    }

    /* Tab elements */
    .tab-icon {
        font-size: 1.125em;
        line-height: 1;
    }

    .tab-label {
        line-height: 1.4;
    }

    .tab-badge {
        background: #ef4444;
        color: white;
        font-size: 0.75rem;
        font-weight: 600;
        padding: 0.125rem 0.5rem;
        border-radius: 10px;
        min-width: 20px;
        text-align: center;
        line-height: 1.2;
    }

    .tab-navigation--default .tab-item.active .tab-badge {
        background: rgba(255, 255, 255, 0.2);
        color: white;
    }

    .tab-navigation--pills .tab-item.active .tab-badge {
        background: #3b82f6;
        color: white;
    }

    .tab-navigation--underline .tab-item.active .tab-badge {
        background: #3b82f6;
        color: white;
    }

    /* Disabled state */
    .tab-item.disabled {
        opacity: 0.5;
        cursor: not-allowed;
        pointer-events: none;
    }

    /* Tab content */
    .tab-content {
        flex: 1;
        padding: 1.5rem 0 0 0;
    }

    .tab-navigation--vertical .tab-content {
        padding: 0 0 0 1.5rem;
        flex: 1;
    }

    /* Focus styles */
    .tab-item:focus-visible {
        outline: 2px solid #3b82f6;
        outline-offset: 2px;
        z-index: 1;
    }

    .tab-navigation--underline .tab-item:focus-visible {
        outline-offset: -2px;
    }

    /* Active indicator animation for underline */
    .tab-navigation--underline .tab-item.active {
        position: relative;
    }

    .tab-navigation--underline .tab-item.active::after {
        content: '';
        position: absolute;
        bottom: -1px;
        left: 0;
        right: 0;
        height: 2px;
        background: #3b82f6;
        transform-origin: left;
        animation: slideIn 0.2s ease-out;
    }

    @keyframes slideIn {
        from {
            transform: scaleX(0);
        }
        to {
            transform: scaleX(1);
        }
    }

    /* 반응형 */
    @media (max-width: 768px) {
        .tab-navigation--vertical {
            flex-direction: column;
        }

        .tab-navigation--vertical .tab-list {
            width: 100%;
            flex-direction: row;
            overflow-x: auto;
        }

        .tab-navigation--vertical .tab-content {
            padding: 1.5rem 0 0 0;
        }

        .tab-item {
            padding: 0.625rem 1rem;
            font-size: 0.8rem;
        }

        .tab-navigation--large .tab-item {
            padding: 0.75rem 1.25rem;
            font-size: 0.9rem;
        }

        .tab-list.scrollable {
            -webkit-overflow-scrolling: touch;
        }
    }

    @media (max-width: 480px) {
        .tab-item {
            padding: 0.5rem 0.75rem;
            font-size: 0.75rem;
        }

        .tab-icon {
            font-size: 1em;
        }

        .tab-badge {
            font-size: 0.7rem;
            padding: 0.1rem 0.4rem;
            min-width: 18px;
        }

        .tab-content {
            padding-top: 1rem;
        }
    }

    /* 접근성 */
    @media (prefers-reduced-motion: reduce) {
        .tab-item {
            transition: none;
        }

        .tab-item.active::after {
            animation: none;
        }
    }

    /* 다크모드 지원 */
    @media (prefers-color-scheme: dark) {
        .tab-list {
            background: #1e293b;
            border-color: #334155;
        }

        .tab-navigation--default .tab-item.active {
            background: #0f172a;
            color: #f1f5f9;
        }

        .tab-navigation--default .tab-item:hover:not(.active):not(.disabled) {
            background: #334155;
            color: #f1f5f9;
        }

        .tab-navigation--pills .tab-list {
            background: #334155;
        }

        .tab-navigation--pills .tab-item.active {
            background: #0f172a;
            color: #f1f5f9;
        }

        .tab-navigation--pills .tab-item:hover:not(.active):not(.disabled) {
            background: rgba(15, 23, 42, 0.5);
            color: #f1f5f9;
        }

        .tab-navigation--underline .tab-list {
            border-bottom-color: #334155;
        }

        .tab-navigation--underline .tab-item:hover:not(.active):not(.disabled) {
            color: #f1f5f9;
            border-bottom-color: #475569;
        }

        .tab-item {
            color: #94a3b8;
        }
    }

    /* 프린트 스타일 */
    @media print {
        .tab-navigation {
            display: none;
        }

        .tab-content {
            padding: 0;
        }
    }

    /* 터치 디바이스 최적화 */
    @media (hover: none) {
        .tab-item:hover {
            background: transparent;
            color: inherit;
            border-bottom-color: transparent;
        }

        .tab-navigation--default .tab-item:hover:not(.active) {
            background: transparent;
            color: #64748b;
        }

        .tab-navigation--pills .tab-item:hover:not(.active) {
            background: transparent;
        }

        .tab-navigation--underline .tab-item:hover:not(.active) {
            color: #64748b;
            border-bottom-color: transparent;
        }
    }

    /* 고해상도 화면 */
    @media (-webkit-min-device-pixel-ratio: 2), (min-resolution: 192dpi) {
        .tab-navigation--underline .tab-item.active::after {
            height: 1px;
            transform: scaleY(2);
        }
    }
</style>
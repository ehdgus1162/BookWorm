<!-- src/lib/components/ui/ActionIcon.svelte -->
<script>
    import { createEventDispatcher } from 'svelte';

    export let icon = '';
    export let variant = 'default'; // 'default', 'primary', 'danger', 'success', 'warning'
    export let size = 'medium'; // 'small', 'medium', 'large'
    export let disabled = false;
    export let tooltip = '';
    export let ariaLabel = '';

    const dispatch = createEventDispatcher();

    function handleClick(event) {
        if (!disabled) {
            dispatch('click', event);
        }
    }

    $: actionIconClasses = [
        'action-icon',
        `action-icon--${variant}`,
        `action-icon--${size}`,
        disabled ? 'action-icon--disabled' : ''
    ].filter(Boolean).join(' ');
</script>

<button
        class={actionIconClasses}
        type="button"
        {disabled}
        title={tooltip}
        aria-label={ariaLabel || tooltip}
        on:click={handleClick}
>
    <span class="action-icon-content" aria-hidden="true">
        {icon}
    </span>
</button>

<style>
    .action-icon {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        border: none;
        border-radius: 6px;
        cursor: pointer;
        transition: all 0.2s ease;
        position: relative;
        font-family: inherit;
        text-decoration: none;
        outline: none;
        flex-shrink: 0;
    }

    .action-icon-content {
        display: flex;
        align-items: center;
        justify-content: center;
        line-height: 1;
    }

    /* Size variants */
    .action-icon--small {
        width: 28px;
        height: 28px;
        font-size: 0.75rem;
    }

    .action-icon--medium {
        width: 32px;
        height: 32px;
        font-size: 0.875rem;
    }

    .action-icon--large {
        width: 40px;
        height: 40px;
        font-size: 1rem;
    }

    /* Variant styles */
    .action-icon--default {
        background: #f8fafc;
        color: #64748b;
        border: 1px solid #e2e8f0;
    }

    .action-icon--default:hover:not(.action-icon--disabled) {
        background: #f1f5f9;
        color: #334155;
        border-color: #cbd5e1;
        transform: translateY(-1px);
    }

    .action-icon--primary {
        background: #eff6ff;
        color: #1d4ed8;
        border: 1px solid #dbeafe;
    }

    .action-icon--primary:hover:not(.action-icon--disabled) {
        background: #dbeafe;
        color: #1e40af;
        border-color: #93c5fd;
        transform: translateY(-1px);
    }

    .action-icon--danger {
        background: #fef2f2;
        color: #dc2626;
        border: 1px solid #fecaca;
    }

    .action-icon--danger:hover:not(.action-icon--disabled) {
        background: #fecaca;
        color: #b91c1c;
        border-color: #f87171;
        transform: translateY(-1px);
    }

    .action-icon--success {
        background: #f0fdf4;
        color: #16a34a;
        border: 1px solid #bbf7d0;
    }

    .action-icon--success:hover:not(.action-icon--disabled) {
        background: #dcfce7;
        color: #15803d;
        border-color: #86efac;
        transform: translateY(-1px);
    }

    .action-icon--warning {
        background: #fffbeb;
        color: #d97706;
        border: 1px solid #fed7aa;
    }

    .action-icon--warning:hover:not(.action-icon--disabled) {
        background: #fef3c7;
        color: #b45309;
        border-color: #fdba74;
        transform: translateY(-1px);
    }

    /* Disabled state */
    .action-icon--disabled {
        opacity: 0.5;
        cursor: not-allowed;
        transform: none !important;
    }

    /* Focus styles */
    .action-icon:focus-visible {
        outline: 2px solid #3b82f6;
        outline-offset: 2px;
    }

    /* Active state */
    .action-icon:active:not(.action-icon--disabled) {
        transform: translateY(0);
    }

    /* 반응형 */
    @media (max-width: 768px) {
        .action-icon--large {
            width: 36px;
            height: 36px;
            font-size: 0.9rem;
        }

        .action-icon--medium {
            width: 30px;
            height: 30px;
            font-size: 0.8rem;
        }

        .action-icon--small {
            width: 26px;
            height: 26px;
            font-size: 0.7rem;
        }
    }

    /* 접근성 */
    @media (prefers-reduced-motion: reduce) {
        .action-icon {
            transition: none;
        }
    }

    /* 다크모드 지원 */
    @media (prefers-color-scheme: dark) {
        .action-icon--default {
            background: #374151;
            color: #9ca3af;
            border-color: #4b5563;
        }

        .action-icon--default:hover:not(.action-icon--disabled) {
            background: #4b5563;
            color: #f9fafb;
            border-color: #6b7280;
        }

        .action-icon--primary {
            background: #1e3a8a;
            color: #93c5fd;
            border-color: #1d4ed8;
        }

        .action-icon--primary:hover:not(.action-icon--disabled) {
            background: #1e40af;
            color: #dbeafe;
            border-color: #3b82f6;
        }

        .action-icon--danger {
            background: #7f1d1d;
            color: #fca5a5;
            border-color: #dc2626;
        }

        .action-icon--danger:hover:not(.action-icon--disabled) {
            background: #991b1b;
            color: #fecaca;
            border-color: #ef4444;
        }

        .action-icon--success {
            background: #14532d;
            color: #86efac;
            border-color: #16a34a;
        }

        .action-icon--success:hover:not(.action-icon--disabled) {
            background: #166534;
            color: #bbf7d0;
            border-color: #22c55e;
        }

        .action-icon--warning {
            background: #92400e;
            color: #fcd34d;
            border-color: #d97706;
        }

        .action-icon--warning:hover:not(.action-icon--disabled) {
            background: #a16207;
            color: #fed7aa;
            border-color: #f59e0b;
        }
    }

    /* 툴팁 효과 (선택사항) */
    .action-icon[title]:hover::after {
        content: attr(title);
        position: absolute;
        bottom: 100%;
        left: 50%;
        transform: translateX(-50%);
        background: #1f2937;
        color: white;
        padding: 0.5rem;
        border-radius: 4px;
        font-size: 0.75rem;
        white-space: nowrap;
        z-index: 1000;
        margin-bottom: 4px;
        opacity: 0;
        animation: tooltipFadeIn 0.2s ease-in-out forwards;
        pointer-events: none;
    }

    .action-icon[title]:hover::before {
        content: '';
        position: absolute;
        bottom: 100%;
        left: 50%;
        transform: translateX(-50%);
        border: 4px solid transparent;
        border-top-color: #1f2937;
        z-index: 1000;
        margin-bottom: -4px;
        opacity: 0;
        animation: tooltipFadeIn 0.2s ease-in-out forwards;
        pointer-events: none;
    }

    @keyframes tooltipFadeIn {
        from {
            opacity: 0;
            transform: translateX(-50%) translateY(4px);
        }
        to {
            opacity: 1;
            transform: translateX(-50%) translateY(0);
        }
    }

    /* 호버 효과 비활성화 (터치 디바이스) */
    @media (hover: none) {
        .action-icon:hover {
            transform: none;
        }

        .action-icon[title]:hover::after,
        .action-icon[title]:hover::before {
            display: none;
        }
    }
</style>
<!-- src/lib/components/ui/Button.svelte -->
<script>
    import { createEventDispatcher } from 'svelte';

    export let variant = 'primary'; // 'primary', 'secondary', 'danger', 'success', 'warning', 'ghost'
    export let size = 'medium'; // 'small', 'medium', 'large'
    export let disabled = false;
    export let loading = false;
    export let fullWidth = false;
    export let icon = '';
    export let iconPosition = 'left'; // 'left', 'right'
    export let type = 'button'; // 'button', 'submit', 'reset'

    const dispatch = createEventDispatcher();

    function handleClick(event) {
        if (!disabled && !loading) {
            dispatch('click', event);
        }
    }

    $: buttonClasses = [
        'btn',
        `btn--${variant}`,
        `btn--${size}`,
        fullWidth ? 'btn--full-width' : '',
        loading ? 'btn--loading' : '',
        disabled ? 'btn--disabled' : ''
    ].filter(Boolean).join(' ');
</script>

<button
        class={buttonClasses}
        {type}
        {disabled}
        on:click={handleClick}
        aria-disabled={disabled || loading}
>
    {#if loading}
        <span class="btn-spinner" aria-hidden="true"></span>
    {:else if icon && iconPosition === 'left'}
        <span class="btn-icon btn-icon--left" aria-hidden="true">{icon}</span>
    {/if}

    <span class="btn-text">
        <slot />
    </span>

    {#if !loading && icon && iconPosition === 'right'}
        <span class="btn-icon btn-icon--right" aria-hidden="true">{icon}</span>
    {/if}
</button>

<style>
    .btn {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 0.5rem;
        border: none;
        border-radius: 8px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s ease;
        text-decoration: none;
        outline: none;
        position: relative;
        font-family: inherit;
        white-space: nowrap;
        text-transform: uppercase;
        letter-spacing: 0.025em;
    }

    /* Size variants */
    .btn--small {
        padding: 0.5rem 1rem;
        font-size: 0.75rem;
        min-height: 32px;
    }

    .btn--medium {
        padding: 0.75rem 1.5rem;
        font-size: 0.875rem;
        min-height: 40px;
    }

    .btn--large {
        padding: 1rem 2rem;
        font-size: 1rem;
        min-height: 48px;
    }

    /* Variant styles */
    .btn--primary {
        background: #3b82f6;
        color: white;
        border: 1px solid #3b82f6;
    }

    .btn--primary:hover:not(.btn--disabled):not(.btn--loading) {
        background: #2563eb;
        border-color: #2563eb;
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
    }

    .btn--secondary {
        background: #f3f4f6;
        color: #374151;
        border: 1px solid #d1d5db;
    }

    .btn--secondary:hover:not(.btn--disabled):not(.btn--loading) {
        background: #e5e7eb;
        border-color: #9ca3af;
    }

    .btn--danger {
        background: #ef4444;
        color: white;
        border: 1px solid #ef4444;
    }

    .btn--danger:hover:not(.btn--disabled):not(.btn--loading) {
        background: #dc2626;
        border-color: #dc2626;
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
    }

    .btn--success {
        background: #10b981;
        color: white;
        border: 1px solid #10b981;
    }

    .btn--success:hover:not(.btn--disabled):not(.btn--loading) {
        background: #059669;
        border-color: #059669;
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
    }

    .btn--warning {
        background: #f59e0b;
        color: white;
        border: 1px solid #f59e0b;
    }

    .btn--warning:hover:not(.btn--disabled):not(.btn--loading) {
        background: #d97706;
        border-color: #d97706;
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
    }

    .btn--ghost {
        background: transparent;
        color: #64748b;
        border: 1px solid transparent;
    }

    .btn--ghost:hover:not(.btn--disabled):not(.btn--loading) {
        background: #f1f5f9;
        color: #334155;
    }

    /* Full width */
    .btn--full-width {
        width: 100%;
    }

    /* Disabled state */
    .btn--disabled {
        opacity: 0.6;
        cursor: not-allowed;
        transform: none !important;
        box-shadow: none !important;
    }

    /* Loading state */
    .btn--loading {
        cursor: wait;
        opacity: 0.8;
    }

    .btn--loading .btn-text {
        opacity: 0.7;
    }

    /* Icons */
    .btn-icon {
        font-size: 1em;
        line-height: 1;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .btn-icon--left {
        margin-right: 0.25rem;
    }

    .btn-icon--right {
        margin-left: 0.25rem;
    }

    /* Spinner */
    .btn-spinner {
        width: 16px;
        height: 16px;
        border: 2px solid transparent;
        border-top: 2px solid currentColor;
        border-radius: 50%;
        animation: spin 1s linear infinite;
    }

    @keyframes spin {
        to {
            transform: rotate(360deg);
        }
    }

    /* Text */
    .btn-text {
        display: flex;
        align-items: center;
        line-height: 1;
    }

    /* Focus styles */
    .btn:focus-visible {
        outline: 2px solid #3b82f6;
        outline-offset: 2px;
    }

    /* 반응형 */
    @media (max-width: 768px) {
        .btn--large {
            padding: 0.875rem 1.75rem;
            font-size: 0.9rem;
        }

        .btn--medium {
            padding: 0.625rem 1.25rem;
            font-size: 0.8rem;
        }

        .btn--small {
            padding: 0.375rem 0.875rem;
            font-size: 0.7rem;
        }
    }

    /* 접근성 */
    @media (prefers-reduced-motion: reduce) {
        .btn {
            transition: none;
        }

        .btn-spinner {
            animation: none;
        }
    }

    /* 다크모드 지원 */
    @media (prefers-color-scheme: dark) {
        .btn--secondary {
            background: #374151;
            color: #f9fafb;
            border-color: #4b5563;
        }

        .btn--secondary:hover:not(.btn--disabled):not(.btn--loading) {
            background: #4b5563;
            border-color: #6b7280;
        }

        .btn--ghost {
            color: #9ca3af;
        }

        .btn--ghost:hover:not(.btn--disabled):not(.btn--loading) {
            background: #374151;
            color: #f9fafb;
        }
    }

    /* 활성 상태 */
    .btn:active:not(.btn--disabled):not(.btn--loading) {
        transform: translateY(0);
    }

    /* 호버 효과 비활성화 (터치 디바이스) */
    @media (hover: none) {
        .btn:hover {
            transform: none;
            box-shadow: none;
        }
    }
</style>
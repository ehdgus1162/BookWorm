<!-- src/lib/components/ui/Modal.svelte -->
<script>
    import { createEventDispatcher } from 'svelte';
    import { fade, scale } from 'svelte/transition';

    export let isOpen = false;
    export let title = '';
    export let icon = '';
    export let size = 'medium'; // 'small', 'medium', 'large'
    export let closable = true;
    export let backdrop = true;

    const dispatch = createEventDispatcher();

    function closeModal() {
        if (closable) {
            isOpen = false;
            dispatch('close');
        }
    }

    function handleKeydown(event) {
        if (event.key === 'Escape' && closable) {
            closeModal();
        }
    }

    function handleBackdropClick() {
        if (backdrop) {
            closeModal();
        }
    }

    $: modalSizeClass = `modal--${size}`;
</script>

<svelte:window on:keydown={handleKeydown} />

{#if isOpen}
    <div
            class="modal-overlay"
            on:click={handleBackdropClick}
            transition:fade={{ duration: 200 }}
            role="dialog"
            aria-modal="true"
            aria-labelledby="modal-title"
    >
        <div
                class="modal {modalSizeClass}"
                on:click|stopPropagation
                transition:scale={{ duration: 300, start: 0.9 }}
        >
            <!-- Header -->
            <div class="modal-header">
                <div class="modal-title-section">
                    {#if icon}
                        <div class="modal-icon">
                            <span class="icon" aria-hidden="true">{icon}</span>
                        </div>
                    {/if}
                    <div class="modal-title-text">
                        <h2 id="modal-title" class="modal-title">{title}</h2>
                        <slot name="subtitle" />
                    </div>
                </div>

                {#if closable}
                    <button
                            class="close-btn"
                            on:click={closeModal}
                            aria-label="Close modal"
                            type="button"
                    >
                        ✕
                    </button>
                {/if}
            </div>

            <!-- Body -->
            <div class="modal-body">
                <slot />
            </div>

            <!-- Footer -->
            {#if $$slots.footer}
                <div class="modal-footer">
                    <slot name="footer" />
                </div>
            {/if}
        </div>
    </div>
{/if}

<style>
    .modal-overlay {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 1000;
        padding: 1rem;
    }

    .modal {
        background: white;
        border-radius: 12px;
        box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
        border: 1px solid #f1f5f9;
        width: 100%;
        max-height: 90vh;
        overflow: hidden;
        display: flex;
        flex-direction: column;
    }

    /* Size variants */
    .modal--small { max-width: 400px; }
    .modal--medium { max-width: 500px; }
    .modal--large { max-width: 700px; }

    .modal-header {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        padding: 1.5rem;
        border-bottom: 1px solid #f1f5f9;
        background: #f8fafc;
    }

    .modal-title-section {
        display: flex;
        align-items: flex-start;
        gap: 1rem;
        flex: 1;
        min-width: 0;
    }

    .modal-icon {
        width: 48px;
        height: 48px;
        border-radius: 8px;
        background: #f1f5f9;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.5rem;
        flex-shrink: 0;
        color: #64748b;
    }

    .modal-title-text {
        flex: 1;
        min-width: 0;
    }

    .modal-title {
        font-size: 1.25rem;
        font-weight: 600;
        color: #1f2937;
        margin: 0;
        line-height: 1.4;
    }

    .close-btn {
        width: 32px;
        height: 32px;
        border: none;
        background: #f3f4f6;
        border-radius: 6px;
        cursor: pointer;
        color: #6b7280;
        font-size: 1.25rem;
        transition: all 0.2s ease;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
    }

    .close-btn:hover {
        background: #e5e7eb;
        color: #374151;
    }

    .modal-body {
        padding: 1.5rem;
        overflow-y: auto;
        flex: 1;
    }

    .modal-footer {
        display: flex;
        justify-content: flex-end;
        gap: 1rem;
        padding: 1.5rem;
        border-top: 1px solid #f1f5f9;
        background: #f8fafc;
    }

    /* 반응형 */
    @media (max-width: 768px) {
        .modal-overlay {
            padding: 0.5rem;
        }

        .modal {
            max-width: none;
            width: 100%;
            margin: 0;
        }

        .modal-header,
        .modal-body,
        .modal-footer {
            padding: 1rem;
        }

        .modal-title-section {
            flex-direction: column;
            gap: 0.75rem;
        }

        .modal-icon {
            width: 40px;
            height: 40px;
            font-size: 1.25rem;
        }
    }

    /* 접근성 */
    @media (prefers-reduced-motion: reduce) {
        .modal {
            transition: none;
        }
    }

    /* 포커스 스타일 */
    .close-btn:focus {
        outline: 2px solid #3b82f6;
        outline-offset: 2px;
    }

    /* 다크모드 지원 */
    @media (prefers-color-scheme: dark) {
        .modal {
            background: #1e293b;
            border-color: #334155;
        }

        .modal-header,
        .modal-footer {
            background: #334155;
            border-color: #475569;
        }

        .modal-title {
            color: #f1f5f9;
        }

        .modal-icon {
            background: #475569;
            color: #94a3b8;
        }

        .close-btn {
            background: #475569;
            color: #94a3b8;
        }

        .close-btn:hover {
            background: #64748b;
            color: #f1f5f9;
        }
    }
</style>
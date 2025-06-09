<!-- src/lib/components/ui/SearchBox.svelte -->
<script>
    import { createEventDispatcher, onDestroy } from 'svelte';

    export let value = '';
    export let placeholder = 'Search...';
    export let size = 'medium';
    export let disabled = false;
    export let clearable = true;
    export let autofocus = false;
    export let debounce = 300;
    export let icon = '🔍';

    const dispatch = createEventDispatcher();

    let inputElement;
    let debounceTimer;

    // 디바운스된 검색 함수
    function debouncedSearch(searchValue) {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            dispatch('search', searchValue);
        }, debounce);
    }

    // 입력 핸들러를 하나로 통합
    function handleInput(event) {
        value = event.target.value;
        dispatch('input', value);
        debouncedSearch(value);
    }

    function clearSearch() {
        value = '';
        clearTimeout(debounceTimer); // 타이머 정리
        dispatch('clear');
        dispatch('search', '');
        if (inputElement) {
            inputElement.focus();
        }
    }

    function handleKeydown(event) {
        if (event.key === 'Enter') {
            clearTimeout(debounceTimer);
            dispatch('search', value);
            dispatch('enter', value);
        } else if (event.key === 'Escape') {
            clearSearch();
            dispatch('escape');
        }
    }

    // 컴포넌트 정리 시 타이머 정리
    onDestroy(() => {
        if (debounceTimer) {
            clearTimeout(debounceTimer);
        }
    });

    // 클래스 계산을 더 명확하게
    $: searchBoxClasses = [
        'search-box',
        `search-box--${size}`,
        disabled && 'search-box--disabled'
    ].filter(Boolean).join(' ');

    // 값이 있는지 확인하는 반응형 변수
    $: hasValue = value && value.trim().length > 0;
</script>

<div class={searchBoxClasses}>
    <div class="search-input-container">
        <span class="search-icon" aria-hidden="true">{icon}</span>

        <input
                bind:this={inputElement}
                bind:value
                type="text"
                {placeholder}
                {disabled}
                {autofocus}
                class="search-input"
                on:input={handleInput}
                on:keydown={handleKeydown}
                on:focus
                on:blur
                aria-label={placeholder}
        />

        {#if clearable && hasValue && !disabled}
            <button
                    class="clear-btn"
                    type="button"
                    on:click={clearSearch}
                    aria-label="Clear search"
                    tabindex="0"
            >
                ✕
            </button>
        {/if}
    </div>
</div>

<style>
    .search-box {
        position: relative;
        width: 100%;
        max-width: 100%;
    }

    .search-input-container {
        position: relative;
        display: flex;
        align-items: center;
        background: white;
        border: 1px solid #d1d5db;
        border-radius: 8px;
        transition: all 0.2s ease;
        overflow: hidden;
    }

    /* :has() 대신 JavaScript로 처리하거나 더 호환성 좋은 방법 사용 */
    .search-input-container:focus-within {
        border-color: #3b82f6;
        box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
    }

    .search-icon {
        position: absolute;
        left: 0.75rem;
        color: #6b7280;
        font-size: 1rem;
        z-index: 1;
        pointer-events: none;
        /* 아이콘 렌더링 개선 */
        line-height: 1;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .search-input {
        width: 100%;
        border: none;
        outline: none;
        background: transparent;
        color: #1f2937;
        font-size: 0.875rem;
        font-family: inherit;
        padding: 0.75rem 2.5rem;
        /* 기본 높이 설정 */
        min-height: 40px;
        box-sizing: border-box;
    }

    .search-input::placeholder {
        color: #9ca3af;
    }

    .clear-btn {
        position: absolute;
        right: 0.75rem;
        background: none;
        border: none;
        color: #6b7280;
        cursor: pointer;
        font-size: 1rem;
        line-height: 1;
        padding: 0.25rem;
        border-radius: 4px;
        transition: all 0.2s ease;
        z-index: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        width: 20px;
        height: 20px;
        /* 터치 영역 확대 */
        min-width: 44px;
        min-height: 44px;
    }

    .clear-btn:hover:not(:disabled) {
        background: #f3f4f6;
        color: #374151;
    }

    .clear-btn:focus {
        outline: 2px solid #3b82f6;
        outline-offset: 1px;
    }

    /* Size variants - 더 일관된 스타일링 */
    .search-box--small .search-input {
        padding: 0.5rem 2.25rem;
        min-height: 36px;
        font-size: 0.8rem;
    }

    .search-box--small .search-icon {
        left: 0.625rem;
        font-size: 0.9rem;
    }

    .search-box--small .clear-btn {
        right: 0.625rem;
        font-size: 0.9rem;
        width: 18px;
        height: 18px;
    }

    .search-box--medium .search-input {
        padding: 0.75rem 2.5rem;
        min-height: 40px;
        font-size: 0.875rem;
    }

    .search-box--large .search-input {
        padding: 1rem 3rem;
        min-height: 48px;
        font-size: 1rem;
    }

    .search-box--large .search-icon {
        left: 1rem;
        font-size: 1.1rem;
    }

    .search-box--large .clear-btn {
        right: 1rem;
        font-size: 1.1rem;
        width: 24px;
        height: 24px;
    }

    /* Disabled state 개선 */
    .search-box--disabled .search-input-container {
        background: #f9fafb;
        border-color: #e5e7eb;
        cursor: not-allowed;
        opacity: 0.6;
    }

    .search-box--disabled .search-input {
        color: #9ca3af;
        cursor: not-allowed;
    }

    .search-box--disabled .search-icon,
    .search-box--disabled .clear-btn {
        color: #d1d5db;
        cursor: not-allowed;
        pointer-events: none;
    }

    /* 접근성 개선 */
    @media (prefers-reduced-motion: reduce) {
        .search-input-container,
        .clear-btn {
            transition: none;
        }
    }

    /* 고대비 모드 지원 */
    @media (prefers-contrast: high) {
        .search-input-container {
            border-width: 2px;
        }

        .search-input-container:focus-within {
            border-color: #000;
            box-shadow: 0 0 0 2px #000;
        }
    }

    /* 다크모드 지원 개선 */
    @media (prefers-color-scheme: dark) {
        .search-input-container {
            background: #374151;
            border-color: #4b5563;
        }

        .search-input-container:focus-within {
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
        }

        .search-input {
            color: #f9fafb;
        }

        .search-input::placeholder {
            color: #6b7280;
        }

        .search-icon,
        .clear-btn {
            color: #9ca3af;
        }

        .clear-btn:hover:not(:disabled) {
            background: #4b5563;
            color: #f9fafb;
        }

        .search-box--disabled .search-input-container {
            background: #1f2937;
            border-color: #374151;
        }

        .search-box--disabled .search-input {
            color: #6b7280;
        }
    }
</style>
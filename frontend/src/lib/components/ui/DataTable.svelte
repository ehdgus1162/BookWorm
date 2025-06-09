<!-- src/lib/components/ui/DataTable.svelte -->
<script>
    import { createEventDispatcher } from 'svelte';
    import { fade } from 'svelte/transition';
    import ActionIcon from './ActionIcon.svelte';

    export let data = [];
    export let columns = [];
    export let actions = [];
    export let loading = false;
    export let searchable = false;
    export let sortable = true;
    export let selectable = false;
    export let emptyMessage = 'No data available';
    export let loadingMessage = 'Loading data...';
    export let showHeader = true;
    export let striped = false;
    export let hoverable = true;
    export let compact = false;

    const dispatch = createEventDispatcher();

    let sortColumn = '';
    let sortDirection = 'asc';
    let selectedItems = new Set();

    // 정렬된 데이터
    $: sortedData = sortData(data);

    function sortData(data) {
        if (!sortColumn || !sortable) return data;

        return [...data].sort((a, b) => {
            const aVal = getNestedValue(a, sortColumn);
            const bVal = getNestedValue(b, sortColumn);

            // 숫자 비교
            if (typeof aVal === 'number' && typeof bVal === 'number') {
                return sortDirection === 'asc' ? aVal - bVal : bVal - aVal;
            }

            // 문자열 비교
            const aStr = String(aVal).toLowerCase();
            const bStr = String(bVal).toLowerCase();

            if (aStr < bStr) return sortDirection === 'asc' ? -1 : 1;
            if (aStr > bStr) return sortDirection === 'asc' ? 1 : -1;
            return 0;
        });
    }

    function getNestedValue(obj, path) {
        return path.split('.').reduce((o, p) => o && o[p], obj);
    }

    function handleSort(columnKey) {
        if (!sortable) return;

        const column = columns.find(col => col.key === columnKey);
        if (column && column.sortable === false) return;

        if (sortColumn === columnKey) {
            sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
        } else {
            sortColumn = columnKey;
            sortDirection = 'asc';
        }

        dispatch('sort', { column: columnKey, direction: sortDirection });
    }

    function handleSelectAll(event) {
        if (event.target.checked) {
            selectedItems = new Set(sortedData.map(item => item.id));
        } else {
            selectedItems = new Set();
        }

        dispatch('selectionChange', {
            selectedItems: Array.from(selectedItems),
            allSelected: event.target.checked
        });
    }

    function handleSelectItem(item) {
        if (selectedItems.has(item.id)) {
            selectedItems.delete(item.id);
        } else {
            selectedItems.add(item.id);
        }
        selectedItems = selectedItems;

        dispatch('selectionChange', {
            selectedItems: Array.from(selectedItems),
            item
        });
    }

    function handleAction(action, item) {
        dispatch('action', { action: action.key, item });
        if (action.handler) {
            action.handler(item);
        }
    }

    function renderCellContent(item, column) {
        const value = getNestedValue(item, column.key);

        if (column.render) {
            return column.render(value, item);
        }

        if (column.format) {
            return column.format(value);
        }

        return value;
    }

    $: tableClasses = [
        'data-table',
        striped ? 'data-table--striped' : '',
        hoverable ? 'data-table--hoverable' : '',
        compact ? 'data-table--compact' : '',
        loading ? 'data-table--loading' : ''
    ].filter(Boolean).join(' ');

    $: allSelected = sortedData.length > 0 && selectedItems.size === sortedData.length;
    $: someSelected = selectedItems.size > 0 && selectedItems.size < sortedData.length;
</script>

<div class="table-container">
    {#if loading}
        <div class="table-loading">
            <div class="loading-spinner"></div>
            <p class="loading-text">{loadingMessage}</p>
        </div>
    {:else if sortedData.length === 0}
        <div class="table-empty">
            <div class="empty-icon">📄</div>
            <p class="empty-message">{emptyMessage}</p>
            <slot name="empty" />
        </div>
    {:else}
        <div class="table-wrapper">
            <table class={tableClasses}>
                {#if showHeader}
                    <thead class="table-head">
                    <tr>
                        {#if selectable}
                            <th class="table-cell table-cell--checkbox">
                                <input
                                        type="checkbox"
                                        class="checkbox"
                                        checked={allSelected}
                                        indeterminate={someSelected}
                                        on:change={handleSelectAll}
                                        aria-label="Select all rows"
                                />
                            </th>
                        {/if}

                        {#each columns as column}
                            <th
                                    class="table-cell table-cell--header"
                                    class:sortable={sortable && column.sortable !== false}
                                    class:sorted={sortColumn === column.key}
                                    class:text-center={column.align === 'center'}
                                    class:text-right={column.align === 'right'}
                                    style={column.width ? `width: ${column.width}` : ''}
                                    on:click={() => handleSort(column.key)}
                                    role={sortable && column.sortable !== false ? 'button' : null}
                                    tabindex={sortable && column.sortable !== false ? 0 : null}
                                    on:keydown={(e) => {
                                        if ((e.key === 'Enter' || e.key === ' ') && sortable && column.sortable !== false) {
                                            e.preventDefault();
                                            handleSort(column.key);
                                        }
                                    }}
                            >
                                <div class="header-content">
                                    <span class="header-label">{column.label}</span>
                                    {#if sortable && column.sortable !== false && sortColumn === column.key}
                                            <span class="sort-indicator">
                                                {sortDirection === 'asc' ? '↑' : '↓'}
                                            </span>
                                    {/if}
                                </div>
                            </th>
                        {/each}

                        {#if actions.length > 0}
                            <th class="table-cell table-cell--actions">Actions</th>
                        {/if}
                    </tr>
                    </thead>
                {/if}

                <tbody class="table-body">
                {#each sortedData as item, index (item.id || index)}
                    <tr
                            class="table-row"
                            class:selected={selectedItems.has(item.id)}
                            in:fade={{ duration: 200, delay: index * 20 }}
                    >
                        {#if selectable}
                            <td class="table-cell table-cell--checkbox">
                                <input
                                        type="checkbox"
                                        class="checkbox"
                                        checked={selectedItems.has(item.id)}
                                        on:change={() => handleSelectItem(item)}
                                        aria-label={`Select row ${index + 1}`}
                                />
                            </td>
                        {/if}

                        {#each columns as column}
                            <td
                                    class="table-cell"
                                    class:text-center={column.align === 'center'}
                                    class:text-right={column.align === 'right'}
                                    class:font-mono={column.mono}
                                    class:font-bold={column.bold}
                            >
                                {#if column.component}
                                    <svelte:component this={column.component} data={item} value={getNestedValue(item, column.key)} />
                                {:else}
                                    {@html renderCellContent(item, column)}
                                {/if}
                            </td>
                        {/each}

                        {#if actions.length > 0}
                            <td class="table-cell table-cell--actions">
                                <div class="action-buttons">
                                    {#each actions as action}
                                        <ActionIcon
                                                icon={action.icon}
                                                variant={action.variant || 'default'}
                                                size="small"
                                                tooltip={action.tooltip || action.label}
                                                on:click={() => handleAction(action, item)}
                                        />
                                    {/each}
                                </div>
                            </td>
                        {/if}
                    </tr>
                {/each}
                </tbody>
            </table>
        </div>
    {/if}
</div>

<style>
    .table-container {
        background: white;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        border: 1px solid #f1f5f9;
        position: relative;
    }

    .table-wrapper {
        overflow-x: auto;
        max-height: 600px;
        overflow-y: auto;
    }

    .data-table {
        width: 100%;
        border-collapse: collapse;
        font-size: 0.875rem;
    }

    .table-head {
        position: sticky;
        top: 0;
        z-index: 10;
    }

    .table-cell {
        padding: 1rem;
        vertical-align: middle;
        border-bottom: 1px solid #f1f5f9;
        color: #1f2937;
    }

    .table-cell--header {
        background: #f8fafc;
        font-weight: 600;
        color: #374151;
        border-bottom: 1px solid #e5e7eb;
        user-select: none;
        position: relative;
    }

    .table-cell--header.sortable {
        cursor: pointer;
        transition: background-color 0.2s ease;
    }

    .table-cell--header.sortable:hover {
        background: #f1f5f9;
    }

    .table-cell--header.sorted {
        background: #eff6ff;
        color: #1d4ed8;
    }

    .header-content {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 0.5rem;
    }

    .header-label {
        flex: 1;
        min-width: 0;
    }

    .sort-indicator {
        font-size: 0.75rem;
        color: #3b82f6;
        flex-shrink: 0;
        width: 12px;
        text-align: center;
    }

    .table-cell--checkbox {
        width: 48px;
        text-align: center;
        padding-left: 1rem;
        padding-right: 0.5rem;
    }

    .table-cell--actions {
        width: auto;
        text-align: center;
        white-space: nowrap;
    }

    .table-row {
        transition: background-color 0.2s ease;
    }

    .data-table--hoverable .table-row:hover {
        background: #f9fafb;
    }

    .table-row.selected {
        background: #eff6ff;
    }

    .data-table--striped .table-row:nth-child(even) {
        background: #f9fafb;
    }

    .data-table--compact .table-cell {
        padding: 0.5rem;
    }

    .action-buttons {
        display: flex;
        gap: 0.25rem;
        justify-content: center;
        align-items: center;
    }

    .checkbox {
        width: 16px;
        height: 16px;
        accent-color: #3b82f6;
        cursor: pointer;
    }

    /* Text alignment classes */
    .text-center {
        text-align: center;
    }

    .text-right {
        text-align: right;
    }

    .font-mono {
        font-family: 'Courier New', monospace;
    }

    .font-bold {
        font-weight: 600;
    }

    /* Loading state */
    .table-loading {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 3rem 1rem;
        color: #64748b;
    }

    .loading-spinner {
        width: 32px;
        height: 32px;
        border: 3px solid #f1f5f9;
        border-top: 3px solid #3b82f6;
        border-radius: 50%;
        animation: spin 1s linear infinite;
        margin-bottom: 1rem;
    }

    @keyframes spin {
        to {
            transform: rotate(360deg);
        }
    }

    .loading-text {
        font-size: 0.875rem;
        margin: 0;
    }

    /* Empty state */
    .table-empty {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 3rem 1rem;
        color: #64748b;
        text-align: center;
    }

    .empty-icon {
        font-size: 3rem;
        margin-bottom: 1rem;
        opacity: 0.5;
    }

    .empty-message {
        font-size: 1rem;
        margin: 0 0 1rem 0;
        font-weight: 500;
    }

    /* 반응형 */
    @media (max-width: 768px) {
        .table-cell {
            padding: 0.75rem 0.5rem;
            font-size: 0.8rem;
        }

        .data-table--compact .table-cell {
            padding: 0.5rem 0.25rem;
        }

        .action-buttons {
            gap: 0.125rem;
        }

        .table-cell--checkbox {
            padding-left: 0.5rem;
            padding-right: 0.25rem;
        }
    }

    @media (max-width: 640px) {
        .table-wrapper {
            font-size: 0.75rem;
        }

        .table-cell {
            padding: 0.5rem 0.25rem;
        }

        .empty-icon {
            font-size: 2rem;
        }

        .loading-spinner {
            width: 24px;
            height: 24px;
            border-width: 2px;
        }
    }

    /* 접근성 */
    @media (prefers-reduced-motion: reduce) {
        .table-row,
        .table-cell--header {
            transition: none;
        }

        .loading-spinner {
            animation: none;
        }
    }

    /* 다크모드 지원 */
    @media (prefers-color-scheme: dark) {
        .table-container {
            background: #1e293b;
            border-color: #334155;
        }

        .table-cell--header {
            background: #334155;
            color: #f1f5f9;
            border-bottom-color: #475569;
        }

        .table-cell--header.sortable:hover {
            background: #475569;
        }

        .table-cell--header.sorted {
            background: #1e40af;
            color: #dbeafe;
        }

        .table-cell {
            color: #e2e8f0;
            border-bottom-color: #334155;
        }

        .data-table--hoverable .table-row:hover {
            background: #334155;
        }

        .table-row.selected {
            background: #1e3a8a;
        }

        .data-table--striped .table-row:nth-child(even) {
            background: #374151;
        }

        .table-loading,
        .table-empty {
            color: #94a3b8;
        }

        .loading-spinner {
            border-color: #475569;
            border-top-color: #3b82f6;
        }
    }

    /* 프린트 스타일 */
    @media print {
        .table-container {
            box-shadow: none;
            border: 1px solid #000;
        }

        .table-cell--actions {
            display: none;
        }

        .table-cell--checkbox {
            display: none;
        }

        .table-cell {
            border: 1px solid #000;
            padding: 0.5rem;
        }

        .action-buttons {
            display: none;
        }
    }

    /* 스크롤바 스타일링 */
    .table-wrapper::-webkit-scrollbar {
        width: 6px;
        height: 6px;
    }

    .table-wrapper::-webkit-scrollbar-track {
        background: #f1f5f9;
        border-radius: 3px;
    }

    .table-wrapper::-webkit-scrollbar-thumb {
        background: #cbd5e1;
        border-radius: 3px;
    }

    .table-wrapper::-webkit-scrollbar-thumb:hover {
        background: #94a3b8;
    }

    /* 포커스 스타일 */
    .table-cell--header.sortable:focus {
        outline: 2px solid #3b82f6;
        outline-offset: -2px;
    }
</style>
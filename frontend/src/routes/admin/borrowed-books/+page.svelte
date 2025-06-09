<!-- src/routes/admin/borrowed-books/+page.svelte -->
<script>
    import { onMount } from 'svelte';
    import { fade, scale } from 'svelte/transition';

    // 탭 상태
    let activeTab = 'borrowed'; // 'borrowed' 또는 'overdue'

    // 모달 상태
    let showOverdueDetailsModal = false;
    let selectedOverdueItem = null;

    // 검색
    let searchTerm = '';

    // 대출 도서 데이터
    let borrowedBooks = [
        { id: 1, userId: 1, userName: 'John Doe', bookCount: 3, borrowDate: '2024-01-15', dueDate: '2024-02-15', status: 'borrowed' },
        { id: 2, userId: 2, userName: 'Jane Smith', bookCount: 2, borrowDate: '2024-01-20', dueDate: '2024-02-20', status: 'borrowed' },
        { id: 3, userId: 3, userName: 'Mike Johnson', bookCount: 5, borrowDate: '2024-01-10', dueDate: '2024-02-10', status: 'borrowed' },
        { id: 4, userId: 4, userName: 'Sarah Wilson', bookCount: 1, borrowDate: '2024-01-25', dueDate: '2024-02-25', status: 'borrowed' },
        { id: 5, userId: 5, userName: 'Tom Brown', bookCount: 4, borrowDate: '2024-01-12', dueDate: '2024-02-12', status: 'borrowed' }
    ];

    // 연체 대출자 데이터
    let overdueBorrowers = [
        {
            id: 1,
            userId: 1,
            userName: 'Hisal Gunasekara',
            amount: '002 Books',
            dueDate: '13 - 03 - 2024',
            dateTime: '25-02-2024, 10:39:43',
            books: [
                { id: 1, name: 'Hibernate - Core', type: 'Educational', language: 'English' },
                { id: 2, name: 'Hibernate - Core', type: 'Educational', language: 'English' }
            ]
        },
        {
            id: 2,
            userId: 1,
            userName: 'Hisal Gunasekara',
            amount: '002 Books',
            dueDate: '13 - 03 - 2024',
            dateTime: '25-02-2024, 10:39:43',
            books: [
                { id: 3, name: 'Hibernate - Core', type: 'Educational', language: 'English' },
                { id: 4, name: 'Hibernate - Core', type: 'Educational', language: 'English' }
            ]
        },
        {
            id: 3,
            userId: 1,
            userName: 'Hisal Gunasekara',
            amount: '002 Books',
            dueDate: '13 - 03 - 2024',
            dateTime: '25-02-2024, 10:39:43',
            books: [
                { id: 5, name: 'Hibernate - Core', type: 'Educational', language: 'English' },
                { id: 6, name: 'Hibernate - Core', type: 'Educational', language: 'English' }
            ]
        },
        {
            id: 4,
            userId: 1,
            userName: 'Hisal Gunasekara',
            amount: '002 Books',
            dueDate: '13 - 03 - 2024',
            dateTime: '25-02-2024, 10:39:43',
            books: [
                { id: 7, name: 'Hibernate - Core', type: 'Educational', language: 'English' },
                { id: 8, name: 'Hibernate - Core', type: 'Educational', language: 'English' }
            ]
        },
        {
            id: 5,
            userId: 1,
            userName: 'Hisal Gunasekara',
            amount: '002 Books',
            dueDate: '13 - 03 - 2024',
            dateTime: '25-02-2024, 10:39:43',
            books: [
                { id: 9, name: 'Hibernate - Core', type: 'Educational', language: 'English' },
                { id: 10, name: 'Hibernate - Core', type: 'Educational', language: 'English' }
            ]
        },
        {
            id: 6,
            userId: 1,
            userName: 'Hisal Gunasekara',
            amount: '002 Books',
            dueDate: '13 - 03 - 2024',
            dateTime: '25-02-2024, 10:39:43',
            books: [
                { id: 11, name: 'Hibernate - Core', type: 'Educational', language: 'English' },
                { id: 12, name: 'Hibernate - Core', type: 'Educational', language: 'English' }
            ]
        },
        {
            id: 7,
            userId: 1,
            userName: 'Hisal Gunasekara',
            amount: '002 Books',
            dueDate: '13 - 03 - 2024',
            dateTime: '25-02-2024, 10:39:43',
            books: [
                { id: 13, name: 'Hibernate - Core', type: 'Educational', language: 'English' },
                { id: 14, name: 'Hibernate - Core', type: 'Educational', language: 'English' }
            ]
        },
        {
            id: 8,
            userId: 1,
            userName: 'Hisal Gunasekara',
            amount: '002 Books',
            dueDate: '13 - 03 - 2024',
            dateTime: '25-02-2024, 10:39:43',
            books: [
                { id: 15, name: 'Hibernate - Core', type: 'Educational', language: 'English' },
                { id: 16, name: 'Hibernate - Core', type: 'Educational', language: 'English' }
            ]
        },
        {
            id: 9,
            userId: 1,
            userName: 'Hisal Gunasekara',
            amount: '002 Books',
            dueDate: '13 - 03 - 2024',
            dateTime: '25-02-2024, 10:39:43',
            books: [
                { id: 17, name: 'Hibernate - Core', type: 'Educational', language: 'English' },
                { id: 18, name: 'Hibernate - Core', type: 'Educational', language: 'English' }
            ]
        },
        {
            id: 10,
            userId: 1,
            userName: 'Hisal Gunasekara',
            amount: '002 Books',
            dueDate: '13 - 03 - 2024',
            dateTime: '25-02-2024, 10:39:43',
            books: [
                { id: 19, name: 'Hibernate - Core', type: 'Educational', language: 'English' },
                { id: 20, name: 'Hibernate - Core', type: 'Educational', language: 'English' }
            ]
        },
        {
            id: 11,
            userId: 1,
            userName: 'Hisal Gunasekara',
            amount: '002 Books',
            dueDate: '13 - 03 - 2024',
            dateTime: '25-02-2024, 10:39:43',
            books: [
                { id: 21, name: 'Hibernate - Core', type: 'Educational', language: 'English' },
                { id: 22, name: 'Hibernate - Core', type: 'Educational', language: 'English' }
            ]
        },
        {
            id: 12,
            userId: 1,
            userName: 'Hisal Gunasekara',
            amount: '002 Books',
            dueDate: '13 - 03 - 2024',
            dateTime: '25-02-2024, 10:39:43',
            books: [
                { id: 23, name: 'Hibernate - Core', type: 'Educational', language: 'English' },
                { id: 24, name: 'Hibernate - Core', type: 'Educational', language: 'English' }
            ]
        }
    ];

    // 필터링된 데이터
    $: filteredBorrowedBooks = borrowedBooks.filter(item =>
        item.userName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        item.userId.toString().includes(searchTerm)
    );

    $: filteredOverdueBorrowers = overdueBorrowers.filter(item =>
        item.userName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        item.userId.toString().includes(searchTerm)
    );

    onMount(() => {
        console.log('Borrowed books management loaded');
    });

    function switchTab(tab) {
        activeTab = tab;
        searchTerm = ''; // 탭 변경 시 검색어 초기화
    }

    function openOverdueDetailsModal(item) {
        selectedOverdueItem = item;
        showOverdueDetailsModal = true;
    }

    function closeModal() {
        showOverdueDetailsModal = false;
        selectedOverdueItem = null;
    }

    function handleAction(action, item) {
        console.log(`Action: ${action}`, item);
        // 실제로는 API 호출
    }

    // 키보드 이벤트
    function handleKeydown(event) {
        if (event.key === 'Escape') {
            closeModal();
        }
    }
</script>

<svelte:head>
    <title>Overdue Borrowers - BookWorm Admin</title>
</svelte:head>

<svelte:window on:keydown={handleKeydown} />

<div class="borrowers-management-container">
    <!-- 페이지 헤더 -->
    <div class="page-header">
        <div class="header-left">
            <h1 class="page-title">Admin Overdue Borrowers Form</h1>
        </div>
        <div class="header-right">
            <span class="current-time">12:29 PM</span>
            <span class="current-date">Sep 25, 2024</span>
        </div>
    </div>

    <!-- 탭 네비게이션 -->
    <div class="tab-navigation">
        <button
                class="tab-btn {activeTab === 'borrowed' ? 'active' : ''}"
                on:click={() => switchTab('borrowed')}
        >
            Borrowed Books
        </button>
        <button
                class="tab-btn {activeTab === 'overdue' ? 'active' : ''}"
                on:click={() => switchTab('overdue')}
        >
            Overdue Borrowers
        </button>
    </div>

    <!-- 검색 컨트롤 -->
    <div class="search-controls">
        <div class="search-container">
            <input
                    type="text"
                    placeholder="Search by ID"
                    bind:value={searchTerm}
                    class="search-input"
            />
            <span class="search-icon">🔍</span>
        </div>
    </div>

    <!-- 콘텐츠 영역 -->
    <div class="content-area">
        {#if activeTab === 'borrowed'}
            <!-- 대출 도서 테이블 -->
            <div class="table-container" in:fade={{ duration: 300 }}>
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>User ID</th>
                        <th>User Name</th>
                        <th>Book Count</th>
                        <th>Borrow Date</th>
                        <th>Due Date</th>
                        <th>Action</th>
                    </tr>
                    </thead>
                    <tbody>
                    {#each filteredBorrowedBooks as book (book.id)}
                        <tr class="data-row" in:fade={{ duration: 200 }}>
                            <td class="book-id">{book.id.toString().padStart(3, '0')}</td>
                            <td class="user-id">{book.userId}</td>
                            <td class="user-name">{book.userName}</td>
                            <td class="book-count">{book.bookCount} Books</td>
                            <td class="borrow-date">{book.borrowDate}</td>
                            <td class="due-date">{book.dueDate}</td>
                            <td class="action-cell">
                                <button
                                        class="action-btn"
                                        on:click={() => handleAction('view', book)}
                                        title="View Details"
                                >
                                    📋
                                </button>
                            </td>
                        </tr>
                    {/each}
                    </tbody>
                </table>
            </div>
        {:else}
            <!-- 연체 대출자 테이블 -->
            <div class="table-container" in:fade={{ duration: 300 }}>
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>User ID</th>
                        <th>Amount</th>
                        <th>Due Date</th>
                        <th>Date & Time</th>
                        <th>Action</th>
                    </tr>
                    </thead>
                    <tbody>
                    {#each filteredOverdueBorrowers as borrower (borrower.id)}
                        <tr class="data-row overdue-row" in:fade={{ duration: 200 }}>
                            <td class="borrower-id">{borrower.id.toString().padStart(3, '0')}</td>
                            <td class="user-id">{borrower.userId}</td>
                            <td class="amount">{borrower.amount}</td>
                            <td class="due-date overdue">{borrower.dueDate}</td>
                            <td class="date-time">{borrower.dateTime}</td>
                            <td class="action-cell">
                                <button
                                        class="action-btn overdue-action"
                                        on:click={() => openOverdueDetailsModal(borrower)}
                                        title="View Overdue Details"
                                >
                                    📋
                                </button>
                            </td>
                        </tr>
                    {/each}
                    </tbody>
                </table>
            </div>
        {/if}
    </div>
</div>

<!-- 연체 상세 정보 모달 -->
{#if showOverdueDetailsModal && selectedOverdueItem}
    <div class="modal-overlay" on:click={closeModal} transition:fade>
        <div class="modal-container overdue-details-modal" on:click|stopPropagation transition:scale>
            <div class="modal-header">
                <div class="modal-title">
                    <span class="modal-icon">📋</span>
                    Admin Overdue Books View PopUp
                </div>
                <button class="close-btn" on:click={closeModal}>✕</button>
            </div>

            <div class="modal-body">
                <!-- 연체 책 목록 -->
                <div class="overdue-books-list">
                    <table class="modal-table">
                        <thead>
                        <tr>
                            <th>Book ID</th>
                            <th>Name</th>
                            <th>Type</th>
                            <th>Language</th>
                        </tr>
                        </thead>
                        <tbody>
                        {#each selectedOverdueItem.books as book (book.id)}
                            <tr class="modal-row">
                                <td class="modal-book-id">{book.id}</td>
                                <td class="modal-book-name">{book.name}</td>
                                <td class="modal-book-type">{book.type}</td>
                                <td class="modal-book-language">{book.language}</td>
                            </tr>
                        {/each}
                        </tbody>
                    </table>
                </div>

                <!-- 요약 정보 -->
                <div class="summary-info">
                    <div class="summary-row">
                        <span class="summary-label">ID:</span>
                        <span class="summary-value">{selectedOverdueItem.id}</span>
                    </div>
                    <div class="summary-row">
                        <span class="summary-label">Total Books:</span>
                        <span class="summary-value">{selectedOverdueItem.books.length} Books</span>
                    </div>
                    <div class="summary-row">
                        <span class="summary-label">Due Date:</span>
                        <span class="summary-value overdue-date">13 - 12 - 2024</span>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button class="btn primary" on:click={closeModal}>CLOSE</button>
            </div>
        </div>
    </div>
{/if}

<style>
    .borrowers-management-container {
        padding: 2rem;
        background: #f8fafc;
        min-height: 100vh;
        font-family: 'Inter', sans-serif;
    }

    /* 페이지 헤더 */
    .page-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 2rem;
        padding-bottom: 1rem;
        border-bottom: 1px solid #e2e8f0;
    }

    .page-title {
        font-size: 1.75rem;
        font-weight: 600;
        color: #1e293b;
        margin: 0;
    }

    .header-right {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        gap: 0.25rem;
    }

    .current-time {
        font-size: 1rem;
        font-weight: 600;
        color: #1e293b;
    }

    .current-date {
        font-size: 0.875rem;
        color: #64748b;
    }

    /* 탭 네비게이션 */
    .tab-navigation {
        display: flex;
        background: white;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        border: 1px solid #f1f5f9;
        margin-bottom: 1.5rem;
        width: fit-content;
    }

    .tab-btn {
        padding: 1rem 2rem;
        background: transparent;
        border: none;
        color: #64748b;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s ease;
        position: relative;
    }

    .tab-btn.active {
        background: #1e293b;
        color: white;
    }

    .tab-btn:not(.active):hover {
        background: #f8fafc;
        color: #374151;
    }

    /* 검색 컨트롤 */
    .search-controls {
        display: flex;
        justify-content: flex-end;
        margin-bottom: 1.5rem;
    }

    .search-container {
        position: relative;
    }

    .search-input {
        padding: 0.75rem 1rem 0.75rem 2.5rem;
        border: 1px solid #d1d5db;
        border-radius: 8px;
        width: 300px;
        font-size: 0.875rem;
        transition: border-color 0.2s ease;
    }

    .search-input:focus {
        outline: none;
        border-color: #3b82f6;
        box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
    }

    .search-icon {
        position: absolute;
        left: 0.75rem;
        top: 50%;
        transform: translateY(-50%);
        color: #6b7280;
    }

    /* 테이블 */
    .table-container {
        background: white;
        border-radius: 12px;
        overflow: hidden;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        border: 1px solid #f1f5f9;
    }

    .data-table {
        width: 100%;
        border-collapse: collapse;
    }

    .data-table th {
        background: #f8fafc;
        padding: 1rem;
        text-align: left;
        font-weight: 600;
        color: #374151;
        border-bottom: 1px solid #e5e7eb;
        font-size: 0.875rem;
    }

    .data-table td {
        padding: 1rem;
        border-bottom: 1px solid #f3f4f6;
        color: #1f2937;
        font-size: 0.875rem;
    }

    .data-row:hover {
        background: #f9fafb;
    }

    .overdue-row {
        background: rgba(239, 68, 68, 0.02);
    }

    .overdue-row:hover {
        background: rgba(239, 68, 68, 0.05);
    }

    .book-id,
    .borrower-id,
    .user-id {
        font-weight: 500;
        color: #6b7280;
    }

    .user-name {
        font-weight: 500;
        color: #1f2937;
    }

    .due-date.overdue {
        color: #dc2626;
        font-weight: 500;
    }

    .date-time {
        color: #6b7280;
        font-size: 0.8rem;
    }

    .action-cell {
        text-align: center;
    }

    .action-btn {
        width: 32px;
        height: 32px;
        border: none;
        border-radius: 6px;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 0.875rem;
        transition: all 0.2s ease;
        background: #eff6ff;
        color: #1d4ed8;
    }

    .action-btn:hover {
        background: #dbeafe;
        transform: scale(1.05);
    }

    .action-btn.overdue-action {
        background: #fef2f2;
        color: #dc2626;
    }

    .action-btn.overdue-action:hover {
        background: #fee2e2;
    }

    /* 모달 */
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

    .modal-container {
        background: white;
        border-radius: 12px;
        width: 100%;
        max-width: 800px;
        max-height: 90vh;
        overflow-y: auto;
        box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
    }

    .modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1.5rem;
        border-bottom: 1px solid #f1f5f9;
    }

    .modal-title {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        font-size: 1.25rem;
        font-weight: 600;
        color: #1f2937;
    }

    .modal-icon {
        font-size: 1.5rem;
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
    }

    .close-btn:hover {
        background: #e5e7eb;
        color: #374151;
    }

    .modal-body {
        padding: 1.5rem;
    }

    /* 모달 테이블 */
    .modal-table {
        width: 100%;
        border-collapse: collapse;
        margin-bottom: 2rem;
    }

    .modal-table th {
        background: #f8fafc;
        padding: 0.75rem;
        text-align: left;
        font-weight: 600;
        color: #374151;
        border-bottom: 1px solid #e5e7eb;
        font-size: 0.875rem;
    }

    .modal-table td {
        padding: 0.75rem;
        border-bottom: 1px solid #f3f4f6;
        color: #1f2937;
        font-size: 0.875rem;
    }

    .modal-row:hover {
        background: #f9fafb;
    }

    .modal-book-id {
        font-weight: 500;
        color: #6b7280;
    }

    .modal-book-name {
        font-weight: 500;
        color: #1f2937;
    }

    /* 요약 정보 */
    .summary-info {
        background: #f8fafc;
        border-radius: 8px;
        padding: 1.5rem;
        border: 1px solid #e2e8f0;
    }

    .summary-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1rem;
    }

    .summary-row:last-child {
        margin-bottom: 0;
    }

    .summary-label {
        font-weight: 600;
        color: #374151;
        font-size: 0.875rem;
    }

    .summary-value {
        color: #1f2937;
        font-size: 0.875rem;
    }

    .summary-value.overdue-date {
        color: #dc2626;
        font-weight: 500;
    }

    .modal-footer {
        display: flex;
        justify-content: flex-end;
        padding: 1.5rem;
        border-top: 1px solid #f1f5f9;
    }

    .btn {
        padding: 0.75rem 1.5rem;
        border-radius: 6px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s ease;
        border: none;
        font-size: 0.875rem;
        text-transform: uppercase;
        letter-spacing: 0.025em;
    }

    .btn.primary {
        background: #1e293b;
        color: white;
    }

    .btn.primary:hover {
        background: #334155;
    }

    /* 반응형 디자인 */
    @media (max-width: 1024px) {
        .search-controls {
            justify-content: center;
        }

        .search-input {
            width: 100%;
            max-width: 400px;
        }

        .modal-container {
            margin: 1rem;
            max-width: none;
        }
    }

    @media (max-width: 768px) {
        .borrowers-management-container {
            padding: 1rem;
        }

        .page-header {
            flex-direction: column;
            align-items: flex-start;
            gap: 1rem;
        }

        .header-right {
            align-items: flex-start;
        }

        .tab-navigation {
            width: 100%;
        }

        .tab-btn {
            flex: 1;
            text-align: center;
        }

        .data-table {
            font-size: 0.75rem;
        }

        .data-table th,
        .data-table td {
            padding: 0.5rem;
        }

        .modal-header,
        .modal-body,
        .modal-footer {
            padding: 1rem;
        }
    }

    @media (max-width: 640px) {
        .data-table,
        .modal-table {
            display: block;
            overflow-x: auto;
            white-space: nowrap;
        }

        .action-btn {
            width: 28px;
            height: 28px;
            font-size: 0.75rem;
        }

        .summary-row {
            flex-direction: column;
            align-items: flex-start;
            gap: 0.25rem;
        }
    }

    /* 접근성 */
    @media (prefers-reduced-motion: reduce) {
        .tab-btn,
        .action-btn,
        .btn,
        .close-btn {
            transition: none;
        }
    }

    /* 다크모드 지원 */
    @media (prefers-color-scheme: dark) {
        .borrowers-management-container {
            background: #0f172a;
            color: #f1f5f9;
        }

        .table-container,
        .modal-container {
            background: #1e293b;
            border-color: #334155;
        }

        .data-table th,
        .modal-table th {
            background: #334155;
            color: #f1f5f9;
            border-bottom-color: #475569;
        }

        .data-table td,
        .modal-table td {
            color: #e2e8f0;
            border-bottom-color: #334155;
        }

        .data-row:hover,
        .modal-row:hover {
            background: #334155;
        }

        .overdue-row {
            background: rgba(239, 68, 68, 0.1);
        }

        .overdue-row:hover {
            background: rgba(239, 68, 68, 0.15);
        }

        .search-input {
            background: #334155;
            border-color: #475569;
            color: #f1f5f9;
        }

        .tab-navigation {
            background: #1e293b;
            border-color: #334155;
        }

        .tab-btn {
            color: #94a3b8;
        }

        .tab-btn.active {
            background: #0f172a;
            color: #f1f5f9;
        }

        .tab-btn:not(.active):hover {
            background: #334155;
            color: #f1f5f9;
        }

        .modal-header,
        .modal-footer {
            border-color: #334155;
        }

        .summary-info {
            background: #334155;
            border-color: #475569;
        }

        .page-title,
        .current-time,
        .modal-title {
            color: #f1f5f9;
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

    /* 로딩 상태 */
    .loading {
        opacity: 0.6;
        pointer-events: none;
        position: relative;
    }

    .loading::after {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(255, 255, 255, 0.8);
        display: flex;
        align-items: center;
        justify-content: center;
    }

    /* 애니메이션 */
    @keyframes fadeIn {
        from {
            opacity: 0;
            transform: translateY(10px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    .data-row {
        animation: fadeIn 0.3s ease-out;
    }

    /* 스크롤바 스타일링 */
    .table-container::-webkit-scrollbar,
    .modal-container::-webkit-scrollbar {
        width: 6px;
        height: 6px;
    }

    .table-container::-webkit-scrollbar-track,
    .modal-container::-webkit-scrollbar-track {
        background: #f1f5f9;
        border-radius: 3px;
    }

    .table-container::-webkit-scrollbar-thumb,
    .modal-container::-webkit-scrollbar-thumb {
        background: #cbd5e1;
        border-radius: 3px;
    }

    .table-container::-webkit-scrollbar-thumb:hover,
    .modal-container::-webkit-scrollbar-thumb:hover {
        background: #94a3b8;
    }

    /* 포커스 스타일 */
    .tab-btn:focus,
    .action-btn:focus,
    .btn:focus,
    .close-btn:focus {
        outline: 2px solid #3b82f6;
        outline-offset: 2px;
    }

    .search-input:focus {
        outline: 2px solid #3b82f6;
        outline-offset: 2px;
    }

    /* 상태 표시 */
    .status-indicator {
        display: inline-flex;
        align-items: center;
        gap: 0.5rem;
        padding: 0.25rem 0.75rem;
        border-radius: 20px;
        font-size: 0.75rem;
        font-weight: 500;
        text-transform: uppercase;
    }

    .status-indicator.overdue {
        background: #fef2f2;
        color: #dc2626;
        border: 1px solid #fecaca;
    }

    .status-indicator.borrowed {
        background: #fef3c7;
        color: #d97706;
        border: 1px solid #fed7aa;
    }

    .status-indicator.returned {
        background: #dcfce7;
        color: #166534;
        border: 1px solid #bbf7d0;
    }

    /* 통계 정보 */
    .stats-summary {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 1rem;
        margin-bottom: 2rem;
    }

    .stat-card {
        background: white;
        border-radius: 8px;
        padding: 1.5rem;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        border: 1px solid #f1f5f9;
    }

    .stat-number {
        font-size: 2rem;
        font-weight: 700;
        color: #1e293b;
        line-height: 1;
    }

    .stat-label {
        font-size: 0.875rem;
        color: #64748b;
        margin-top: 0.5rem;
    }

    .stat-card.danger .stat-number {
        color: #dc2626;
    }

    .stat-card.warning .stat-number {
        color: #d97706;
    }

    .stat-card.success .stat-number {
        color: #166534;
    }

    /* 테이블 정렬 */
    .sortable-header {
        cursor: pointer;
        user-select: none;
        position: relative;
        display: flex;
        align-items: center;
        gap: 0.5rem;
    }

    .sortable-header:hover {
        background: #f1f5f9;
    }

    .sort-icon {
        font-size: 0.75rem;
        color: #9ca3af;
        transition: transform 0.2s ease;
    }

    .sort-icon.asc {
        transform: rotate(180deg);
    }

    /* 액션 버튼 그룹 */
    .action-group {
        display: flex;
        gap: 0.5rem;
        align-items: center;
    }

    .action-group .action-btn {
        width: 28px;
        height: 28px;
        font-size: 0.75rem;
    }

    /* 빈 상태 */
    .empty-state {
        text-align: center;
        padding: 3rem 1rem;
        color: #6b7280;
    }

    .empty-state-icon {
        font-size: 3rem;
        margin-bottom: 1rem;
        opacity: 0.5;
    }

    .empty-state-title {
        font-size: 1.125rem;
        font-weight: 600;
        margin-bottom: 0.5rem;
        color: #374151;
    }

    .empty-state-description {
        font-size: 0.875rem;
        color: #6b7280;
    }

    /* 페이지네이션 */
    .pagination {
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 0.5rem;
        margin-top: 2rem;
    }

    .page-btn {
        padding: 0.5rem 1rem;
        border: 1px solid #d1d5db;
        background: white;
        color: #374151;
        border-radius: 6px;
        cursor: pointer;
        font-size: 0.875rem;
        transition: all 0.2s ease;
        min-width: 40px;
        text-align: center;
    }

    .page-btn:hover:not(:disabled) {
        background: #f3f4f6;
    }

    .page-btn.active {
        background: #3b82f6;
        color: white;
        border-color: #3b82f6;
    }

    .page-btn:disabled {
        opacity: 0.5;
        cursor: not-allowed;
    }

    .page-info {
        margin: 0 1rem;
        font-size: 0.875rem;
        color: #6b7280;
    }

    /* 툴팁 */
    .tooltip {
        position: relative;
        display: inline-block;
    }

    .tooltip::before {
        content: attr(data-tooltip);
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
        opacity: 0;
        pointer-events: none;
        transition: opacity 0.2s ease;
        z-index: 1000;
    }

    .tooltip:hover::before {
        opacity: 1;
    }

    /* 프린트 스타일 */
    @media print {
        .borrowers-management-container {
            background: white;
            padding: 1rem;
        }

        .page-header,
        .search-controls,
        .tab-navigation {
            display: none;
        }

        .table-container {
            box-shadow: none;
            border: 1px solid #000;
        }

        .action-cell {
            display: none;
        }

        .data-table th,
        .data-table td {
            border: 1px solid #000;
            padding: 0.5rem;
        }
    }
</style>
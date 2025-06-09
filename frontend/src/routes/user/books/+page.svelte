<!-- =============================================== -->
<!-- src/routes/user/books/+page.svelte -->
<!-- =============================================== -->

<script>
    import { onMount, getContext } from 'svelte';
    import { fade, scale, slide } from 'svelte/transition';

    // 레이아웃에서 제공하는 컨텍스트 사용
    const { currentUser, setError } = getContext('userLayout');

    // 도서 관련 상태 (페이지별 상태만)
    let books = [];
    let selectedBooks = new Set();
    let availableBooks = [];
    let loading = false;

    // 검색 및 필터링
    let searchKeyword = '';
    let selectedType = '';
    let selectedLanguage = '';

    // 대출 관련 상태
    let showConfirmModal = false;
    let borrowConfirmData = null;
    let totalSelectedBooks = 0;

    // ==================== 백엔드 API 연동 함수들 ====================

    /**
     * 도서 목록 조회 API 호출
     */
    async function fetchAvailableBooks() {
        loading = true;

        try {
            console.log('📚 이용 가능한 도서 목록 조회 시작');

            const response = await fetch('/api/books?status=AVAILABLE', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();

            // BookResponse 구조에 맞게 데이터 처리
            if (result.success && result.data) {
                availableBooks = result.data.content || result.data;
                books = [...availableBooks];

                console.log('✅ 도서 목록 로드 완료:', books.length);
            } else {
                throw new Error(result.message || '도서 목록 조회 실패');
            }

        } catch (err) {
            console.error('❌ 도서 목록 조회 실패:', err);
            setError(err.message);
            books = generateMockBooks(); // 개발 중에는 목업 데이터 사용
        } finally {
            loading = false;
        }
    }

    /**
     * 도서 검색 API 호출
     */
    async function searchBooks() {
        if (!searchKeyword.trim() && !selectedType && !selectedLanguage) {
            books = [...availableBooks];
            return;
        }

        loading = true;

        try {
            console.log('🔍 도서 검색 시작:', { searchKeyword, selectedType, selectedLanguage });

            const searchParams = new URLSearchParams();
            if (searchKeyword.trim()) searchParams.append('keyword', searchKeyword.trim());
            if (selectedType) searchParams.append('type', selectedType);
            if (selectedLanguage) searchParams.append('language', selectedLanguage);
            searchParams.append('status', 'AVAILABLE'); // 대출 가능한 도서만

            const response = await fetch(`/api/books/search?${searchParams}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();

            if (result.success && result.data) {
                books = result.data.content || result.data;
                console.log('✅ 도서 검색 완료:', books.length);
            } else {
                throw new Error(result.message || '도서 검색 실패');
            }

        } catch (err) {
            console.error('❌ 도서 검색 실패:', err);
            setError(err.message);
            // 검색 실패 시 필터링으로 대체
            filterBooksLocally();
        } finally {
            loading = false;
        }
    }

    /**
     * 도서 대출 API 호출
     */
    async function borrowSelectedBooks() {
        if (selectedBooks.size === 0) {
            setError('대출할 도서를 선택해주세요.');
            return;
        }

        loading = true;

        try {
            console.log('📝 도서 대출 요청 시작');

            // BorrowRequestDto 구조에 맞게 요청 데이터 구성
            const borrowRequest = {
                userId: currentUser.id,
                bookIds: Array.from(selectedBooks),
                loanPeriodDays: 14 // 기본 2주 대출
            };

            console.log('📤 대출 요청 데이터:', borrowRequest);

            const response = await fetch('/api/loans/borrow', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                },
                body: JSON.stringify(borrowRequest)
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();

            // BorrowResponseDto 처리
            if (result && (result.loanId || result.success)) {
                console.log('✅ 도서 대출 성공:', result);

                // 성공 처리
                borrowConfirmData = {
                    loanId: result.loanId,
                    userName: result.userName || currentUser.name,
                    borrowedBooks: result.borrowedBooks || getSelectedBooksInfo(),
                    loanDate: result.loanDate || getCurrentDate(),
                    dueDate: result.dueDate || getDueDate(),
                    totalBooks: result.totalBooks || selectedBooks.size
                };

                // 선택 초기화 및 도서 목록 새로고침
                selectedBooks.clear();
                selectedBooks = new Set();
                await fetchAvailableBooks();

                showConfirmModal = true;

            } else {
                throw new Error(result?.message || '도서 대출 실패');
            }

        } catch (err) {
            console.error('❌ 도서 대출 실패:', err);

            // LoanBusinessException 처리
            if (err.message.includes('대출 한도')) {
                setError('대출 한도를 초과했습니다. 기존 대출 도서를 반납 후 다시 시도해주세요.');
            } else if (err.message.includes('이미 대출')) {
                setError('이미 대출 중인 도서가 포함되어 있습니다.');
            } else if (err.message.includes('예약')) {
                setError('예약된 도서가 포함되어 있습니다.');
            } else {
                setError(err.message || '도서 대출 중 오류가 발생했습니다.');
            }
        } finally {
            loading = false;
        }
    }

    // ==================== 유틸리티 함수들 ====================

    /**
     * 개발용 목업 도서 데이터 생성
     */
    function generateMockBooks() {
        return Array.from({ length: 12 }, (_, i) => ({
            id: i + 1,
            title: `Hibernate Core -11th`,
            type: 'Educational',
            language: 'English',
            quantity: Math.floor(Math.random() * 5) + 1,
            status: Math.random() > 0.3 ? 'AVAILABLE' : 'BORROWED',
            isAvailable: Math.random() > 0.3
        }));
    }

    /**
     * 로컬 필터링 (검색 API 실패시 백업)
     */
    function filterBooksLocally() {
        books = availableBooks.filter(book => {
            const matchKeyword = !searchKeyword.trim() ||
                book.title.toLowerCase().includes(searchKeyword.toLowerCase());
            const matchType = !selectedType || book.type === selectedType;
            const matchLanguage = !selectedLanguage || book.language === selectedLanguage;

            return matchKeyword && matchType && matchLanguage && book.isAvailable;
        });
    }

    /**
     * 도서 선택/해제 처리
     */
    function toggleBookSelection(bookId) {
        if (selectedBooks.has(bookId)) {
            selectedBooks.delete(bookId);
        } else {
            selectedBooks.add(bookId);
        }
        selectedBooks = new Set(selectedBooks); // 반응성 트리거
        totalSelectedBooks = selectedBooks.size;
    }

    /**
     * 선택된 도서 정보 가져오기
     */
    function getSelectedBooksInfo() {
        return books.filter(book => selectedBooks.has(book.id))
            .map(book => ({
                id: book.id,
                title: book.title,
                type: book.type,
                language: book.language
            }));
    }

    /**
     * 현재 날짜 반환
     */
    function getCurrentDate() {
        return new Date().toISOString().split('T')[0];
    }

    /**
     * 반납 예정일 계산 (14일 후)
     */
    function getDueDate() {
        const date = new Date();
        date.setDate(date.getDate() + 14);
        return date.toISOString().split('T')[0];
    }

    /**
     * 검색 실행
     */
    function handleSearch() {
        if (searchKeyword.trim() || selectedType || selectedLanguage) {
            searchBooks();
        } else {
            books = [...availableBooks];
        }
    }

    /**
     * 모달 닫기
     */
    function closeModal() {
        showConfirmModal = false;
        borrowConfirmData = null;
    }

    // ==================== 생명주기 ====================

    onMount(async () => {
        console.log('📚 도서 대출 페이지 초기화');
        await fetchAvailableBooks();
    });

    // 반응성 - 선택된 도서 수 업데이트
    $: totalSelectedBooks = selectedBooks.size;
    $: canBorrow = totalSelectedBooks > 0 && !loading;
</script>

<svelte:head>
    <title>도서 대출 - BookWorm Library</title>
</svelte:head>

<svelte:window on:keydown={(e) => {
    if (e.key === 'Enter' && e.target.type === 'text') handleSearch();
    else if (e.key === 'Escape') closeModal();
}} />

<!-- 도서 대출 폼 (레이아웃은 자동 적용) -->
<div class="borrow-form">
    <div class="form-header">
        <h2>Library Lane Books</h2>
        <div class="search-controls">
            <div class="search-tabs">
                <button class="tab-btn active">All books</button>
                <button class="tab-btn">Search by ID or type</button>
            </div>

            <div class="search-bar">
                <input
                        type="text"
                        placeholder="Search books..."
                        bind:value={searchKeyword}
                        on:input={handleSearch}
                        class="search-input"
                />
                <button class="search-btn" on:click={handleSearch}>
                    🔍
                </button>
            </div>
        </div>
    </div>

    <!-- 필터 컨트롤 -->
    <div class="filter-controls">
        <select bind:value={selectedType} on:change={handleSearch} class="filter-select">
            <option value="">All Types</option>
            <option value="Educational">Educational</option>
            <option value="Fiction">Fiction</option>
            <option value="Non-Fiction">Non-Fiction</option>
            <option value="Science">Science</option>
            <option value="Technology">Technology</option>
        </select>

        <select bind:value={selectedLanguage} on:change={handleSearch} class="filter-select">
            <option value="">All Languages</option>
            <option value="English">English</option>
            <option value="Korean">Korean</option>
            <option value="Spanish">Spanish</option>
        </select>
    </div>

    <!-- 도서 테이블 -->
    <div class="books-table-container">
        {#if loading}
            <div class="loading-overlay">
                <div class="loading-spinner"></div>
                <span>Loading books...</span>
            </div>
        {/if}

        <table class="books-table">
            <thead>
            <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Type</th>
                <th>Language</th>
                <th>Availability</th>
                <th>Add to Cart</th>
            </tr>
            </thead>
            <tbody>
            {#each books as book (book.id)}
                <tr class="book-row"
                    class:selected={selectedBooks.has(book.id)}
                    in:fade={{ duration: 200 }}>
                    <td class="book-id">{book.id}</td>
                    <td class="book-name">{book.title}</td>
                    <td class="book-type">{book.type}</td>
                    <td class="book-language">{book.language}</td>
                    <td class="book-availability">
                            <span class="status-badge {book.status?.toLowerCase()}">
                                {book.isAvailable ? 'Available' : 'Borrowed'}
                            </span>
                    </td>
                    <td class="book-action">
                        {#if book.isAvailable}
                            <input
                                    type="checkbox"
                                    checked={selectedBooks.has(book.id)}
                                    on:change={() => toggleBookSelection(book.id)}
                                    class="book-checkbox"
                                    disabled={loading}
                            />
                        {:else}
                            <span class="unavailable-text">Unavailable</span>
                        {/if}
                    </td>
                </tr>
            {:else}
                <tr>
                    <td colspan="6" class="no-books">
                        {loading ? 'Loading books...' : 'No books found'}
                    </td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>

    <!-- 대출 확인 버튼 -->
    <div class="borrow-actions">
        <div class="selected-count">
            Selected: {totalSelectedBooks} book{totalSelectedBooks !== 1 ? 's' : ''}
        </div>
        <button
                class="borrow-btn"
                class:disabled={!canBorrow}
                disabled={!canBorrow}
                on:click={borrowSelectedBooks}
        >
            {#if loading}
                <span class="btn-spinner"></span>
                Processing...
            {:else}
                Borrow Selected Books
            {/if}
        </button>
    </div>
</div>

<!-- 대출 확인 모달 -->
{#if showConfirmModal && borrowConfirmData}
    <div class="modal-overlay" on:click={closeModal} in:fade>
        <div class="confirm-modal" on:click|stopPropagation in:scale>
            <div class="modal-header">
                <h3>도서 대출 확인</h3>
                <button class="close-btn" on:click={closeModal}>✕</button>
            </div>

            <div class="modal-content">
                <div class="confirm-info">
                    <div class="info-row">
                        <span class="label">Book ID:</span>
                        <span class="value">{borrowConfirmData.loanId}</span>
                    </div>

                    <div class="info-row">
                        <span class="label">Name:</span>
                        <span class="value">{borrowConfirmData.userName}</span>
                    </div>

                    <div class="info-row">
                        <span class="label">Type:</span>
                        <span class="value">
                            {borrowConfirmData.borrowedBooks.map(b => b.type).join(', ')}
                        </span>
                    </div>

                    <div class="info-row">
                        <span class="label">Language:</span>
                        <span class="value">
                            {borrowConfirmData.borrowedBooks.map(b => b.language).join(', ')}
                        </span>
                    </div>

                    <div class="info-row">
                        <span class="label">Action:</span>
                        <span class="value">
                            <button class="delete-btn">🗑️</button>
                        </span>
                    </div>
                </div>

                <div class="loan-summary">
                    <div class="summary-row">
                        <span class="label">ID:</span>
                        <span class="value">{borrowConfirmData.loanId}</span>
                    </div>

                    <div class="summary-row">
                        <span class="label">Total Books:</span>
                        <span class="value">{borrowConfirmData.totalBooks} Books</span>
                    </div>

                    <div class="summary-row">
                        <span class="label">Due Date:</span>
                        <span class="value">{borrowConfirmData.dueDate}</span>
                    </div>
                </div>

                <div class="modal-actions">
                    <button class="cancel-btn" on:click={closeModal}>
                        CANCEL
                    </button>
                    <button class="confirm-btn" on:click={closeModal}>
                        CONFIRM
                    </button>
                </div>
            </div>
        </div>
    </div>
{/if}

<style>
    /* ==================== 도서 대출 폼 ==================== */
    .borrow-form {
        background: white;
        border-radius: 12px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        overflow: hidden;
        border: 1px solid #e5e7eb;
    }

    .form-header {
        padding: 1.5rem;
        border-bottom: 1px solid #e5e7eb;
        background: #f9fafb;
    }

    .form-header h2 {
        font-size: 1.5rem;
        font-weight: 600;
        color: #111827;
        margin: 0 0 1rem 0;
    }

    .search-controls {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 1rem;
    }

    .search-tabs {
        display: flex;
        gap: 0.5rem;
    }

    .tab-btn {
        padding: 0.5rem 1rem;
        background: white;
        border: 1px solid #d1d5db;
        border-radius: 6px;
        font-size: 0.875rem;
        cursor: pointer;
        transition: all 0.2s;
        color: #6b7280;
    }

    .tab-btn.active {
        background: #000000;
        color: white;
        border-color: #000000;
    }

    .tab-btn:hover:not(.active) {
        background: #f3f4f6;
        border-color: #9ca3af;
    }

    .search-bar {
        display: flex;
        gap: 0.5rem;
    }

    .search-input {
        padding: 0.75rem;
        border: 1px solid #d1d5db;
        border-radius: 6px;
        width: 250px;
        font-size: 0.875rem;
        transition: border-color 0.2s;
    }

    .search-input:focus {
        outline: none;
        border-color: #000000;
        box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.1);
    }

    .search-btn {
        padding: 0.75rem 1rem;
        background: #000000;
        color: white;
        border: none;
        border-radius: 6px;
        cursor: pointer;
        transition: background-color 0.2s;
    }

    .search-btn:hover {
        background: #374151;
    }

    /* 필터 컨트롤 */
    .filter-controls {
        padding: 1rem 1.5rem;
        border-bottom: 1px solid #e5e7eb;
        display: flex;
        gap: 1rem;
        background: #f9fafb;
    }

    .filter-select {
        padding: 0.5rem;
        border: 1px solid #d1d5db;
        border-radius: 6px;
        background: white;
        font-size: 0.875rem;
        cursor: pointer;
        min-width: 120px;
        color: #374151;
    }

    .filter-select:focus {
        outline: none;
        border-color: #000000;
    }

    /* ==================== 도서 테이블 ==================== */
    .books-table-container {
        position: relative;
        max-height: 400px;
        overflow-y: auto;
    }

    .loading-overlay {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(255, 255, 255, 0.9);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 1rem;
        z-index: 10;
    }

    .loading-spinner {
        width: 32px;
        height: 32px;
        border: 3px solid #f3f4f6;
        border-top: 3px solid #000000;
        border-radius: 50%;
        animation: spin 1s linear infinite;
    }

    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
    }

    .books-table {
        width: 100%;
        border-collapse: collapse;
    }

    .books-table th {
        background: #f9fafb;
        padding: 1rem;
        text-align: left;
        font-weight: 600;
        color: #111827;
        border-bottom: 2px solid #e5e7eb;
        font-size: 0.875rem;
        position: sticky;
        top: 0;
        z-index: 5;
    }

    .books-table td {
        padding: 1rem;
        border-bottom: 1px solid #e5e7eb;
        color: #374151;
        font-size: 0.875rem;
    }

    .book-row {
        transition: background-color 0.2s;
    }

    .book-row:hover {
        background: #f9fafb;
    }

    .book-row.selected {
        background: #f3f4f6;
        border-left: 4px solid #000000;
    }

    .book-id {
        font-weight: 500;
        color: #6b7280;
        width: 60px;
    }

    .book-name {
        font-weight: 500;
        color: #111827;
        max-width: 200px;
    }

    .book-type,
    .book-language {
        color: #6b7280;
    }

    .status-badge {
        padding: 0.25rem 0.75rem;
        border-radius: 12px;
        font-size: 0.75rem;
        font-weight: 500;
        text-transform: uppercase;
        border: 1px solid;
    }

    .status-badge.available {
        background: #f0fdf4;
        color: #166534;
        border-color: #22c55e;
    }

    .status-badge.borrowed {
        background: #fef3c7;
        color: #92400e;
        border-color: #f59e0b;
    }

    .book-checkbox {
        width: 18px;
        height: 18px;
        cursor: pointer;
        accent-color: #000000;
    }

    .book-checkbox:disabled {
        opacity: 0.5;
        cursor: not-allowed;
    }

    .unavailable-text {
        color: #6b7280;
        font-style: italic;
        font-size: 0.75rem;
    }

    .no-books {
        text-align: center;
        color: #6b7280;
        font-style: italic;
        padding: 2rem;
    }

    /* ==================== 대출 액션 ==================== */
    .borrow-actions {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1.5rem;
        background: #f9fafb;
        border-top: 1px solid #e5e7eb;
    }

    .selected-count {
        font-weight: 500;
        color: #374151;
    }

    .borrow-btn {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        padding: 0.75rem 1.5rem;
        background: #000000;
        color: white;
        border: none;
        border-radius: 6px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s;
        font-size: 0.875rem;
    }

    .borrow-btn:hover:not(:disabled) {
        background: #374151;
        transform: translateY(-1px);
    }

    .borrow-btn:disabled,
    .borrow-btn.disabled {
        background: #9ca3af;
        cursor: not-allowed;
        transform: none;
    }

    .btn-spinner {
        width: 16px;
        height: 16px;
        border: 2px solid rgba(255, 255, 255, 0.3);
        border-top: 2px solid white;
        border-radius: 50%;
        animation: spin 1s linear infinite;
    }

    /* ==================== 모달 스타일 ==================== */
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

    .confirm-modal {
        background: white;
        border-radius: 12px;
        width: 100%;
        max-width: 500px;
        box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
        border: 1px solid #e5e7eb;
    }

    .modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1.5rem;
        border-bottom: 1px solid #e5e7eb;
        background: #f9fafb;
    }

    .modal-header h3 {
        margin: 0;
        font-size: 1.25rem;
        font-weight: 600;
        color: #111827;
    }

    .close-btn {
        width: 32px;
        height: 32px;
        background: white;
        border: 1px solid #d1d5db;
        border-radius: 6px;
        cursor: pointer;
        color: #6b7280;
        font-size: 1.25rem;
        transition: all 0.2s;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .close-btn:hover {
        background: #f3f4f6;
        color: #374151;
        border-color: #9ca3af;
    }

    .modal-content {
        padding: 1.5rem;
    }

    .confirm-info {
        margin-bottom: 1.5rem;
    }

    .info-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 0.75rem;
        border-bottom: 1px solid #f3f4f6;
        background: #f9fafb;
        margin-bottom: 0.5rem;
        border-radius: 6px;
    }

    .info-row:last-child {
        border-bottom: none;
        margin-bottom: 0;
    }

    .label {
        font-weight: 500;
        color: #374151;
        min-width: 100px;
    }

    .value {
        color: #111827;
        text-align: right;
        font-weight: 500;
    }

    .delete-btn {
        background: #dc2626;
        color: white;
        border: none;
        border-radius: 4px;
        padding: 0.25rem 0.5rem;
        cursor: pointer;
        font-size: 0.875rem;
        transition: background-color 0.2s;
    }

    .delete-btn:hover {
        background: #b91c1c;
    }

    .loan-summary {
        background: #f9fafb;
        border: 1px solid #e5e7eb;
        border-radius: 8px;
        padding: 1rem;
        margin-bottom: 1.5rem;
    }

    .summary-row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 0.5rem;
        color: #374151;
    }

    .summary-row:last-child {
        margin-bottom: 0;
        font-weight: 600;
        color: #111827;
        border-top: 1px solid #e5e7eb;
        padding-top: 0.5rem;
    }

    .modal-actions {
        display: flex;
        justify-content: flex-end;
        gap: 1rem;
    }

    .cancel-btn,
    .confirm-btn {
        padding: 0.75rem 1.5rem;
        border-radius: 6px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s;
        border: 1px solid;
        font-size: 0.875rem;
        text-transform: uppercase;
    }

    .cancel-btn {
        background: white;
        color: #6b7280;
        border-color: #d1d5db;
    }

    .cancel-btn:hover {
        background: #f9fafb;
        border-color: #9ca3af;
    }

    .confirm-btn {
        background: #000000;
        color: white;
        border-color: #000000;
    }

    .confirm-btn:hover {
        background: #374151;
    }

    /* ==================== 반응형 디자인 ==================== */
    @media (max-width: 1024px) {
        .search-controls {
            flex-direction: column;
            gap: 1rem;
        }

        .search-bar {
            width: 100%;
        }

        .search-input {
            width: 100%;
        }
    }

    @media (max-width: 768px) {
        .books-table-container {
            overflow-x: auto;
        }

        .books-table {
            min-width: 600px;
        }

        .books-table th,
        .books-table td {
            padding: 0.5rem;
            font-size: 0.75rem;
        }

        .filter-controls {
            flex-direction: column;
        }

        .filter-select {
            width: 100%;
        }

        .borrow-actions {
            flex-direction: column;
            gap: 1rem;
            text-align: center;
        }

        .confirm-modal {
            margin: 1rem;
            max-width: none;
        }

        .modal-header,
        .modal-content {
            padding: 1rem;
        }

        .info-row {
            flex-direction: column;
            align-items: flex-start;
            gap: 0.5rem;
        }

        .value {
            text-align: left;
        }
    }

    @media (max-width: 480px) {
        .search-tabs {
            flex-direction: column;
            width: 100%;
        }

        .tab-btn {
            width: 100%;
        }

        .modal-actions {
            flex-direction: column;
        }

        .cancel-btn,
        .confirm-btn {
            width: 100%;
        }
    }
</style>
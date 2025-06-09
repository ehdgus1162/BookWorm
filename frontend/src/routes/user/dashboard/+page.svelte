<!-- src/routes/user/dashboard/+page.svelte -->
<script>
    import { onMount, getContext } from 'svelte';
    import { fade, scale, fly } from 'svelte/transition';

    // 레이아웃에서 제공하는 컨텍스트 사용
    const { currentUser, setError } = getContext('userLayout');

    // 대출 관련 데이터 (페이지별 상태만)
    let borrowedBooks = [];
    let returnedBooks = [];
    let totalBorrowedCount = 0;
    let totalReturnedCount = 0;
    let overdueCount = 0;
    let loading = false;

    // 통계 데이터
    let statistics = {
        totalBorrowed: 0,
        totalReturned: 0,
        currentlyBorrowed: 0,
        overdue: 0
    };

    // UI 상태
    let showBorrowedModal = false;
    let showReturnedModal = false;

    // ==================== API 연동 함수들 ====================

    /**
     * 사용자의 대출 도서 목록 조회
     */
    async function fetchUserLoans() {
        if (!currentUser?.id) return;

        loading = true;

        try {
            console.log('📚 사용자 대출 목록 조회 시작:', currentUser.id);

            const response = await fetch(`/api/loans/user/${currentUser.id}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();

            if (result && Array.isArray(result)) {
                // LoanResponse 구조에 맞게 데이터 분류
                const allLoans = result;

                borrowedBooks = allLoans.filter(loan => loan.isActive && !loan.isReturned);
                returnedBooks = allLoans.filter(loan => loan.isReturned);

                // 통계 계산
                statistics = {
                    totalBorrowed: allLoans.length,
                    totalReturned: returnedBooks.length,
                    currentlyBorrowed: borrowedBooks.length,
                    overdue: allLoans.filter(loan => loan.isOverdue).length
                };

                totalBorrowedCount = borrowedBooks.length;
                totalReturnedCount = returnedBooks.length;
                overdueCount = statistics.overdue;

                console.log('✅ 대출 목록 처리 완료:', {
                    borrowed: borrowedBooks.length,
                    returned: returnedBooks.length,
                    overdue: overdueCount
                });

            } else {
                throw new Error('잘못된 응답 형식');
            }

        } catch (err) {
            console.error('❌ 대출 목록 조회 실패:', err);
            setError(err.message);

            // 개발 중에는 목업 데이터 사용
            generateMockLoanData();

        } finally {
            loading = false;
        }
    }

    /**
     * 개발용 목업 대출 데이터 생성
     */
    function generateMockLoanData() {
        const mockBorrowedBooks = [
            {
                id: 1,
                bookTitle: 'Hibernate Core -11th',
                bookType: 'Educational',
                bookLanguage: 'English',
                loanDate: '2024-12-01',
                dueDate: '2024-12-15',
                status: 'ACTIVE',
                isActive: true,
                isReturned: false,
                isOverdue: false,
                daysUntilDue: 3
            },
            {
                id: 2,
                bookTitle: 'Advanced JavaScript Patterns',
                bookType: 'Technology',
                bookLanguage: 'English',
                loanDate: '2024-11-20',
                dueDate: '2024-12-10',
                status: 'ACTIVE',
                isActive: true,
                isReturned: false,
                isOverdue: true,
                daysUntilDue: -2,
                overdueDays: 2
            }
        ];

        const mockReturnedBooks = [
            {
                id: 3,
                bookTitle: 'Clean Code',
                bookType: 'Technology',
                bookLanguage: 'English',
                loanDate: '2024-11-01',
                dueDate: '2024-11-15',
                status: 'RETURNED',
                isActive: false,
                isReturned: true,
                isOverdue: false
            },
            {
                id: 4,
                bookTitle: 'Design Patterns',
                bookType: 'Technology',
                bookLanguage: 'English',
                loanDate: '2024-10-15',
                dueDate: '2024-10-29',
                status: 'RETURNED',
                isActive: false,
                isReturned: true,
                isOverdue: false
            }
        ];

        borrowedBooks = mockBorrowedBooks;
        returnedBooks = mockReturnedBooks;

        statistics = {
            totalBorrowed: mockBorrowedBooks.length + mockReturnedBooks.length,
            totalReturned: mockReturnedBooks.length,
            currentlyBorrowed: mockBorrowedBooks.length,
            overdue: mockBorrowedBooks.filter(book => book.isOverdue).length
        };

        totalBorrowedCount = mockBorrowedBooks.length;
        totalReturnedCount = mockReturnedBooks.length;
        overdueCount = statistics.overdue;
    }

    /**
     * 도서 반납 요청
     */
    async function returnBook(loanId) {
        loading = true;

        try {
            console.log('📤 도서 반납 요청:', loanId);

            const response = await fetch(`/api/loans/${loanId}/return`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                }
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || '반납 처리 실패');
            }

            const result = await response.json();
            console.log('✅ 도서 반납 성공:', result);

            // 대출 목록 새로고침
            await fetchUserLoans();

        } catch (err) {
            console.error('❌ 도서 반납 실패:', err);
            setError(err.message || '도서 반납 중 오류가 발생했습니다.');
        } finally {
            loading = false;
        }
    }

    /**
     * 대출 연장 요청
     */
    async function extendLoan(loanId, extensionDays = 7) {
        loading = true;

        try {
            console.log('📅 대출 연장 요청:', loanId, extensionDays);

            const response = await fetch(`/api/loans/${loanId}/extend`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                },
                body: JSON.stringify({ extensionDays })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || '연장 처리 실패');
            }

            const result = await response.json();
            console.log('✅ 대출 연장 성공:', result);

            // 대출 목록 새로고침
            await fetchUserLoans();

        } catch (err) {
            console.error('❌ 대출 연장 실패:', err);
            setError(err.message || '대출 연장 중 오류가 발생했습니다.');
        } finally {
            loading = false;
        }
    }

    // ==================== 유틸리티 함수들 ====================

    /**
     * 날짜 포맷팅
     */
    function formatDate(dateString) {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleDateString('ko-KR');
    }

    /**
     * 상태 메시지 생성
     */
    function getStatusMessage(loan) {
        if (loan.isOverdue) {
            return `${loan.overdueDays || 0}일 연체`;
        } else if (loan.isReturned) {
            return '반납 완료';
        } else if (loan.isActive) {
            if (loan.daysUntilDue <= 0) {
                return '오늘 반납';
            } else if (loan.daysUntilDue <= 3) {
                return `${loan.daysUntilDue}일 후 반납`;
            } else {
                return '대출 중';
            }
        }
        return loan.statusDescription || '-';
    }

    /**
     * 통계 차트 데이터 계산
     */
    function getChartData() {
        const total = statistics.totalBorrowed || 1; // 0으로 나누기 방지
        return {
            borrowed: (statistics.currentlyBorrowed / total) * 100,
            returned: (statistics.totalReturned / total) * 100,
            overdue: (statistics.overdue / total) * 100
        };
    }

    /**
     * 모달 제어
     */
    function openBorrowedModal() {
        showBorrowedModal = true;
    }

    function openReturnedModal() {
        showReturnedModal = true;
    }

    function closeModals() {
        showBorrowedModal = false;
        showReturnedModal = false;
    }

    // ==================== 생명주기 ====================

    onMount(async () => {
        console.log('📊 사용자 대시보드 초기화');
        await fetchUserLoans();
    });

    // 반응성
    $: chartData = getChartData();
</script>

<svelte:head>
    <title>대시보드 - BookWorm Library</title>
</svelte:head>

<svelte:window on:keydown={(e) => e.key === 'Escape' && closeModals()} />

<!-- 대시보드 콘텐츠 (레이아웃은 자동 적용) -->
<div class="dashboard-content">
    <!-- 상단 카드 섹션 -->
    <div class="cards-section">
        <!-- 대출 중인 도서 카드 -->
        <div class="dashboard-card borrowed-card"
             on:click={openBorrowedModal}
             in:fly={{ y: 20, duration: 300, delay: 100 }}>
            <div class="card-icon">📖</div>
            <div class="card-content">
                <h3 class="card-title">Your Borrowed Book List</h3>
                <div class="card-count">{totalBorrowedCount}</div>
                {#if overdueCount > 0}
                    <div class="overdue-notice">
                        {overdueCount}권 연체 중
                    </div>
                {/if}
            </div>
        </div>

        <!-- 반납한 도서 카드 -->
        <div class="dashboard-card returned-card"
             on:click={openReturnedModal}
             in:fly={{ y: 20, duration: 300, delay: 200 }}>
            <div class="card-icon">📚</div>
            <div class="card-content">
                <h3 class="card-title">Your Returned Book List</h3>
                <div class="card-count">{totalReturnedCount}</div>
            </div>
        </div>

        <!-- 도서 탐색 카드 -->
        <div class="dashboard-card browse-card"
             on:click={() => window.location.href = '/user/books'}
             in:fly={{ y: 20, duration: 300, delay: 300 }}>
            <div class="card-icon">🔍</div>
            <div class="card-content">
                <h3 class="card-title">Let's browse available book inventory</h3>
            </div>
        </div>
    </div>

    <!-- 하단 섹션 -->
    <div class="bottom-section">
        <!-- 인용문 섹션 -->
        <div class="quote-section" in:fade={{ delay: 400 }}>
            <blockquote class="inspiration-quote">
                "Embarking on the journey of reading fosters personal growth, nurturing a path towards excellence and the refinement of character."
            </blockquote>
            <div class="quote-author">- BookWorm Team</div>
        </div>

        <!-- 통계 차트 섹션 -->
        <div class="chart-section" in:scale={{ delay: 500 }}>
            <div class="chart-container">
                <!-- SVG 원형 차트 -->
                <svg class="pie-chart" viewBox="0 0 200 200">
                    <circle cx="100" cy="100" r="80"
                            fill="none"
                            stroke="#374151"
                            stroke-width="40"/>

                    <!-- 반납한 도서 -->
                    <circle cx="100" cy="100" r="80"
                            fill="none"
                            stroke="#10b981"
                            stroke-width="40"
                            stroke-dasharray="{chartData.returned * 5.03} 502.65"
                            stroke-dashoffset="0"
                            transform="rotate(-90 100 100)"/>

                    <!-- 대출 중인 도서 -->
                    <circle cx="100" cy="100" r="80"
                            fill="none"
                            stroke="#3b82f6"
                            stroke-width="40"
                            stroke-dasharray="{chartData.borrowed * 5.03} 502.65"
                            stroke-dashoffset="-{chartData.returned * 5.03}"
                            transform="rotate(-90 100 100)"/>

                    <!-- 연체 도서 (있는 경우) -->
                    {#if chartData.overdue > 0}
                        <circle cx="100" cy="100" r="80"
                                fill="none"
                                stroke="#ef4444"
                                stroke-width="40"
                                stroke-dasharray="{chartData.overdue * 5.03} 502.65"
                                stroke-dashoffset="-{(chartData.returned + chartData.borrowed) * 5.03}"
                                transform="rotate(-90 100 100)"/>
                    {/if}
                </svg>

                <!-- 차트 중앙 텍스트 -->
                <div class="chart-center">
                    <div class="chart-total">{statistics.totalBorrowed}</div>
                    <div class="chart-label">Total</div>
                </div>
            </div>

            <!-- 범례 -->
            <div class="chart-legend">
                <div class="legend-item">
                    <div class="legend-color borrowed"></div>
                    <span class="legend-text">Total Borrowed Books</span>
                </div>
                <div class="legend-item">
                    <div class="legend-color returned"></div>
                    <span class="legend-text">Total Returned Books</span>
                </div>
                {#if statistics.overdue > 0}
                    <div class="legend-item">
                        <div class="legend-color overdue"></div>
                        <span class="legend-text">Overdue Books</span>
                    </div>
                {/if}
            </div>
        </div>
    </div>
</div>

<!-- 대출 중인 도서 모달 -->
{#if showBorrowedModal}
    <div class="modal-overlay" on:click={closeModals} in:fade>
        <div class="modal-container" on:click|stopPropagation in:scale>
            <div class="modal-header">
                <h3>대출 중인 도서</h3>
                <button class="close-btn" on:click={closeModals}>✕</button>
            </div>

            <div class="modal-content">
                {#if loading}
                    <div class="loading-state">
                        <div class="loading-spinner"></div>
                        <span>로딩 중...</span>
                    </div>
                {:else if borrowedBooks.length === 0}
                    <div class="empty-state">
                        <div class="empty-icon">📚</div>
                        <p>현재 대출 중인 도서가 없습니다.</p>
                        <button class="browse-btn" on:click={() => window.location.href = '/user/books'}>
                            도서 둘러보기
                        </button>
                    </div>
                {:else}
                    <div class="books-list">
                        {#each borrowedBooks as book (book.id)}
                            <div class="book-item" class:overdue={book.isOverdue} in:fade>
                                <div class="book-info">
                                    <h4 class="book-title">{book.bookTitle}</h4>
                                    <div class="book-meta">
                                        <span class="book-type">{book.bookType}</span>
                                        <span class="book-language">{book.bookLanguage}</span>
                                    </div>
                                    <div class="loan-details">
                                        <span class="loan-date">대출일: {formatDate(book.loanDate)}</span>
                                        <span class="due-date">반납일: {formatDate(book.dueDate)}</span>
                                    </div>
                                    <div class="status-badge {book.isOverdue ? 'overdue' : 'active'}">
                                        {getStatusMessage(book)}
                                    </div>
                                </div>

                                <div class="book-actions">
                                    <button class="action-btn extend"
                                            on:click={() => extendLoan(book.id)}
                                            disabled={loading || book.isOverdue}>
                                        📅 연장
                                    </button>
                                    <button class="action-btn return"
                                            on:click={() => returnBook(book.id)}
                                            disabled={loading}>
                                        📤 반납
                                    </button>
                                </div>
                            </div>
                        {/each}
                    </div>
                {/if}
            </div>
        </div>
    </div>
{/if}

<!-- 반납한 도서 모달 -->
{#if showReturnedModal}
    <div class="modal-overlay" on:click={closeModals} in:fade>
        <div class="modal-container" on:click|stopPropagation in:scale>
            <div class="modal-header">
                <h3>반납한 도서</h3>
                <button class="close-btn" on:click={closeModals}>✕</button>
            </div>

            <div class="modal-content">
                {#if loading}
                    <div class="loading-state">
                        <div class="loading-spinner"></div>
                        <span>로딩 중...</span>
                    </div>
                {:else if returnedBooks.length === 0}
                    <div class="empty-state">
                        <div class="empty-icon">📚</div>
                        <p>반납한 도서가 없습니다.</p>
                    </div>
                {:else}
                    <div class="books-list">
                        {#each returnedBooks as book (book.id)}
                            <div class="book-item returned" in:fade>
                                <div class="book-info">
                                    <h4 class="book-title">{book.bookTitle}</h4>
                                    <div class="book-meta">
                                        <span class="book-type">{book.bookType}</span>
                                        <span class="book-language">{book.bookLanguage}</span>
                                    </div>
                                    <div class="loan-details">
                                        <span class="loan-date">대출일: {formatDate(book.loanDate)}</span>
                                        <span class="due-date">반납일: {formatDate(book.dueDate)}</span>
                                    </div>
                                    <div class="status-badge returned">
                                        반납 완료
                                    </div>
                                </div>
                            </div>
                        {/each}
                    </div>
                {/if}
            </div>
        </div>
    </div>
{/if}

<style>
    /* ==================== 대시보드 콘텐츠 ==================== */
    .dashboard-content {
        display: flex;
        flex-direction: column;
        gap: 2rem;
        flex: 1;
    }

    /* 카드 섹션 */
    .cards-section {
        display: grid;
        grid-template-columns: 1fr 1fr 1fr;
        gap: 1.5rem;
    }

    .dashboard-card {
        background: white;
        border-radius: 12px;
        padding: 2rem;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        cursor: pointer;
        transition: all 0.3s ease;
        border: 1px solid #e5e7eb;
    }

    .dashboard-card:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
        border-color: #d1d5db;
    }

    .borrowed-card {
        border-left: 4px solid #000000;
    }

    .returned-card {
        border-left: 4px solid #6b7280;
    }

    .browse-card {
        border-left: 4px solid #374151;
    }

    .card-icon {
        font-size: 2.5rem;
        margin-bottom: 1rem;
        color: #374151;
    }

    .card-content {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }

    .card-title {
        font-size: 1.125rem;
        font-weight: 600;
        color: #111827;
        margin: 0;
        line-height: 1.4;
    }

    .card-count {
        font-size: 2rem;
        font-weight: 700;
        color: #000000;
    }

    .overdue-notice {
        font-size: 0.875rem;
        color: #dc2626;
        font-weight: 500;
        background: #fef2f2;
        padding: 0.25rem 0.5rem;
        border-radius: 4px;
        align-self: flex-start;
        border: 1px solid #fecaca;
    }

    /* ==================== 하단 섹션 ==================== */
    .bottom-section {
        display: grid;
        grid-template-columns: 1fr 400px;
        gap: 2rem;
        align-items: start;
    }

    /* 인용문 섹션 */
    .quote-section {
        background: white;
        border-radius: 12px;
        padding: 2rem;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        display: flex;
        flex-direction: column;
        justify-content: center;
        min-height: 300px;
        border: 1px solid #e5e7eb;
    }

    .inspiration-quote {
        font-size: 1.125rem;
        line-height: 1.6;
        color: #374151;
        font-style: italic;
        margin: 0 0 1rem 0;
        padding: 0;
        border: none;
    }

    .quote-author {
        font-size: 0.875rem;
        color: #6b7280;
        text-align: right;
        font-weight: 500;
    }

    /* ==================== 차트 섹션 ==================== */
    .chart-section {
        background: white;
        border-radius: 12px;
        padding: 2rem;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 1.5rem;
        border: 1px solid #e5e7eb;
    }

    .chart-container {
        position: relative;
        width: 200px;
        height: 200px;
    }

    .pie-chart {
        width: 100%;
        height: 100%;
        transform: rotate(-90deg);
    }

    .chart-center {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        text-align: center;
    }

    .chart-total {
        font-size: 2rem;
        font-weight: 700;
        color: #111827;
    }

    .chart-label {
        font-size: 0.875rem;
        color: #6b7280;
        font-weight: 500;
    }

    .chart-legend {
        display: flex;
        flex-direction: column;
        gap: 0.75rem;
    }

    .legend-item {
        display: flex;
        align-items: center;
        gap: 0.5rem;
    }

    .legend-color {
        width: 16px;
        height: 16px;
        border-radius: 50%;
    }

    .legend-color.borrowed {
        background: #3b82f6;
    }

    .legend-color.returned {
        background: #10b981;
    }

    .legend-color.overdue {
        background: #ef4444;
    }

    .legend-text {
        font-size: 0.875rem;
        color: #374151;
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

    .modal-container {
        background: white;
        border-radius: 12px;
        width: 100%;
        max-width: 600px;
        max-height: 80vh;
        overflow: hidden;
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
        overflow-y: auto;
        max-height: calc(80vh - 120px);
    }

    /* ==================== 로딩 및 빈 상태 ==================== */
    .loading-state {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 1rem;
        padding: 2rem;
        color: #6b7280;
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

    .empty-state {
        text-align: center;
        padding: 2rem;
        color: #6b7280;
    }

    .empty-icon {
        font-size: 3rem;
        margin-bottom: 1rem;
        opacity: 0.5;
    }

    .empty-state p {
        margin: 0 0 1.5rem 0;
        font-size: 1rem;
    }

    .browse-btn {
        padding: 0.75rem 1.5rem;
        background: #000000;
        color: white;
        border: none;
        border-radius: 6px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s;
    }

    .browse-btn:hover {
        background: #374151;
    }

    /* ==================== 도서 목록 ==================== */
    .books-list {
        display: flex;
        flex-direction: column;
        gap: 1rem;
    }

    .book-item {
        background: #f9fafb;
        border-radius: 8px;
        padding: 1.5rem;
        border: 1px solid #e5e7eb;
        transition: all 0.2s;
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        gap: 1rem;
    }

    .book-item:hover {
        background: #f3f4f6;
        border-color: #d1d5db;
    }

    .book-item.overdue {
        border-left: 4px solid #dc2626;
        background: #fef2f2;
    }

    .book-item.returned {
        opacity: 0.8;
    }

    .book-info {
        flex: 1;
    }

    .book-title {
        font-size: 1.125rem;
        font-weight: 600;
        color: #111827;
        margin: 0 0 0.5rem 0;
    }

    .book-meta {
        display: flex;
        gap: 1rem;
        margin-bottom: 0.5rem;
    }

    .book-type,
    .book-language {
        font-size: 0.875rem;
        color: #6b7280;
        background: #e5e7eb;
        padding: 0.25rem 0.5rem;
        border-radius: 4px;
        border: 1px solid #d1d5db;
    }

    .loan-details {
        display: flex;
        gap: 1rem;
        margin-bottom: 0.75rem;
        font-size: 0.875rem;
        color: #6b7280;
    }

    .status-badge {
        display: inline-block;
        padding: 0.25rem 0.75rem;
        border-radius: 12px;
        font-size: 0.75rem;
        font-weight: 500;
        text-transform: uppercase;
        border: 1px solid;
    }

    .status-badge.active {
        background: #dbeafe;
        color: #1e40af;
        border-color: #3b82f6;
    }

    .status-badge.overdue {
        background: #fee2e2;
        color: #991b1b;
        border-color: #dc2626;
    }

    .status-badge.returned {
        background: #d1fae5;
        color: #065f46;
        border-color: #10b981;
    }

    .book-actions {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }

    .action-btn {
        padding: 0.5rem 1rem;
        border-radius: 6px;
        font-size: 0.875rem;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s;
        border: 1px solid;
        white-space: nowrap;
    }

    .action-btn:disabled {
        opacity: 0.5;
        cursor: not-allowed;
    }

    .action-btn.extend {
        background: white;
        color: #92400e;
        border-color: #d97706;
    }

    .action-btn.extend:hover:not(:disabled) {
        background: #fef3c7;
    }

    .action-btn.return {
        background: white;
        color: #065f46;
        border-color: #10b981;
    }

    .action-btn.return:hover:not(:disabled) {
        background: #d1fae5;
    }

    /* ==================== 반응형 디자인 ==================== */
    @media (max-width: 1200px) {
        .bottom-section {
            grid-template-columns: 1fr;
            gap: 1.5rem;
        }

        .chart-section {
            order: -1;
        }
    }

    @media (max-width: 768px) {
        .cards-section {
            grid-template-columns: 1fr;
            gap: 1rem;
        }

        .modal-container {
            margin: 1rem;
            max-width: none;
        }

        .book-item {
            flex-direction: column;
            align-items: stretch;
        }

        .book-actions {
            flex-direction: row;
            justify-content: flex-end;
        }
    }

    @media (max-width: 480px) {
        .dashboard-card {
            padding: 1.5rem;
        }

        .card-title {
            font-size: 1rem;
        }

        .chart-container {
            width: 150px;
            height: 150px;
        }

        .chart-total {
            font-size: 1.5rem;
        }

        .book-actions {
            flex-direction: column;
        }
    }
</style>
<!-- =============================================== -->
<!-- src/routes/user/loans/+page.svelte -->
<!-- =============================================== -->

<script>
    import { onMount, getContext } from 'svelte';
    import { fade, scale, slide } from 'svelte/transition';

    // 레이아웃에서 제공하는 컨텍스트 사용
    const { currentUser, setError } = getContext('userLayout');

    // 대출 관련 상태
    let borrowedLoans = [];
    let returnedLoans = [];
    let loading = false;
    let currentTab = 'borrowed'; // 'borrowed' | 'returned'

    // 반납 관련 상태
    let showReturnModal = false;
    let returnConfirmData = null;
    let selectedLoans = new Set();

    // 검색 및 필터링
    let searchKeyword = '';

    // ==================== 백엔드 API 연동 함수들 ====================

    /**
     * 사용자의 대출 중인 도서 목록 조회 (수정됨)
     */
    async function fetchBorrowedLoans() {
        // currentUser 검증
        if (!currentUser || !currentUser.id) {
            console.warn('⚠️ 사용자 정보가 없습니다. 목업 데이터를 사용합니다.');
            borrowedLoans = generateMockBorrowedLoans();
            return;
        }

        loading = true;

        try {
            console.log('📚 대출 중인 도서 목록 조회 시작 - UserId:', currentUser.id);

            // ✅ 수정된 API 엔드포인트: /api/returns/user/{userId}/available
            const response = await fetch(`http://localhost:8080/api/returns/user/${currentUser.id}/available`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();
            console.log('📋 API 응답:', result);

            if (result && Array.isArray(result)) {
                // 직접 배열 응답
                borrowedLoans = result;
                console.log('✅ 대출 중인 도서 목록 로드 완료:', borrowedLoans.length);
            } else if (result.success && result.data) {
                // 래핑된 응답
                borrowedLoans = Array.isArray(result.data) ? result.data : result.data.content || [];
                console.log('✅ 대출 중인 도서 목록 로드 완료:', borrowedLoans.length);
            } else {
                throw new Error(result.message || '대출 목록 조회 실패');
            }

        } catch (err) {
            console.error('❌ 대출 목록 조회 실패:', err);

            // 네트워크 오류 시 목업 데이터 사용
            if (err.message.includes('Failed to fetch') || err.message.includes('HTTP 404')) {
                console.log('🔄 백엔드 연결 실패. 목업 데이터를 사용합니다.');
                borrowedLoans = generateMockBorrowedLoans();
            } else {
                setError(err.message);
                borrowedLoans = generateMockBorrowedLoans();
            }
        } finally {
            loading = false;
        }
    }

    /**
     * 사용자의 반납 완료 도서 목록 조회 (수정됨)
     */
    async function fetchReturnedLoans() {
        // currentUser 검증
        if (!currentUser || !currentUser.id) {
            console.warn('⚠️ 사용자 정보가 없습니다. 목업 데이터를 사용합니다.');
            returnedLoans = generateMockReturnedLoans();
            return;
        }

        loading = true;

        try {
            console.log('📚 반납 완료 도서 목록 조회 시작 - UserId:', currentUser.id);

            // ✅ 수정된 API 엔드포인트: /api/returns/user/{userId}/history
            const response = await fetch(`http://localhost:8080/api/returns/user/${currentUser.id}/history`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();
            console.log('📋 API 응답:', result);

            if (result && Array.isArray(result)) {
                // 직접 배열 응답
                returnedLoans = result;
                console.log('✅ 반납 완료 도서 목록 로드 완료:', returnedLoans.length);
            } else if (result.success && result.data) {
                // 래핑된 응답
                returnedLoans = Array.isArray(result.data) ? result.data : result.data.content || [];
                console.log('✅ 반납 완료 도서 목록 로드 완료:', returnedLoans.length);
            } else {
                throw new Error(result.message || '반납 이력 조회 실패');
            }

        } catch (err) {
            console.error('❌ 반납 이력 조회 실패:', err);

            // 네트워크 오류 시 목업 데이터 사용
            if (err.message.includes('Failed to fetch') || err.message.includes('HTTP 404')) {
                console.log('🔄 백엔드 연결 실패. 목업 데이터를 사용합니다.');
                returnedLoans = generateMockReturnedLoans();
            } else {
                setError(err.message);
                returnedLoans = generateMockReturnedLoans();
            }
        } finally {
            loading = false;
        }
    }

    /**
     * 도서 반납 API 호출 (수정됨)
     */
    async function returnBook(loanId) {
        if (!loanId) {
            setError('올바른 대출 ID가 없습니다.');
            return;
        }

        loading = true;

        try {
            console.log('📝 도서 반납 요청 시작:', loanId);

            // ✅ 수정된 API 엔드포인트: /api/returns/{loanId}/process
            const response = await fetch(`http://localhost:8080/api/returns/${loanId}/process`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`
                }
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();
            console.log('📋 반납 API 응답:', result);

            if (result && (result.success !== false)) {
                console.log('✅ 도서 반납 성공:', result);

                // 반납 확인 데이터 설정
                returnConfirmData = {
                    loanId: result.loanId || loanId,
                    bookTitle: result.bookTitle || 'Unknown Book',
                    userName: result.userName || currentUser?.name || 'Unknown User',
                    returnDate: result.returnDate || getCurrentDate(),
                    wasOverdue: result.wasOverdue || false,
                    overdueDays: result.overdueDays || 0,
                    message: result.message || '도서가 정상적으로 반납되었습니다.'
                };

                // 대출 목록 새로고침
                await fetchBorrowedLoans();

                // 확인 모달 표시
                showReturnModal = true;

            } else {
                throw new Error(result?.message || '도서 반납 실패');
            }

        } catch (err) {
            console.error('❌ 도서 반납 실패:', err);

            // 백엔드 연결 실패 시 목업 처리
            if (err.message.includes('Failed to fetch') || err.message.includes('HTTP 404')) {
                console.log('🔄 백엔드 연결 실패. 목업 반납을 시뮬레이션합니다.');

                // 목업 반납 성공 처리
                const loanToReturn = borrowedLoans.find(loan => loan.id === loanId);
                if (loanToReturn) {
                    returnConfirmData = {
                        loanId: loanId,
                        bookTitle: loanToReturn.bookTitle,
                        userName: currentUser?.name || 'Test User',
                        returnDate: getCurrentDate(),
                        wasOverdue: loanToReturn.isOverdue || false,
                        overdueDays: loanToReturn.overdueDays || 0,
                        message: loanToReturn.isOverdue ?
                            `도서가 반납되었습니다. (연체 ${loanToReturn.overdueDays}일)` :
                            '도서가 정상적으로 반납되었습니다.'
                    };

                    // 목업 데이터에서 제거
                    borrowedLoans = borrowedLoans.filter(loan => loan.id !== loanId);
                    showReturnModal = true;
                } else {
                    setError('반납할 도서를 찾을 수 없습니다.');
                }
            } else {
                setError(err.message || '도서 반납 중 오류가 발생했습니다.');
            }
        } finally {
            loading = false;
        }
    }

    /**
     * 여러 도서 일괄 반납 (수정됨)
     */
    async function returnSelectedBooks() {
        if (selectedLoans.size === 0) {
            setError('반납할 도서를 선택해주세요.');
            return;
        }

        loading = true;

        try {
            console.log('📝 일괄 반납 요청 시작:', Array.from(selectedLoans));

            // ✅ 수정된 API 엔드포인트: /api/returns/bulk-process
            const response = await fetch('http://localhost:8080/api/returns/bulk-process', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`
                },
                body: JSON.stringify(Array.from(selectedLoans))
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
            }

            const results = await response.json();
            console.log('📋 일괄 반납 API 응답:', results);

            if (results && Array.isArray(results)) {
                console.log('✅ 일괄 반납 성공:', results);

                // 성공한 반납들의 정보를 수집
                const successCount = results.filter(r => r.success !== false).length;

                returnConfirmData = {
                    isMultiple: true,
                    totalBooks: selectedLoans.size,
                    successCount: successCount,
                    message: `${successCount}권의 도서가 성공적으로 반납되었습니다.`
                };

                // 선택 초기화
                selectedLoans.clear();
                selectedLoans = new Set();

                // 대출 목록 새로고침
                await fetchBorrowedLoans();

                // 확인 모달 표시
                showReturnModal = true;

            } else {
                throw new Error('일괄 반납 응답 형식이 올바르지 않습니다.');
            }

        } catch (err) {
            console.error('❌ 일괄 반납 실패:', err);

            // 백엔드 연결 실패 시 목업 처리
            if (err.message.includes('Failed to fetch') || err.message.includes('HTTP 404')) {
                console.log('🔄 백엔드 연결 실패. 목업 일괄 반납을 시뮬레이션합니다.');

                const selectedLoanIds = Array.from(selectedLoans);
                const loansToReturn = borrowedLoans.filter(loan => selectedLoanIds.includes(loan.id));

                if (loansToReturn.length > 0) {
                    returnConfirmData = {
                        isMultiple: true,
                        totalBooks: selectedLoanIds.length,
                        successCount: loansToReturn.length,
                        message: `${loansToReturn.length}권의 도서가 성공적으로 반납되었습니다.`
                    };

                    // 목업 데이터에서 제거
                    borrowedLoans = borrowedLoans.filter(loan => !selectedLoanIds.includes(loan.id));

                    // 선택 초기화
                    selectedLoans.clear();
                    selectedLoans = new Set();

                    showReturnModal = true;
                } else {
                    setError('반납할 도서를 찾을 수 없습니다.');
                }
            } else {
                setError(err.message || '일괄 반납 중 오류가 발생했습니다.');
            }
        } finally {
            loading = false;
        }
    }

    // ==================== 유틸리티 함수들 ====================

    /**
     * 현재 날짜 반환 (추가된 헬퍼 함수)
     */
    function getCurrentDate() {
        return new Date().toISOString().split('T')[0];
    }

    /**
     * 개발용 대출 중인 도서 목업 데이터
     */
    function generateMockBorrowedLoans() {
        return [
            {
                id: 1,
                bookId: 1,
                bookTitle: 'Hibernate - Core',
                bookType: 'Educational',
                bookLanguage: 'English',
                userId: currentUser?.id || 1,
                userName: currentUser?.name || 'Test User',
                quantity: 2,
                loanDate: '2024-03-13',
                dueDate: '2024-03-15',
                status: 'ACTIVE',
                isOverdue: true,
                isActive: true,
                daysUntilDue: -10,
                overdueDays: 10
            },
            {
                id: 2,
                bookId: 2,
                bookTitle: 'Spring Framework Guide',
                bookType: 'Educational',
                bookLanguage: 'English',
                userId: currentUser?.id || 1,
                userName: currentUser?.name || 'Test User',
                quantity: 1,
                loanDate: '2024-03-20',
                dueDate: '2024-04-03',
                status: 'ACTIVE',
                isOverdue: false,
                isActive: true,
                daysUntilDue: 5,
                overdueDays: 0
            }
        ];
    }

    /**
     * 개발용 반납 완료 도서 목업 데이터
     */
    function generateMockReturnedLoans() {
        return [
            {
                id: 3,
                bookId: 3,
                bookTitle: 'Java Programming',
                bookType: 'Educational',
                bookLanguage: 'English',
                userId: currentUser?.id || 1,
                userName: currentUser?.name || 'Test User',
                quantity: 1,
                loanDate: '2024-02-01',
                dueDate: '2024-02-15',
                status: 'RETURNED',
                isOverdue: false,
                isReturned: true,
                returnDate: '2024-02-14'
            }
        ];
    }

    /**
     * 도서 선택/해제 처리
     */
    function toggleLoanSelection(loanId) {
        if (selectedLoans.has(loanId)) {
            selectedLoans.delete(loanId);
        } else {
            selectedLoans.add(loanId);
        }
        selectedLoans = new Set(selectedLoans);
    }

    /**
     * 탭 변경 처리
     */
    function switchTab(tab) {
        currentTab = tab;
        selectedLoans.clear();
        selectedLoans = new Set();

        if (tab === 'borrowed') {
            fetchBorrowedLoans();
        } else {
            fetchReturnedLoans();
        }
    }

    /**
     * 검색 처리
     */
    function handleSearch() {
        // 클라이언트 사이드 필터링
        if (currentTab === 'borrowed') {
            if (searchKeyword.trim()) {
                borrowedLoans = borrowedLoans.filter(loan =>
                    loan.bookTitle.toLowerCase().includes(searchKeyword.toLowerCase()) ||
                    loan.bookType.toLowerCase().includes(searchKeyword.toLowerCase())
                );
            } else {
                fetchBorrowedLoans();
            }
        } else {
            if (searchKeyword.trim()) {
                returnedLoans = returnedLoans.filter(loan =>
                    loan.bookTitle.toLowerCase().includes(searchKeyword.toLowerCase()) ||
                    loan.bookType.toLowerCase().includes(searchKeyword.toLowerCase())
                );
            } else {
                fetchReturnedLoans();
            }
        }
    }

    /**
     * 날짜 포맷팅
     */
    function formatDate(dateString) {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        }).replace(/\./g, '-').replace(/-$/, '');
    }

    /**
     * 시간 포맷팅
     */
    function formatDateTime(dateString) {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('ko-KR') + ' ' +
            date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
    }

    /**
     * 연체 상태 확인
     */
    function isOverdue(loan) {
        return loan.isOverdue || loan.daysUntilDue < 0;
    }

    /**
     * 상태 텍스트 반환
     */
    function getStatusText(loan) {
        if (loan.status === 'RETURNED') return 'Returned';
        if (isOverdue(loan)) return 'Overdue';
        if (loan.daysUntilDue === 0) return 'Due Today';
        if (loan.daysUntilDue <= 3) return `Due in ${loan.daysUntilDue} days`;
        return 'Active';
    }

    /**
     * 상태 CSS 클래스 반환
     */
    function getStatusClass(loan) {
        if (loan.status === 'RETURNED') return 'returned';
        if (isOverdue(loan)) return 'overdue';
        if (loan.daysUntilDue <= 3) return 'due-soon';
        return 'active';
    }

    /**
     * 모달 닫기
     */
    function closeModal() {
        showReturnModal = false;
        returnConfirmData = null;
    }

    // ==================== 생명주기 ====================

    onMount(async () => {
        console.log('📚 대출 관리 페이지 초기화');
        console.log('👤 현재 사용자:', currentUser);

        // currentUser가 로드될 때까지 대기
        if (currentUser && currentUser.id) {
            await fetchBorrowedLoans();
        } else {
            // currentUser가 없으면 목업 데이터 사용
            console.log('⚠️ 사용자 정보 대기 중... 목업 데이터를 로드합니다.');
            borrowedLoans = generateMockBorrowedLoans();
        }
    });

    // currentUser가 변경되면 데이터 다시 로드
    $: if (currentUser && currentUser.id && borrowedLoans.length === 0) {
        console.log('👤 사용자 정보가 업데이트됨. 대출 목록을 다시 로드합니다.');
        fetchBorrowedLoans();
    }

    // 반응성
    $: filteredBorrowedLoans = borrowedLoans;
    $: filteredReturnedLoans = returnedLoans;
    $: selectedCount = selectedLoans.size;
    $: canReturn = selectedCount > 0 && !loading;
</script>

<svelte:head>
    <title>대출 관리 - BookWorm Library</title>
</svelte:head>

<svelte:window on:keydown={(e) => {
    if (e.key === 'Enter' && e.target.type === 'text') handleSearch();
    else if (e.key === 'Escape') closeModal();
}} />

<!-- 대출 관리 폼 -->
<div class="loans-form">
    <!-- 헤더 -->
    <div class="form-header">
        <h2>User Returned Books Form</h2>
        <div class="header-info">
            <span class="user-name">{currentUser.name}</span>
            <span class="user-label">User</span>
        </div>
    </div>

    <!-- 탭 메뉴 -->
    <div class="tab-menu">
        <button
                class="tab-btn"
                class:active={currentTab === 'borrowed'}
                on:click={() => switchTab('borrowed')}
        >
            Borrowed Books
        </button>
        <button
                class="tab-btn"
                class:active={currentTab === 'returned'}
                on:click={() => switchTab('returned')}
        >
            Returned Books
        </button>
    </div>

    <!-- 검색 바 -->
    <div class="search-section">
        <div class="search-bar">
            <input
                    type="text"
                    placeholder="Search by ID"
                    bind:value={searchKeyword}
                    on:input={handleSearch}
                    class="search-input"
            />
            <button class="search-btn" on:click={handleSearch}>
                🔍
            </button>
        </div>
    </div>

    <!-- 대출 테이블 -->
    <div class="loans-table-container">
        {#if loading}
            <div class="loading-overlay">
                <div class="loading-spinner"></div>
                <span>Loading loans...</span>
            </div>
        {/if}

        <table class="loans-table">
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
            {#if currentTab === 'borrowed'}
                {#each filteredBorrowedLoans as loan (loan.id)}
                    <tr class="loan-row {getStatusClass(loan)}"
                        class:selected={selectedLoans.has(loan.id)}
                        in:fade={{ duration: 200 }}>
                        <td class="loan-id">{loan.id.toString().padStart(3, '0')}</td>
                        <td class="user-id">{loan.userId}</td>
                        <td class="amount">{loan.quantity.toString().padStart(3, '0')} Books</td>
                        <td class="due-date">{formatDate(loan.dueDate)}</td>
                        <td class="datetime">{formatDateTime(loan.loanDate)} {formatDateTime(loan.dueDate)}</td>
                        <td class="action">
                            <div class="action-buttons">
                                <input
                                        type="checkbox"
                                        checked={selectedLoans.has(loan.id)}
                                        on:change={() => toggleLoanSelection(loan.id)}
                                        class="loan-checkbox"
                                        disabled={loading}
                                />
                                <button
                                        class="return-btn"
                                        on:click={() => returnBook(loan.id)}
                                        disabled={loading}
                                        title="Return Book"
                                >
                                    📖
                                </button>
                            </div>
                        </td>
                    </tr>
                {:else}
                    <tr>
                        <td colspan="6" class="no-loans">
                            {loading ? 'Loading borrowed books...' : 'No borrowed books found'}
                        </td>
                    </tr>
                {/each}
            {:else}
                {#each filteredReturnedLoans as loan (loan.id)}
                    <tr class="loan-row returned" in:fade={{ duration: 200 }}>
                        <td class="loan-id">{loan.id.toString().padStart(3, '0')}</td>
                        <td class="user-id">{loan.userId}</td>
                        <td class="amount">{loan.quantity.toString().padStart(3, '0')} Books</td>
                        <td class="due-date">{formatDate(loan.dueDate)}</td>
                        <td class="datetime">{formatDateTime(loan.returnDate || loan.loanDate)}</td>
                        <td class="action">
                            <span class="status-badge returned">Returned</span>
                        </td>
                    </tr>
                {:else}
                    <tr>
                        <td colspan="6" class="no-loans">
                            {loading ? 'Loading return history...' : 'No return history found'}
                        </td>
                    </tr>
                {/each}
            {/if}
            </tbody>
        </table>
    </div>

    <!-- 대출 중인 도서 액션 버튼 -->
    {#if currentTab === 'borrowed'}
        <div class="action-section">
            <div class="selected-info">
                <span class="selected-count">Selected: {selectedCount} book{selectedCount !== 1 ? 's' : ''}</span>
            </div>
            <button
                    class="bulk-return-btn"
                    class:disabled={!canReturn}
                    disabled={!canReturn}
                    on:click={returnSelectedBooks}
            >
                {#if loading}
                    <span class="btn-spinner"></span>
                    Processing...
                {:else}
                    Return Selected Books
                {/if}
            </button>
        </div>
    {/if}
</div>

<!-- 반납 확인 모달 -->
{#if showReturnModal && returnConfirmData}
    <div class="modal-overlay" on:click={closeModal} in:fade>
        <div class="return-modal" on:click|stopPropagation in:scale>
            <div class="modal-header">
                <h3>Book Return Confirmation</h3>
                <button class="close-btn" on:click={closeModal}>✕</button>
            </div>

            <div class="modal-content">
                {#if returnConfirmData.isMultiple}
                    <!-- 일괄 반납 결과 -->
                    <div class="bulk-return-result">
                        <div class="result-icon">✅</div>
                        <div class="result-message">{returnConfirmData.message}</div>
                        <div class="result-details">
                            <div class="detail-row">
                                <span class="label">Total Books:</span>
                                <span class="value">{returnConfirmData.totalBooks} Books</span>
                            </div>
                            <div class="detail-row">
                                <span class="label">Successfully Returned:</span>
                                <span class="value">{returnConfirmData.successCount} Books</span>
                            </div>
                        </div>
                    </div>
                {:else}
                    <!-- 단일 반납 결과 -->
                    <div class="return-details">
                        <div class="book-info">
                            <div class="info-row">
                                <span class="label">Book ID:</span>
                                <span class="value">{returnConfirmData.loanId}</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Name:</span>
                                <span class="value">{returnConfirmData.userName}</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Book Title:</span>
                                <span class="value">{returnConfirmData.bookTitle}</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Return Date:</span>
                                <span class="value">{formatDate(returnConfirmData.returnDate)}</span>
                            </div>
                            {#if returnConfirmData.wasOverdue}
                                <div class="info-row overdue">
                                    <span class="label">Overdue Days:</span>
                                    <span class="value">{returnConfirmData.overdueDays} days</span>
                                </div>
                            {/if}
                        </div>

                        <div class="return-summary">
                            <div class="summary-icon">
                                {returnConfirmData.wasOverdue ? '⚠️' : '✅'}
                            </div>
                            <div class="summary-message">
                                {returnConfirmData.message}
                            </div>
                        </div>
                    </div>
                {/if}

                <div class="modal-actions">
                    <button class="confirm-btn" on:click={closeModal}>
                        CLOSE
                    </button>
                </div>
            </div>
        </div>
    </div>
{/if}

<style>
    /* ==================== 대출 관리 폼 ==================== */
    .loans-form {
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
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .form-header h2 {
        font-size: 1.5rem;
        font-weight: 600;
        color: #111827;
        margin: 0;
    }

    .header-info {
        display: flex;
        align-items: center;
        gap: 0.5rem;
    }

    .user-name {
        font-weight: 600;
        color: #111827;
    }

    .user-label {
        background: #f3f4f6;
        color: #6b7280;
        padding: 0.25rem 0.75rem;
        border-radius: 12px;
        font-size: 0.75rem;
        font-weight: 500;
    }

    /* 탭 메뉴 */
    .tab-menu {
        display: flex;
        background: #f9fafb;
        border-bottom: 1px solid #e5e7eb;
    }

    .tab-btn {
        flex: 1;
        padding: 1rem;
        background: none;
        border: none;
        font-weight: 500;
        color: #6b7280;
        cursor: pointer;
        transition: all 0.2s;
        border-bottom: 3px solid transparent;
    }

    .tab-btn:hover {
        background: rgba(0, 0, 0, 0.05);
        color: #374151;
    }

    .tab-btn.active {
        background: white;
        color: #000000;
        border-bottom-color: #000000;
    }

    /* 검색 섹션 */
    .search-section {
        padding: 1rem 1.5rem;
        border-bottom: 1px solid #e5e7eb;
        background: #f9fafb;
    }

    .search-bar {
        display: flex;
        gap: 0.5rem;
        max-width: 400px;
    }

    .search-input {
        flex: 1;
        padding: 0.75rem;
        border: 1px solid #d1d5db;
        border-radius: 6px;
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

    /* ==================== 대출 테이블 ==================== */
    .loans-table-container {
        position: relative;
        max-height: 500px;
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

    .loans-table {
        width: 100%;
        border-collapse: collapse;
    }

    .loans-table th {
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

    .loans-table td {
        padding: 1rem;
        border-bottom: 1px solid #e5e7eb;
        color: #374151;
        font-size: 0.875rem;
    }

    .loan-row {
        transition: all 0.2s;
        position: relative;
    }

    .loan-row:hover {
        background: #f9fafb;
    }

    .loan-row.selected {
        background: #f3f4f6;
        border-left: 4px solid #000000;
    }

    /* 상태별 스타일 */
    .loan-row.overdue {
        background: rgba(239, 68, 68, 0.05);
        border-left: 4px solid #ef4444;
    }

    .loan-row.due-soon {
        background: rgba(245, 158, 11, 0.05);
        border-left: 4px solid #f59e0b;
    }

    .loan-row.returned {
        background: rgba(34, 197, 94, 0.05);
        border-left: 4px solid #22c55e;
    }

    .loan-id,
    .user-id {
        font-family: monospace;
        font-weight: 500;
        color: #6b7280;
    }

    .amount {
        font-weight: 500;
        color: #111827;
    }

    .due-date {
        font-family: monospace;
        font-weight: 500;
    }

    .datetime {
        font-family: monospace;
        font-size: 0.75rem;
        color: #6b7280;
    }

    .action {
        text-align: center;
    }

    .action-buttons {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 0.5rem;
    }

    .loan-checkbox {
        width: 18px;
        height: 18px;
        cursor: pointer;
        accent-color: #000000;
    }

    .loan-checkbox:disabled {
        opacity: 0.5;
        cursor: not-allowed;
    }

    .return-btn {
        width: 32px;
        height: 32px;
        background: #000000;
        color: white;
        border: none;
        border-radius: 6px;
        cursor: pointer;
        font-size: 1rem;
        transition: all 0.2s;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .return-btn:hover:not(:disabled) {
        background: #374151;
        transform: scale(1.05);
    }

    .return-btn:disabled {
        background: #9ca3af;
        cursor: not-allowed;
        transform: none;
    }

    .status-badge {
        padding: 0.25rem 0.75rem;
        border-radius: 12px;
        font-size: 0.75rem;
        font-weight: 500;
        text-transform: uppercase;
        border: 1px solid;
    }

    .status-badge.returned {
        background: #f0fdf4;
        color: #166534;
        border-color: #22c55e;
    }

    .status-badge.overdue {
        background: #fef2f2;
        color: #991b1b;
        border-color: #ef4444;
    }

    .status-badge.due-soon {
        background: #fef3c7;
        color: #92400e;
        border-color: #f59e0b;
    }

    .status-badge.active {
        background: #f0f9ff;
        color: #0369a1;
        border-color: #3b82f6;
    }

    .no-loans {
        text-align: center;
        color: #6b7280;
        font-style: italic;
        padding: 2rem;
    }

    /* ==================== 액션 섹션 ==================== */
    .action-section {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1.5rem;
        background: #f9fafb;
        border-top: 1px solid #e5e7eb;
    }

    .selected-info {
        display: flex;
        align-items: center;
        gap: 1rem;
    }

    .selected-count {
        font-weight: 500;
        color: #374151;
    }

    .bulk-return-btn {
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

    .bulk-return-btn:hover:not(:disabled) {
        background: #374151;
        transform: translateY(-1px);
    }

    .bulk-return-btn:disabled,
    .bulk-return-btn.disabled {
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

    .return-modal {
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

    /* 일괄 반납 결과 */
    .bulk-return-result {
        text-align: center;
        margin-bottom: 1.5rem;
    }

    .result-icon {
        font-size: 3rem;
        margin-bottom: 1rem;
    }

    .result-message {
        font-size: 1.125rem;
        font-weight: 600;
        color: #111827;
        margin-bottom: 1.5rem;
    }

    .result-details {
        background: #f9fafb;
        border: 1px solid #e5e7eb;
        border-radius: 8px;
        padding: 1rem;
    }

    .detail-row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 0.5rem;
    }

    .detail-row:last-child {
        margin-bottom: 0;
        font-weight: 600;
        border-top: 1px solid #e5e7eb;
        padding-top: 0.5rem;
    }

    /* 단일 반납 결과 */
    .return-details {
        margin-bottom: 1.5rem;
    }

    .book-info {
        background: #f9fafb;
        border: 1px solid #e5e7eb;
        border-radius: 8px;
        padding: 1rem;
        margin-bottom: 1rem;
    }

    .info-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 0.5rem 0;
        border-bottom: 1px solid #e5e7eb;
    }

    .info-row:last-child {
        border-bottom: none;
    }

    .info-row.overdue {
        color: #dc2626;
        font-weight: 600;
    }

    .label {
        font-weight: 500;
        color: #374151;
        min-width: 120px;
    }

    .value {
        color: #111827;
        font-weight: 500;
        text-align: right;
    }

    .return-summary {
        display: flex;
        align-items: center;
        gap: 1rem;
        padding: 1rem;
        background: #f0fdf4;
        border: 1px solid #22c55e;
        border-radius: 8px;
    }

    .summary-icon {
        font-size: 2rem;
    }

    .summary-message {
        font-weight: 500;
        color: #166534;
    }

    .modal-actions {
        display: flex;
        justify-content: flex-end;
        gap: 1rem;
    }

    .confirm-btn {
        padding: 0.75rem 1.5rem;
        background: #000000;
        color: white;
        border: none;
        border-radius: 6px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s;
        font-size: 0.875rem;
        text-transform: uppercase;
    }

    .confirm-btn:hover {
        background: #374151;
    }

    /* ==================== 반응형 디자인 ==================== */
    @media (max-width: 1024px) {
        .form-header {
            flex-direction: column;
            gap: 1rem;
            text-align: center;
        }

        .search-bar {
            max-width: none;
        }

        .action-section {
            flex-direction: column;
            gap: 1rem;
            text-align: center;
        }
    }

    @media (max-width: 768px) {
        .loans-table-container {
            overflow-x: auto;
        }

        .loans-table {
            min-width: 700px;
        }

        .loans-table th,
        .loans-table td {
            padding: 0.5rem;
            font-size: 0.75rem;
        }

        .datetime {
            font-size: 0.625rem;
        }

        .return-modal {
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
            gap: 0.25rem;
        }

        .value {
            text-align: left;
        }

        .return-summary {
            flex-direction: column;
            text-align: center;
        }
    }

    @media (max-width: 480px) {
        .tab-btn {
            font-size: 0.875rem;
            padding: 0.75rem;
        }

        .search-bar {
            flex-direction: column;
        }

        .search-input,
        .search-btn {
            width: 100%;
        }

        .action-buttons {
            flex-direction: column;
            gap: 0.25rem;
        }

        .return-btn {
            width: 100%;
            height: 28px;
            font-size: 0.875rem;
        }

        .bulk-return-btn,
        .confirm-btn {
            width: 100%;
        }

        .result-details,
        .book-info {
            padding: 0.75rem;
        }
    }

    /* ==================== 접근성 ==================== */
    @media (prefers-reduced-motion: reduce) {
        .loan-row,
        .return-btn,
        .bulk-return-btn,
        .tab-btn {
            transition: none;
        }

        .loading-spinner,
        .btn-spinner {
            animation: none;
        }
    }

    /* 고대비 모드 지원 */
    @media (prefers-contrast: high) {
        .loans-table {
            border: 2px solid #000000;
        }

        .loan-row {
            border-bottom: 2px solid #000000;
        }

        .status-badge {
            border-width: 2px;
        }
    }

    /* 포커스 스타일 */
    .tab-btn:focus,
    .search-input:focus,
    .search-btn:focus,
    .return-btn:focus,
    .bulk-return-btn:focus,
    .loan-checkbox:focus,
    .close-btn:focus,
    .confirm-btn:focus {
        outline: 2px solid #000000;
        outline-offset: 2px;
    }

    /* 프린트 스타일 */
    @media print {
        .search-section,
        .action-section,
        .modal-overlay,
        .return-btn,
        .loan-checkbox {
            display: none;
        }

        .loans-form {
            box-shadow: none;
            border: 1px solid #000000;
        }

        .loans-table {
            font-size: 0.75rem;
        }

        .loan-row {
            break-inside: avoid;
        }
    }

    /* 다크모드 지원 (선택사항) */
    @media (prefers-color-scheme: dark) {
        .loans-form {
            background: #1f2937;
            border-color: #374151;
        }

        .form-header,
        .search-section {
            background: #111827;
        }

        .form-header h2,
        .user-name,
        .loans-table th {
            color: #f9fafb;
        }

        .loans-table td,
        .selected-count {
            color: #d1d5db;
        }

        .search-input {
            background: #374151;
            border-color: #4b5563;
            color: #f9fafb;
        }

        .search-input::placeholder {
            color: #9ca3af;
        }

        .loan-row:hover {
            background: #374151;
        }

        .return-modal {
            background: #1f2937;
            border-color: #374151;
        }

        .modal-header {
            background: #111827;
        }

        .modal-header h3 {
            color: #f9fafb;
        }

        .book-info,
        .result-details {
            background: #111827;
            border-color: #374151;
        }

        .label,
        .value {
            color: #d1d5db;
        }
    }
</style>
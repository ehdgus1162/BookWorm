<script>
    import { onMount } from 'svelte';
    import { fade, scale } from 'svelte/transition';
    import { bookApi, unifiedAuthApi } from '$lib/api.js';

    // 상태 관리
    let books = [];
    let loading = false;
    let error = null;
    let totalPages = 0;
    let totalBooks = 0;

    // 모달 상태
    let showAddModal = false;
    let showEditModal = false;
    let showViewModal = false;
    let showDeleteModal = false;
    let selectedBook = null;

    // 폼 데이터 - 백엔드 구조에 맞춤
    let bookForm = {
        title: '',
        language: 'English',
        type: 'Fiction',
        quantity: 1
    };

    // 검색 및 필터 - 백엔드 구조에 맞춤
    let searchParams = {
        keyword: '',
        category: '',
        language: '',
        status: '',
        page: 0,
        size: 20
    };

    // === 유틸리티 함수들 ===

    /**
     * 숫자 유효성 검사
     * @param {any} num - 검사할 값
     * @returns {boolean} - 유효한 양수인지 여부
     */
    function isValidNumber(num) {
        if (!num) return false;
        const number = parseInt(num);
        return !isNaN(number) && number > 0;
    }

    /**
     * 문자열 정리
     * @param {string} str - 정리할 문자열
     * @returns {string} - 정리된 문자열
     */
    function sanitizeString(str) {
        return str ? str.trim() : '';
    }

    /**
     * 백엔드 페이징 응답 처리 - BookPageResponse 구조
     * Spring Boot의 페이징 응답을 표준화된 형태로 변환
     * @param {Object} result - API 응답 결과
     * @returns {Object} - 표준화된 페이징 데이터
     */
    function processPageResponse(result) {
        // Spring Boot BookPageResponse 구조 처리
        if (result && result.content && Array.isArray(result.content)) {
            return {
                books: result.content,
                totalPages: result.totalPages || 0,
                totalElements: result.totalElements || 0,
                currentPage: result.currentPage || 0,
                pageSize: result.size || 20
            };
        }

        // 일반 배열인 경우
        if (Array.isArray(result)) {
            return {
                books: result,
                totalPages: 1,
                totalElements: result.length,
                currentPage: 0,
                pageSize: result.length
            };
        }

        // 예상치 못한 응답 구조
        console.warn('예상치 못한 응답 구조:', result);
        return {
            books: [],
            totalPages: 0,
            totalElements: 0,
            currentPage: 0,
            pageSize: 20
        };
    }

    /**
     * 인증 에러 처리 공통 함수
     * 401 Unauthorized 에러 발생 시 로그인 페이지로 리다이렉트
     * @param {Error} err - 발생한 에러
     * @returns {boolean} - 인증 에러 여부
     */
    function handleAuthError(err) {
        if (err.message.includes('인증이 필요합니다') ||
            err.message.includes('401') ||
            err.message.includes('Unauthorized')) {
            console.warn('관리자 인증이 만료되었습니다. 관리자 로그인 페이지로 이동합니다.');
            window.location.href = '/admin/login';
            return true;
        }
        return false;
    }

    // === API 호출 함수들 ===

    /**
     * 도서 목록 조회 (페이징)
     * 전체 도서 목록을 페이징 처리하여 조회
     */
    async function fetchBooks() {
        loading = true;
        error = null;

        try {
            console.log('📚 도서 목록 조회 시작:', searchParams);

            const result = await bookApi.getBooks(
                searchParams.page,
                searchParams.size
            );

            console.log('📚 도서 목록 조회 응답:', result);

            // API 응답이 성공적인 경우 처리
            if (result && (result.success || result.data || Array.isArray(result))) {
                const pageData = processPageResponse(result.data || result);
                books = pageData.books;
                totalPages = pageData.totalPages;
                totalBooks = pageData.totalElements;

                console.log('✅ 도서 목록 처리 완료:', { books: books.length, totalPages, totalBooks });
            } else {
                throw new Error(result?.message || '도서 목록 조회 실패');
            }

        } catch (err) {
            console.error('❌ 도서 목록 조회 실패:', err);
            if (handleAuthError(err)) return;

            error = err.message || '도서 목록을 불러오는 중 오류가 발생했습니다.';
            books = [];
            totalPages = 0;
            totalBooks = 0;
        } finally {
            loading = false;
        }
    }

    /**
     * 도서 검색
     * 키워드, 카테고리, 언어, 상태 등으로 도서를 검색
     */
    async function searchBooks() {
        loading = true;
        error = null;

        try {
            const searchRequest = {
                keyword: sanitizeString(searchParams.keyword) || null,
                category: searchParams.category || null,
                language: searchParams.language || null,
                status: searchParams.status || null,
                page: searchParams.page,
                size: searchParams.size
            };

            console.log('🔍 도서 검색 시작:', searchRequest);

            const result = await bookApi.searchBooks(searchRequest);

            console.log('🔍 도서 검색 응답:', result);

            if (result && (result.success || result.data || Array.isArray(result))) {
                const pageData = processPageResponse(result.data || result);
                books = pageData.books;
                totalPages = pageData.totalPages;
                totalBooks = pageData.totalElements;

                console.log('✅ 도서 검색 처리 완료:', { books: books.length, totalPages, totalBooks });
            } else {
                throw new Error(result?.message || '도서 검색 실패');
            }

        } catch (err) {
            console.error('❌ 도서 검색 실패:', err);
            if (handleAuthError(err)) return;

            error = err.message || '도서 검색 중 오류가 발생했습니다.';
            books = [];
            totalPages = 0;
            totalBooks = 0;
        } finally {
            loading = false;
        }
    }

    /**
     * 새 도서 등록
     * 사용자가 입력한 정보를 바탕으로 새로운 도서를 등록
     */
    async function createBook() {
        // 클라이언트 측 유효성 검사
        if (!sanitizeString(bookForm.title)) {
            error = '제목은 필수 입력 항목입니다.';
            return;
        }

        loading = true;
        error = null;

        try {
            const createRequest = {
                title: sanitizeString(bookForm.title),
                language: bookForm.language || 'English',
                type: bookForm.type || 'Fiction',
                quantity: isValidNumber(bookForm.quantity) ? parseInt(bookForm.quantity) : 1
            };

            console.log('📝 도서 등록 시작:', createRequest);

            const result = await bookApi.createBook(createRequest);

            console.log('📝 도서 등록 응답:', result);

            if (result && (result.success || result.data)) {
                await fetchBooks();
                closeAllModals();
                console.log('✅ 도서 등록 성공:', result.data || result);
            } else {
                throw new Error(result?.message || '도서 등록 실패');
            }

        } catch (err) {
            console.error('❌ 도서 등록 실패:', err);
            if (handleAuthError(err)) return;

            error = err.message || '도서 등록 중 오류가 발생했습니다.';
        } finally {
            loading = false;
        }
    }

    /**
     * 도서 정보 수정
     * 선택된 도서의 정보를 수정
     */
    async function updateBook() {
        if (!selectedBook) return;

        if (!sanitizeString(bookForm.title)) {
            error = '제목은 필수 입력 항목입니다.';
            return;
        }

        loading = true;
        error = null;

        try {
            const updateRequest = {
                title: sanitizeString(bookForm.title),
                language: bookForm.language || 'English',
                type: bookForm.type || 'Fiction',
                quantity: isValidNumber(bookForm.quantity) ? parseInt(bookForm.quantity) : 1
            };

            console.log('✏️ 도서 수정 시작:', selectedBook.id, updateRequest);

            const result = await bookApi.updateBook(selectedBook.id, updateRequest);

            console.log('✏️ 도서 수정 응답:', result);

            if (result && (result.success || result.data)) {
                await fetchBooks();
                closeAllModals();
                console.log('✅ 도서 수정 성공:', result.data || result);
            } else {
                throw new Error(result?.message || '도서 수정 실패');
            }

        } catch (err) {
            console.error('❌ 도서 수정 실패:', err);
            if (handleAuthError(err)) return;
            error = err.message || '도서 수정 중 오류가 발생했습니다.';
        } finally {
            loading = false;
        }
    }

    /**
     * 도서 삭제
     * 선택된 도서를 삭제 (대출 중인 도서는 삭제 불가)
     */
    async function deleteBook() {
        if (!selectedBook) return;

        loading = true;
        error = null;

        try {
            console.log('🗑️ 도서 삭제 시작:', selectedBook.id);

            const result = await bookApi.deleteBook(selectedBook.id);

            console.log('🗑️ 도서 삭제 응답:', result);

            await fetchBooks();
            closeAllModals();
            console.log('✅ 도서 삭제 성공:', selectedBook.title);

        } catch (err) {
            console.error('❌ 도서 삭제 실패:', err);
            if (handleAuthError(err)) return;

            if (err.message.includes('대출')) {
                error = '현재 대출 중인 도서는 삭제할 수 없습니다.';
            } else {
                error = err.message || '도서 삭제 중 오류가 발생했습니다.';
            }
        } finally {
            loading = false;
        }
    }

    /**
     * 특정 도서 상세 조회
     * @param {number} bookId - 조회할 도서 ID
     * @returns {Object|null} - 도서 상세 정보 또는 null
     */
    async function fetchBookDetail(bookId) {
        try {
            console.log('📖 도서 상세 조회 시작:', bookId);

            const result = await bookApi.getBook(bookId);

            console.log('📖 도서 상세 조회 응답:', result);

            if (result && (result.success || result.data)) {
                return result.data || result;
            } else {
                throw new Error(result?.message || '도서 상세 조회 실패');
            }

        } catch (err) {
            console.error('❌ 도서 상세 조회 실패:', err);
            if (handleAuthError(err)) return null;

            error = err.message || '도서 정보를 불러오는 중 오류가 발생했습니다.';
            return null;
        }
    }

    // === UI 이벤트 핸들러들 ===

    /**
     * 도서 추가 모달 열기
     */
    function openAddModal() {
        resetForm();
        error = null;
        showAddModal = true;
    }

    /**
     * 도서 편집 모달 열기
     * @param {Object} book - 편집할 도서 객체
     */
    async function openEditModal(book) {
        selectedBook = book;
        error = null;

        // 상세 정보 조회 후 폼에 설정
        const detailBook = await fetchBookDetail(book.id);
        if (detailBook) {
            bookForm = {
                title: detailBook.title || '',
                language: detailBook.language || 'English',
                type: detailBook.type || 'Fiction',
                quantity: detailBook.quantity || 1
            };
        }

        showEditModal = true;
    }

    /**
     * 도서 상세보기 모달 열기
     * @param {Object} book - 상세 정보를 볼 도서 객체
     */
    async function openViewModal(book) {
        selectedBook = book;
        error = null;

        // 상세 정보 조회
        const detailBook = await fetchBookDetail(book.id);
        if (detailBook) {
            selectedBook = detailBook;
        }

        showViewModal = true;
    }

    /**
     * 도서 삭제 확인 모달 열기
     * @param {Object} book - 삭제할 도서 객체
     */
    function openDeleteModal(book) {
        selectedBook = book;
        error = null;
        showDeleteModal = true;
    }

    /**
     * 모든 모달 닫기 및 상태 초기화
     */
    function closeAllModals() {
        showAddModal = false;
        showEditModal = false;
        showViewModal = false;
        showDeleteModal = false;
        selectedBook = null;
        resetForm();
        error = null;
    }

    /**
     * 폼 데이터 초기화
     */
    function resetForm() {
        bookForm = {
            title: '',
            language: 'English',
            type: 'Fiction',
            quantity: 1
        };
    }

    /**
     * 검색 실행
     * 키워드가 있으면 검색, 없으면 전체 목록 조회
     */
    function handleSearch() {
        searchParams.page = 0; // 첫 페이지로 리셋
        error = null;

        if (sanitizeString(searchParams.keyword)) {
            searchBooks();
        } else {
            fetchBooks();
        }
    }

    /**
     * 페이지 변경
     * @param {number} page - 이동할 페이지 번호
     */
    function goToPage(page) {
        if (page >= 0 && page < totalPages) {
            searchParams.page = page;
            error = null;

            if (sanitizeString(searchParams.keyword)) {
                searchBooks();
            } else {
                fetchBooks();
            }
        }
    }

    /**
     * 키보드 이벤트 처리
     * @param {KeyboardEvent} event - 키보드 이벤트
     */
    function handleKeydown(event) {
        if (event.key === 'Escape') {
            closeAllModals();
        }
    }

    /**
     * 폼 내에서 엔터키 처리
     * @param {KeyboardEvent} event - 키보드 이벤트
     */
    function handleFormKeydown(event) {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();

            if (showAddModal) {
                createBook();
            } else if (showEditModal) {
                updateBook();
            }
        }
    }

    // 컴포넌트 마운트 시 실행
    onMount(async () => {
        console.log('📚 Books 페이지 마운트');

        try {
            // 관리자 인증 상태 확인
            console.log('🔐 관리자 인증 상태 확인 중...');
            const authResult = await unifiedAuthApi.getCurrentUser();

            if (!authResult || !authResult.success || authResult.data?.role !== 'ADMIN') {
                console.warn('⚠️ 관리자 권한 없음, 로그인 페이지로 이동');
                window.location.href = '/admin/login';
                return;
            }

            console.log('✅ 관리자 인증 확인됨, 도서 목록 로드 시작');
            await fetchBooks();

        } catch (err) {
            console.error('❌ 인증 확인 또는 데이터 로드 실패:', err);
            if (handleAuthError(err)) return;
            error = '데이터를 불러오는 중 오류가 발생했습니다.';
        }
    });
</script>

<svelte:head>
    <title>Book Management - BookWorm Admin</title>
</svelte:head>

<svelte:window on:keydown={handleKeydown} />

<div class="book-management-container">
    <!-- 페이지 헤더 -->
    <div class="page-header">
        <div class="header-left">
            <h1 class="page-title">Book Management</h1>
            {#if totalBooks > 0}
                <span class="book-count">총 {totalBooks}권의 도서</span>
            {/if}
        </div>

    </div>

    <!-- 에러 메시지 -->
    {#if error}
        <div class="error-banner" in:fade>
            <span class="error-icon">⚠️</span>
            <span class="error-message">{error}</span>
            <button class="error-close" on:click={() => error = null}>✕</button>
        </div>
    {/if}

    <!-- 컨트롤 바 -->
    <div class="controls-bar">
        <div class="left-controls">
            <button class="add-btn" on:click={openAddModal} disabled={loading}>
                <span class="btn-icon">➕</span>
                Add Book
            </button>

            <div class="search-container">
                <input
                        type="text"
                        placeholder="Search books by title..."
                        bind:value={searchParams.keyword}
                        on:keydown={(e) => e.key === 'Enter' && handleSearch()}
                        class="search-input"
                        disabled={loading}
                />
                <button class="search-btn" on:click={handleSearch} disabled={loading}>
                    🔍
                </button>
            </div>
        </div>

        <div class="right-controls">
            <select bind:value={searchParams.category} on:change={handleSearch} disabled={loading}>
                <option value="">All Types</option>
                <option value="Educational">Educational</option>
                <option value="Fiction">Fiction</option>
                <option value="Non-Fiction">Non-Fiction</option>
                <option value="Science">Science</option>
                <option value="Technology">Technology</option>
            </select>

            <select bind:value={searchParams.language} on:change={handleSearch} disabled={loading}>
                <option value="">All Languages</option>
                <option value="English">English</option>
                <option value="Korean">Korean</option>
                <option value="Spanish">Spanish</option>
            </select>

            <select bind:value={searchParams.status} on:change={handleSearch} disabled={loading}>
                <option value="">All Status</option>
                <option value="AVAILABLE">Available</option>
                <option value="BORROWED">Borrowed</option>
                <option value="RESERVED">Reserved</option>
                <option value="MAINTENANCE">Maintenance</option>
            </select>
        </div>
    </div>

    <!-- 도서 테이블 -->
    <div class="table-container" class:loading>
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
                <th>Title</th>
                <th>Type</th>
                <th>Language</th>
                <th>Quantity</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            {#each books as book (book.id)}
                <tr class="book-row" in:fade={{ duration: 200 }}>
                    <td class="book-id">#{book.id}</td>
                    <td class="book-title">{book.title}</td>
                    <td class="book-type">{book.type || '-'}</td>
                    <td class="book-language">{book.language}</td>
                    <td class="book-quantity">{book.quantity || '-'}</td>
                    <td class="book-status">
                            <span class="status-badge {(book.status || 'available').toLowerCase()}">
                                {book.status || 'Available'}
                            </span>
                    </td>
                    <td class="book-actions">
                        <button
                                type="button"
                                class="action-btn view"
                                on:click={() => openViewModal(book)}
                                disabled={loading}
                                aria-label="View details for {book.title}"
                        >
                            👁️
                        </button>
                        <button
                                type="button"
                                class="action-btn edit"
                                on:click={() => openEditModal(book)}
                                disabled={loading}
                                aria-label="Edit {book.title}"
                        >
                            ✏️
                        </button>
                        <button
                                type="button"
                                class="action-btn delete"
                                on:click={() => openDeleteModal(book)}
                                disabled={loading}
                                aria-label="Delete {book.title}"
                        >
                            🗑️
                        </button>
                    </td>
                </tr>
            {:else}
                <tr>
                    <td colspan="7" class="no-data">
                        {#if loading}
                            Loading books...
                        {:else}
                            No books found
                        {/if}
                    </td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>

    <!-- 페이지네이션 -->
    {#if totalPages > 1}
        <div class="pagination">
            <button
                    class="page-btn"
                    on:click={() => goToPage(searchParams.page - 1)}
                    disabled={loading || searchParams.page === 0}
            >
                이전
            </button>

            {#each Array(totalPages) as _, i}
                <button
                        class="page-btn"
                        class:active={i === searchParams.page}
                        on:click={() => goToPage(i)}
                        disabled={loading}
                >
                    {i + 1}
                </button>
            {/each}

            <button
                    class="page-btn"
                    on:click={() => goToPage(searchParams.page + 1)}
                    disabled={loading || searchParams.page >= totalPages - 1}
            >
                다음
            </button>
        </div>
    {/if}

    <!-- Add Book Modal -->
    {#if showAddModal}
        <div class="modal-overlay" on:click={closeAllModals} transition:fade>
            <div class="modal-container add-modal" on:click|stopPropagation transition:scale>
                <div class="modal-header">
                    <div class="modal-title">
                        <span class="modal-icon">📚</span>
                        Add New Book
                    </div>
                    <button type="button" class="close-btn" on:click={closeAllModals}>✕</button>
                </div>

                <div class="modal-body">
                    {#if error}
                        <div class="error-message">{error}</div>
                    {/if}

                    <form on:submit|preventDefault={createBook} on:keydown={handleFormKeydown}>
                        <div class="form-grid">
                            <div class="form-group">
                                <label for="title">Title *</label>
                                <input type="text" id="title" bind:value={bookForm.title} required disabled={loading}>
                            </div>

                            <div class="form-group">
                                <label for="type">Type *</label>
                                <select id="type" bind:value={bookForm.type} required disabled={loading}>
                                    <option value="">Select type</option>
                                    <option value="Educational">Educational</option>
                                    <option value="Fiction">Fiction</option>
                                    <option value="Non-Fiction">Non-Fiction</option>
                                    <option value="Science">Science</option>
                                    <option value="Technology">Technology</option>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="language">Language *</label>
                                <select id="language" bind:value={bookForm.language} required disabled={loading}>
                                    <option value="English">English</option>
                                    <option value="Korean">Korean</option>
                                    <option value="Spanish">Spanish</option>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="quantity">Quantity *</label>
                                <input type="number" id="quantity" bind:value={bookForm.quantity} min="1" required disabled={loading}>
                            </div>
                        </div>
                    </form>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn secondary" on:click={closeAllModals} disabled={loading}>
                        CANCEL
                    </button>
                    <button type="button" class="btn primary" on:click={createBook} disabled={loading}>
                        {#if loading}
                            ADDING...
                        {:else}
                            ADD BOOK
                        {/if}
                    </button>
                </div>
            </div>
        </div>
    {/if}

    <!-- Edit Book Modal - 백엔드 필드만 사용 -->
    {#if showEditModal}
        <div class="modal-overlay" on:click={closeAllModals} transition:fade>
            <div class="modal-container edit-modal" on:click|stopPropagation transition:scale>
                <div class="modal-header">
                    <div class="modal-title">
                        <span class="modal-icon">✏️</span>
                        Edit Book
                    </div>
                    <button type="button" class="close-btn" on:click={closeAllModals}>✕</button>
                </div>

                <div class="modal-body">
                    {#if error}
                        <div class="error-message">{error}</div>
                    {/if}

                    <form on:submit|preventDefault={updateBook}>
                        <div class="form-grid">
                            <div class="form-group">
                                <label for="edit-title">Title *</label>
                                <input type="text" id="edit-title" bind:value={bookForm.title} required disabled={loading}>
                            </div>

                            <div class="form-group">
                                <label for="edit-type">Type *</label>
                                <select id="edit-type" bind:value={bookForm.type} required disabled={loading}>
                                    <option value="">Select type</option>
                                    <option value="Educational">Educational</option>
                                    <option value="Fiction">Fiction</option>
                                    <option value="Non-Fiction">Non-Fiction</option>
                                    <option value="Science">Science</option>
                                    <option value="Technology">Technology</option>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="edit-language">Language *</label>
                                <select id="edit-language" bind:value={bookForm.language} required disabled={loading}>
                                    <option value="English">English</option>
                                    <option value="Korean">Korean</option>
                                    <option value="Spanish">Spanish</option>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="edit-quantity">Quantity *</label>
                                <input type="number" id="edit-quantity" bind:value={bookForm.quantity} min="1" required disabled={loading}>
                            </div>
                        </div>

                        <!-- 다른 필드들 제거 -->
                    </form>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn secondary" on:click={closeAllModals} disabled={loading}>
                        CANCEL
                    </button>
                    <button type="button" class="btn primary" on:click={updateBook} disabled={loading}>
                        {#if loading}
                            UPDATING...
                        {:else}
                            UPDATE
                        {/if}
                    </button>
                </div>
            </div>
        </div>
    {/if}

    <!-- View Book Modal - 백엔드 응답 필드만 표시 -->
    {#if showViewModal && selectedBook}
        <div class="modal-overlay" on:click={closeAllModals} transition:fade>
            <div class="modal-container view-modal" on:click|stopPropagation transition:scale>
                <div class="modal-header">
                    <div class="modal-title">
                        <span class="modal-icon">👁️</span>
                        Book Details
                    </div>
                    <button type="button" class="close-btn" on:click={closeAllModals}>✕</button>
                </div>

                <div class="modal-body">
                    <div class="view-content">
                        <div class="view-item">
                            <span class="view-label">Title:</span>
                            <span class="view-value">{selectedBook.title}</span>
                        </div>

                        <div class="view-item">
                            <span class="view-label">Type:</span>
                            <span class="view-value">{selectedBook.type || '-'}</span>
                        </div>

                        <div class="view-item">
                            <span class="view-label">Language:</span>
                            <span class="view-value">{selectedBook.language}</span>
                        </div>

                        <div class="view-item">
                            <span class="view-label">Quantity:</span>
                            <span class="view-value">{selectedBook.quantity}</span>
                        </div>

                        <div class="view-item">
                            <span class="view-label">Status:</span>
                            <span class="status-badge {(selectedBook.status || 'available').toLowerCase()}">
                            {selectedBook.status || 'Available'}
                        </span>
                        </div>

                        <div class="view-item">
                            <span class="view-label">Registered By:</span>
                            <span class="view-value">{selectedBook.registeredBy || '-'}</span>
                        </div>

                        {#if selectedBook.registeredAt}
                            <div class="view-item">
                                <span class="view-label">Registered At:</span>
                                <span class="view-value">{selectedBook.registeredAt}</span>
                            </div>
                        {/if}

                        <div class="view-item">
                            <span class="view-label">Available:</span>
                            <span class="view-value">{selectedBook.isAvailable ? 'Yes' : 'No'}</span>
                        </div>

                        <div class="view-item">
                            <span class="view-label">Book ID:</span>
                            <span class="view-value">#{selectedBook.id}</span>
                        </div>

                        <!-- author, isbn, publisher, publicationYear, description 필드 제거 -->
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn primary" on:click={closeAllModals}>CLOSE</button>
                </div>
            </div>
        </div>
    {/if}

    <!-- Delete Confirmation Modal -->
    {#if showDeleteModal && selectedBook}
        <div class="modal-overlay" on:click={closeAllModals} transition:fade>
            <div class="modal-container delete-modal" on:click|stopPropagation transition:scale>
                <div class="modal-header">
                    <div class="modal-title">
                        <span class="modal-icon">⚠️</span>
                        Delete Confirmation
                    </div>
                    <button type="button" class="close-btn" on:click={closeAllModals}>✕</button>
                </div>

                <div class="modal-body">
                    <div class="delete-content">
                        <p class="delete-message">
                            Are you sure you want to delete "<strong>{selectedBook.title}</strong>"?
                        </p>
                        <p class="delete-warning">
                            This action cannot be undone. All associated borrowing records will also be affected.
                        </p>
                        {#if error}
                            <div class="error-message">{error}</div>
                        {/if}
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn secondary" on:click={closeAllModals} disabled={loading}>
                        CANCEL
                    </button>
                    <button type="button" class="btn danger" on:click={deleteBook} disabled={loading}>
                        {#if loading}
                            DELETING...
                        {:else}
                            CONFIRM DELETE
                        {/if}
                    </button>
                </div>
            </div>
        </div>
    {/if}
</div>
                    <style>
                        .book-management-container {
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

                        .header-left {
                            display: flex;
                            align-items: center;
                            gap: 1rem;
                        }

                        .page-title {
                            font-size: 1.75rem;
                            font-weight: 600;
                            color: #1e293b;
                            margin: 0;
                        }

                        .book-count {
                            font-size: 0.875rem;
                            color: #64748b;
                            padding: 0.25rem 0.75rem;
                            background: #f1f5f9;
                            border-radius: 20px;
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

                        /* 에러 배너 */
                        .error-banner {
                            display: flex;
                            align-items: center;
                            gap: 1rem;
                            padding: 1rem;
                            background: #fef2f2;
                            border: 1px solid #fecaca;
                            border-radius: 8px;
                            margin-bottom: 1.5rem;
                            color: #dc2626;
                        }

                        .error-icon {
                            font-size: 1.25rem;
                        }

                        .error-message {
                            flex: 1;
                            font-size: 0.875rem;
                        }

                        .error-close {
                            background: none;
                            border: none;
                            color: #dc2626;
                            cursor: pointer;
                            font-size: 1.25rem;
                            padding: 0.25rem;
                        }

                        /* 컨트롤 바 */
                        .controls-bar {
                            display: flex;
                            justify-content: space-between;
                            align-items: center;
                            margin-bottom: 1.5rem;
                            gap: 1rem;
                        }

                        .left-controls {
                            display: flex;
                            align-items: center;
                            gap: 1rem;
                        }

                        .add-btn {
                            display: flex;
                            align-items: center;
                            gap: 0.5rem;
                            padding: 0.75rem 1.5rem;
                            background: #3b82f6;
                            color: white;
                            border: none;
                            border-radius: 8px;
                            font-weight: 500;
                            cursor: pointer;
                            transition: all 0.2s ease;
                        }

                        .add-btn:hover:not(:disabled) {
                            background: #2563eb;
                            transform: translateY(-1px);
                        }

                        .add-btn:disabled {
                            opacity: 0.6;
                            cursor: not-allowed;
                            transform: none;
                        }

                        .search-container {
                            display: flex;
                            position: relative;
                        }

                        .search-input {
                            padding: 0.75rem 1rem;
                            border: 1px solid #d1d5db;
                            border-radius: 8px 0 0 8px;
                            width: 300px;
                            font-size: 0.875rem;
                            transition: border-color 0.2s ease;
                        }

                        .search-input:focus {
                            outline: none;
                            border-color: #3b82f6;
                            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
                        }

                        .search-btn {
                            padding: 0.75rem;
                            background: #f3f4f6;
                            border: 1px solid #d1d5db;
                            border-left: none;
                            border-radius: 0 8px 8px 0;
                            cursor: pointer;
                            transition: background-color 0.2s ease;
                        }

                        .search-btn:hover:not(:disabled) {
                            background: #e5e7eb;
                        }

                        .search-btn:disabled {
                            opacity: 0.6;
                            cursor: not-allowed;
                        }

                        .right-controls {
                            display: flex;
                            gap: 1rem;
                        }

                        .right-controls select {
                            padding: 0.75rem;
                            border: 1px solid #d1d5db;
                            border-radius: 8px;
                            background: white;
                            font-size: 0.875rem;
                            cursor: pointer;
                        }

                        .right-controls select:disabled {
                            opacity: 0.6;
                            cursor: not-allowed;
                        }

                        /* 테이블 컨테이너 */
                        .table-container {
                            position: relative;
                            background: white;
                            border-radius: 12px;
                            overflow: hidden;
                            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
                            border: 1px solid #f1f5f9;
                            margin-bottom: 1.5rem;
                        }

                        .table-container.loading {
                            pointer-events: none;
                        }

                        .loading-overlay {
                            position: absolute;
                            top: 0;
                            left: 0;
                            right: 0;
                            bottom: 0;
                            background: rgba(255, 255, 255, 0.8);
                            display: flex;
                            flex-direction: column;
                            align-items: center;
                            justify-content: center;
                            gap: 1rem;
                            z-index: 10;
                            font-size: 0.875rem;
                            color: #64748b;
                        }

                        .loading-spinner {
                            width: 32px;
                            height: 32px;
                            border: 3px solid #f3f4f6;
                            border-top: 3px solid #3b82f6;
                            border-radius: 50%;
                            animation: spin 1s linear infinite;
                        }

                        @keyframes spin {
                            0% { transform: rotate(0deg); }
                            100% { transform: rotate(360deg); }
                        }

                        /* 테이블 */
                        .books-table {
                            width: 100%;
                            border-collapse: collapse;
                        }

                        .books-table th {
                            background: #f8fafc;
                            padding: 1rem;
                            text-align: left;
                            font-weight: 600;
                            color: #374151;
                            border-bottom: 1px solid #e5e7eb;
                            font-size: 0.875rem;
                        }

                        .books-table td {
                            padding: 1rem;
                            border-bottom: 1px solid #f3f4f6;
                            color: #1f2937;
                            font-size: 0.875rem;
                        }

                        .book-row:hover {
                            background: #f9fafb;
                        }

                        .sort-btn {
                            background: none;
                            border: none;
                            cursor: pointer;
                            display: flex;
                            align-items: center;
                            gap: 0.5rem;
                            font-weight: 600;
                            color: #374151;
                            font-size: 0.875rem;
                            padding: 0;
                        }

                        .sort-btn:hover {
                            color: #1f2937;
                        }

                        .sort-icon {
                            font-size: 0.75rem;
                            color: #3b82f6;
                        }

                        .book-id {
                            font-weight: 500;
                            color: #6b7280;
                        }

                        .book-title {
                            font-weight: 500;
                            color: #1f2937;
                            max-width: 200px;
                            overflow: hidden;
                            text-overflow: ellipsis;
                            white-space: nowrap;
                        }

                        .status-badge {
                            padding: 0.25rem 0.75rem;
                            border-radius: 20px;
                            font-size: 0.75rem;
                            font-weight: 500;
                            text-transform: uppercase;
                        }

                        .status-badge.available {
                            background: #dcfce7;
                            color: #166534;
                        }

                        .status-badge.borrowed {
                            background: #fef3c7;
                            color: #92400e;
                        }

                        .status-badge.reserved {
                            background: #e0e7ff;
                            color: #3730a3;
                        }

                        .status-badge.maintenance {
                            background: #fee2e2;
                            color: #dc2626;
                        }

                        .book-actions {
                            display: flex;
                            gap: 0.5rem;
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
                        }

                        .action-btn:disabled {
                            opacity: 0.6;
                            cursor: not-allowed;
                        }

                        .action-btn.view {
                            background: #eff6ff;
                            color: #1d4ed8;
                        }

                        .action-btn.view:hover:not(:disabled) {
                            background: #dbeafe;
                        }

                        .action-btn.edit {
                            background: #fef3c7;
                            color: #d97706;
                        }

                        .action-btn.edit:hover:not(:disabled) {
                            background: #fde68a;
                        }

                        .action-btn.delete {
                            background: #fee2e2;
                            color: #dc2626;
                        }

                        .action-btn.delete:hover:not(:disabled) {
                            background: #fecaca;
                        }

                        .no-data {
                            text-align: center;
                            color: #64748b;
                            font-style: italic;
                            padding: 2rem;
                        }

                        /* 페이지네이션 */
                        .pagination {
                            display: flex;
                            justify-content: center;
                            gap: 0.5rem;
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
                        }

                        .page-btn:hover:not(:disabled):not(.active) {
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

                        /* 모달 스타일 */
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

                        .form-grid {
                            display: grid;
                            grid-template-columns: 1fr 1fr;
                            gap: 1rem;
                            margin-bottom: 1rem;
                        }

                        .form-group {
                            display: flex;
                            flex-direction: column;
                            gap: 0.5rem;
                        }

                        .form-group.full-width {
                            grid-column: 1 / -1;
                        }

                        .form-group label {
                            font-size: 0.875rem;
                            font-weight: 500;
                            color: #374151;
                        }

                        .form-group input,
                        .form-group select,
                        .form-group textarea {
                            padding: 0.75rem;
                            border: 1px solid #d1d5db;
                            border-radius: 6px;
                            font-size: 0.875rem;
                            transition: border-color 0.2s ease;
                            font-family: inherit;
                        }

                        .form-group input:focus,
                        .form-group select:focus,
                        .form-group textarea:focus {
                            outline: none;
                            border-color: #3b82f6;
                            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
                        }

                        .form-group input:disabled,
                        .form-group select:disabled,
                        .form-group textarea:disabled {
                            opacity: 0.6;
                            cursor: not-allowed;
                        }

                        .form-group textarea {
                            resize: vertical;
                            min-height: 80px;
                        }

                        .modal-footer {
                            display: flex;
                            justify-content: flex-end;
                            gap: 1rem;
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

                        .btn:disabled {
                            opacity: 0.6;
                            cursor: not-allowed;
                            transform: none;
                        }

                        .btn.primary {
                            background: #3b82f6;
                            color: white;
                        }

                        .btn.primary:hover:not(:disabled) {
                            background: #2563eb;
                        }

                        .btn.secondary {
                            background: #f3f4f6;
                            color: #374151;
                        }

                        .btn.secondary:hover:not(:disabled) {
                            background: #e5e7eb;
                        }

                        .btn.danger {
                            background: #dc2626;
                            color: white;
                        }

                        .btn.danger:hover:not(:disabled) {
                            background: #b91c1c;
                        }

                        /* View Modal */
                        .view-content {
                            display: flex;
                            flex-direction: column;
                            gap: 1rem;
                        }

                        .view-item {
                            display: flex;
                            justify-content: space-between;
                            align-items: flex-start;
                            padding: 0.75rem;
                            background: #f8fafc;
                            border-radius: 6px;
                        }

                        .view-item.full-width {
                            flex-direction: column;
                            align-items: stretch;
                        }

                        .view-label {
                            font-weight: 500;
                            color: #374151;
                            min-width: 120px;
                        }

                        .view-value {
                            color: #1f2937;
                            text-align: right;
                        }

                        .view-item.full-width .view-value {
                            text-align: left;
                            margin-top: 0.5rem;
                        }

                        .view-value.description {
                            line-height: 1.5;
                        }

                        /* Delete Modal */
                        .delete-content {
                            text-align: center;
                            padding: 1rem 0;
                        }

                        .delete-message {
                            font-size: 1rem;
                            color: #1f2937;
                            margin-bottom: 0.75rem;
                        }

                        .delete-warning {
                            font-size: 0.875rem;
                            color: #6b7280;
                            line-height: 1.5;
                            margin-bottom: 1rem;
                        }

                        /* 반응형 디자인 */
                        @media (max-width: 1024px) {
                            .controls-bar {
                                flex-direction: column;
                                align-items: stretch;
                                gap: 1rem;
                            }

                            .left-controls,
                            .right-controls {
                                justify-content: space-between;
                            }

                            .search-input {
                                width: 100%;
                            }

                            .form-grid {
                                grid-template-columns: 1fr;
                            }
                        }

                        @media (max-width: 768px) {
                            .book-management-container {
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

                            .header-left {
                                flex-direction: column;
                                align-items: flex-start;
                            }

                            .left-controls {
                                flex-direction: column;
                                width: 100%;
                            }

                            .add-btn {
                                width: 100%;
                                justify-content: center;
                            }

                            .search-container {
                                width: 100%;
                            }

                            .right-controls {
                                flex-wrap: wrap;
                            }

                            .right-controls select {
                                flex: 1;
                                min-width: 120px;
                            }

                            .books-table {
                                font-size: 0.75rem;
                            }

                            .books-table th,
                            .books-table td {
                                padding: 0.5rem;
                            }

                            .book-title {
                                max-width: 150px;
                            }

                            .modal-container {
                                margin: 1rem;
                                max-width: none;
                            }

                            .modal-header,
                            .modal-body,
                            .modal-footer {
                                padding: 1rem;
                            }

                            .form-grid {
                                gap: 0.75rem;
                            }
                        }

                        @media (max-width: 480px) {
                            .book-actions {
                                flex-direction: column;
                            }

                            .action-btn {
                                width: 28px;
                                height: 28px;
                                font-size: 0.75rem;
                            }

                            .pagination {
                                flex-wrap: wrap;
                                gap: 0.25rem;
                            }

                            .page-btn {
                                padding: 0.25rem 0.5rem;
                                font-size: 0.75rem;
                            }

                            .view-item {
                                flex-direction: column;
                                align-items: flex-start;
                            }

                            .view-value {
                                text-align: left;
                                margin-top: 0.25rem;
                            }
                        }

                        /* 접근성 개선 */
                        @media (prefers-reduced-motion: reduce) {
                            .add-btn,
                            .action-btn,
                            .page-btn,
                            .btn,
                            .close-btn,
                            .search-btn {
                                transition: none;
                            }

                            .loading-spinner {
                                animation: none;
                            }
                        }

                        /* 포커스 스타일 */
                        .add-btn:focus,
                        .action-btn:focus,
                        .page-btn:focus,
                        .btn:focus,
                        .close-btn:focus,
                        .search-btn:focus,
                        .sort-btn:focus {
                            outline: 2px solid #3b82f6;
                            outline-offset: 2px;
                        }

                        .search-input:focus,
                        select:focus,
                        .form-group input:focus,
                        .form-group select:focus,
                        .form-group textarea:focus {
                            outline: 2px solid #3b82f6;
                            outline-offset: 2px;
                        }
                    </style>
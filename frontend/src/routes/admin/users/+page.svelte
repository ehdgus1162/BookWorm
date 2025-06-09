<!-- src/routes/admin/users/+page.svelte -->
<script>
    import { onMount } from 'svelte';
    import { fade, scale } from 'svelte/transition';

    // API 기본 설정
    const API_BASE_URL = 'http://localhost:8080/api';

    // 사용자 데이터
    let users = [];
    let loading = false;
    let error = null;
    let totalPages = 0;
    let currentPage = 0;
    let pageSize = 20;

    // 모달 상태 관리
    let showAddModal = false;
    let showEditModal = false;
    let showViewModal = false;
    let showDeleteModal = false;
    let selectedUser = null;

    // ✅ 폼 데이터 - 서버 SignUpRequest DTO에 정확히 맞춤
    let userForm = {
        email: '',
        password: '',
        firstName: '',
        lastName: '',
        street: '',
        city: '',
        country: '',
        state: '',
        phoneNumber: ''
    };

    // 검색
    let searchTerm = '';
    let searchTimeout;

    // 알림 상태
    let notification = null;

    // 계산된 데이터
    $: filteredUsers = users.filter(user => {
        if (!searchTerm) return true;
        const searchLower = searchTerm.toLowerCase();
        return user.firstName?.toLowerCase().includes(searchLower) ||
            user.lastName?.toLowerCase().includes(searchLower) ||
            user.email?.toLowerCase().includes(searchLower);
    });

    // 공통 API 호출 함수 - 에러 처리 개선
    async function makeApiRequest(url, options = {}) {
        try {
            const response = await fetch(url, {
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                },
                ...options
            });

            const data = await response.json();

            if (!response.ok) {
                // ✅ validation 에러 시 상세 정보 출력
                if (response.status === 400 && data.data) {
                    console.error('Validation 에러:', data.data);
                    const errorMessages = Object.entries(data.data)
                        .map(([field, message]) => `${field}: ${message}`)
                        .join('\n');
                    throw new Error(`입력값 오류:\n${errorMessages}`);
                }
                throw new Error(data.message || `HTTP ${response.status}: ${response.statusText}`);
            }

            return data;
        } catch (err) {
            console.error('API 요청 실패:', err);
            console.error('요청 URL:', url);
            if (options.body) {
                console.error('요청 데이터:', options.body);
            }
            throw err;
        }
    }

    // 알림 표시 함수
    function showNotification(message, type = 'success') {
        notification = { message, type };
        setTimeout(() => {
            notification = null;
        }, 5000);
    }

    // 사용자 목록 조회 (페이징)
    async function fetchUsers(page = 0, size = 20) {
        loading = true;
        error = null;

        try {
            const data = await makeApiRequest(
                `${API_BASE_URL}/users/paged?page=${page}&size=${size}&sort=createdAt,desc`
            );

            if (data.success && data.data) {
                users = data.data.content || [];
                totalPages = data.data.totalPages || 0;
                currentPage = data.data.number || 0;
                console.log('사용자 목록 조회 성공:', users.length, '명');
            } else {
                throw new Error(data.message || '사용자 목록 조회에 실패했습니다.');
            }
        } catch (err) {
            error = '사용자 목록을 불러오는데 실패했습니다: ' + err.message;
            console.error('사용자 목록 조회 실패:', err);
            users = [];
        } finally {
            loading = false;
        }
    }

    // ✅ 사용자 생성 - 서버 SignUpRequest 구조에 정확히 맞춤
    async function createUser(userData) {
        loading = true;
        try {
            // 서버 SignUpRequest DTO 구조에 맞춰 데이터 전송
            const requestData = {
                email: userData.email?.trim() || '',
                password: userData.password?.trim() || '',
                firstName: userData.firstName?.trim() || '',
                lastName: userData.lastName?.trim() || '',
                street: userData.street?.trim() || '기본 주소',  // 필수 필드에 기본값
                city: userData.city?.trim() || '서울',           // 필수 필드에 기본값
                country: userData.country?.trim() || '대한민국',  // 필수 필드에 기본값
                state: userData.state?.trim() || '',             // 선택 필드
                phoneNumber: userData.phoneNumber?.trim() || ''
            };

            console.log('사용자 생성 요청 데이터:', requestData);

            const data = await makeApiRequest(`${API_BASE_URL}/auth/signup`, {
                method: 'POST',
                body: JSON.stringify(requestData)
            });

            if (data.success) {
                console.log('사용자 생성 성공:', data.data);
                showNotification('사용자가 성공적으로 추가되었습니다.', 'success');
                await fetchUsers(currentPage, pageSize);
                return true;
            } else {
                throw new Error(data.message || '사용자 생성에 실패했습니다.');
            }
        } catch (err) {
            const errorMessage = '사용자 생성에 실패했습니다: ' + err.message;
            error = errorMessage;
            showNotification(errorMessage, 'error');
            console.error('사용자 생성 실패:', err);
            return false;
        } finally {
            loading = false;
        }
    }

    // ✅ 사용자 수정 - 서버 SignUpRequest 구조에 정확히 맞춤
    async function updateUser(userId, userData) {
        loading = true;
        try {
            // 먼저 기존 사용자 데이터 가져오기
            const existingUser = await fetchUserById(userId);
            if (!existingUser) {
                throw new Error('기존 사용자 정보를 찾을 수 없습니다.');
            }

            // 기존 데이터와 새 데이터 병합
            const requestData = {
                email: userData.email?.trim() || existingUser.email,
                password: userData.password?.trim() || 'TempPassword123!', // 임시 비밀번호
                firstName: userData.firstName?.trim() || existingUser.firstName,
                lastName: userData.lastName?.trim() || existingUser.lastName,
                street: userData.street?.trim() || existingUser.street || '기본 주소',
                city: userData.city?.trim() || existingUser.city || '서울',
                country: userData.country?.trim() || existingUser.country || '대한민국',
                state: userData.state?.trim() || existingUser.state || '',
                phoneNumber: userData.phoneNumber?.trim() || existingUser.phoneNumber || ''
            };

            console.log('사용자 수정 요청 데이터:', requestData);

            const data = await makeApiRequest(`${API_BASE_URL}/users/${userId}`, {
                method: 'PUT',
                body: JSON.stringify(requestData)
            });

            if (data.success) {
                console.log('사용자 수정 성공:', data.data);
                showNotification('사용자 정보가 성공적으로 수정되었습니다.', 'success');
                await fetchUsers(currentPage, pageSize);
                return true;
            } else {
                throw new Error(data.message || '사용자 수정에 실패했습니다.');
            }
        } catch (err) {
            const errorMessage = '사용자 수정에 실패했습니다: ' + err.message;
            error = errorMessage;
            showNotification(errorMessage, 'error');
            console.error('사용자 수정 실패:', err);
            return false;
        } finally {
            loading = false;
        }
    }

    // 사용자 삭제
    async function deleteUserApi(userId) {
        loading = true;
        try {
            const data = await makeApiRequest(`${API_BASE_URL}/users/${userId}`, {
                method: 'DELETE'
            });

            if (data.success) {
                console.log('사용자 삭제 성공');
                showNotification('사용자가 성공적으로 삭제되었습니다.', 'success');
                await fetchUsers(currentPage, pageSize);
                return true;
            } else {
                throw new Error(data.message || '사용자 삭제에 실패했습니다.');
            }
        } catch (err) {
            const errorMessage = '사용자 삭제에 실패했습니다: ' + err.message;
            error = errorMessage;
            showNotification(errorMessage, 'error');
            console.error('사용자 삭제 실패:', err);
            return false;
        } finally {
            loading = false;
        }
    }

    // 특정 사용자 조회
    async function fetchUserById(userId) {
        try {
            const data = await makeApiRequest(`${API_BASE_URL}/users/${userId}`);
            if (data.success && data.data) {
                return data.data;
            } else {
                throw new Error(data.message || '사용자 정보를 찾을 수 없습니다.');
            }
        } catch (err) {
            const errorMessage = '사용자 정보를 불러오는데 실패했습니다: ' + err.message;
            error = errorMessage;
            showNotification(errorMessage, 'error');
            console.error('사용자 조회 실패:', err);
            return null;
        }
    }

    // 이메일 중복 확인
    async function checkEmailExists(email) {
        try {
            const data = await makeApiRequest(
                `${API_BASE_URL}/auth/check-email?email=${encodeURIComponent(email)}`
            );
            return data.success && data.data === true;
        } catch (err) {
            console.error('이메일 중복 확인 실패:', err);
            return true;
        }
    }

    // 컴포넌트 마운트 시 실행
    onMount(() => {
        console.log('User management loaded');
        fetchUsers(0, pageSize);
    });

    // 검색어 변경 시 디바운싱
    $: {
        if (searchTimeout) clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            console.log('검색어:', searchTerm);
        }, 300);
    }

    // CRUD 함수들
    function openAddModal() {
        resetForm();
        showAddModal = true;
    }

    async function openEditModal(user) {
        selectedUser = user;
        const latestUser = await fetchUserById(user.id);
        if (latestUser) {
            // ✅ 서버에서 받은 데이터를 폼 구조에 맞게 설정
            userForm = {
                email: latestUser.email || '',
                password: '', // 보안상 비밀번호는 비움
                firstName: latestUser.firstName || '',
                lastName: latestUser.lastName || '',
                street: latestUser.street || '',
                city: latestUser.city || '',
                country: latestUser.country || '',
                state: latestUser.state || '',
                phoneNumber: latestUser.phoneNumber || ''
            };
        }
        showEditModal = true;
    }

    async function openViewModal(user) {
        selectedUser = user;
        const latestUser = await fetchUserById(user.id);
        if (latestUser) {
            selectedUser = latestUser;
        }
        showViewModal = true;
    }

    function openDeleteModal(user) {
        selectedUser = user;
        showDeleteModal = true;
    }

    function closeAllModals() {
        showAddModal = false;
        showEditModal = false;
        showViewModal = false;
        showDeleteModal = false;
        selectedUser = null;
        resetForm();
        error = null;
    }

    // ✅ 폼 리셋 - 새로운 필드 구조에 맞춤
    function resetForm() {
        userForm = {
            email: '',
            password: '',
            firstName: '',
            lastName: '',
            street: '',
            city: '',
            country: '',
            state: '',
            phoneNumber: ''
        };
    }

    async function addUser() {
        if (!await validateForm(true)) return;

        const success = await createUser(userForm);
        if (success) {
            closeAllModals();
        }
    }

    async function updateUserAction() {
        if (!await validateForm(false)) return;

        const success = await updateUser(selectedUser.id, userForm);
        if (success) {
            closeAllModals();
        }
    }

    async function deleteUserAction() {
        const success = await deleteUserApi(selectedUser.id);
        if (success) {
            closeAllModals();
        }
    }

    // ✅ 폼 검증 - 새로운 필드들에 맞춤
    async function validateForm(isCreate = true) {
        // 이메일 검증
        if (!userForm.email.trim()) {
            showNotification('이메일을 입력해주세요.', 'error');
            return false;
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(userForm.email)) {
            showNotification('올바른 이메일 형식을 입력해주세요.', 'error');
            return false;
        }

        // 이름 검증
        if (!userForm.firstName.trim()) {
            showNotification('이름을 입력해주세요.', 'error');
            return false;
        }

        if (!userForm.lastName.trim()) {
            showNotification('성을 입력해주세요.', 'error');
            return false;
        }

        // 비밀번호 검증
        if (isCreate && !userForm.password.trim()) {
            showNotification('비밀번호를 입력해주세요.', 'error');
            return false;
        }

        if (userForm.password && userForm.password.length < 8) {
            showNotification('비밀번호는 최소 8자 이상이어야 합니다.', 'error');
            return false;
        }

        // 주소 검증 (필수 필드들)
        if (!userForm.street.trim()) {
            userForm.street = '기본 주소'; // 기본값 설정
        }

        if (!userForm.city.trim()) {
            userForm.city = '서울'; // 기본값 설정
        }

        if (!userForm.country.trim()) {
            userForm.country = '대한민국'; // 기본값 설정
        }

        // 이메일 중복 확인 (생성 시에만)
        if (isCreate) {
            const emailExists = await checkEmailExists(userForm.email);
            if (emailExists) {
                showNotification('이미 사용 중인 이메일입니다.', 'error');
                return false;
            }
        }

        return true;
    }

    // 페이지 변경
    function changePage(newPage) {
        if (newPage >= 0 && newPage < totalPages && !loading) {
            fetchUsers(newPage, pageSize);
        }
    }

    // 키보드 이벤트
    function handleKeydown(event) {
        if (event.key === 'Escape') {
            closeAllModals();
        }
    }

    // 유틸리티 함수들
    function getCurrentTime() {
        const now = new Date();
        return now.toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
        });
    }

    function getCurrentDate() {
        const now = new Date();
        return now.toLocaleDateString('en-US', {
            month: 'short',
            day: '2-digit',
            year: 'numeric'
        });
    }

    function getRoleDisplayName(role) {
        switch(role) {
            case 'USER': return '일반 사용자';
            case 'ADMIN': return '관리자';
            default: return role || 'N/A';
        }
    }

    function formatDate(dateString) {
        if (!dateString) return 'N/A';
        try {
            const date = new Date(dateString);
            return date.toLocaleDateString('ko-KR', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit'
            });
        } catch (err) {
            return 'N/A';
        }
    }

    // ✅ 전체 이름 표시용 헬퍼 함수
    function getFullName(user) {
        return `${user.firstName || ''} ${user.lastName || ''}`.trim();
    }
</script>

<svelte:head>
    <title>User Management - BookWorm Admin</title>
</svelte:head>

<svelte:window on:keydown={handleKeydown} />

<div class="user-management-container">
    <!-- 알림 메시지 -->
    {#if notification}
        <div class="notification notification-{notification.type}" transition:fade>
            <span class="notification-icon">
                {#if notification.type === 'success'}✅
                {:else if notification.type === 'error'}❌
                {:else if notification.type === 'warning'}⚠️
                {:else}ℹ️{/if}
            </span>
            {notification.message}
            <button class="notification-close" on:click={() => notification = null}>✕</button>
        </div>
    {/if}

    <!-- 페이지 헤더 -->
    <div class="page-header">
        <div class="header-left">
            <h1 class="page-title">사용자 관리</h1>
            <p class="page-subtitle">시스템 사용자를 관리할 수 있습니다.</p>
        </div>
        <div class="header-right">
            <span class="current-time">{getCurrentTime()}</span>
            <span class="current-date">{getCurrentDate()}</span>
        </div>
    </div>

    <!-- 에러 메시지 -->
    {#if error}
        <div class="error-message" transition:fade>
            <span class="error-icon">⚠️</span>
            {error}
            <button class="error-close" on:click={() => error = null}>✕</button>
        </div>
    {/if}

    <!-- 로딩 상태 -->
    {#if loading}
        <div class="loading-overlay" transition:fade>
            <div class="loading-spinner">⏳</div>
            <span>처리 중...</span>
        </div>
    {/if}

    <!-- 사용자 관리 섹션 -->
    <div class="management-section">
        <div class="section-header">
            <h2 class="section-title">
                사용자 목록
                {#if users.length > 0}
                    <span class="user-count">({users.length}명)</span>
                {/if}
            </h2>
            <div class="section-controls">
                <button class="add-user-btn" on:click={openAddModal} disabled={loading}>
                    <span class="btn-icon">👤➕</span>
                    사용자 추가
                </button>
                <div class="search-container">
                    <input
                            type="text"
                            placeholder="이름 또는 이메일로 검색"
                            bind:value={searchTerm}
                            class="search-input"
                            disabled={loading}
                    />
                    <span class="search-icon">🔍</span>
                </div>
            </div>
        </div>

        <!-- 사용자 테이블 -->
        <div class="table-container">
            <table class="users-table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>이름</th>
                    <th>이메일</th>
                    <th>전화번호</th>
                    <th>주소</th>
                    <th>역할</th>
                    <th>생성일</th>
                    <th>작업</th>
                </tr>
                </thead>
                <tbody>
                {#each filteredUsers as user (user.id)}
                    <tr class="user-row" in:fade={{ duration: 200 }}>
                        <td class="user-id" data-label="ID">{user.id}</td>
                        <td class="user-name" data-label="이름">{getFullName(user)}</td>
                        <td class="user-email" data-label="이메일">{user.email}</td>
                        <td class="user-phone" data-label="전화번호">{user.phoneNumber || 'N/A'}</td>
                        <td class="user-address" data-label="주소">
                            {#if user.street || user.city || user.country}
                                <div class="address-info">
                                    <div class="address-line">{user.street || ''}</div>
                                    <div class="address-city">{user.city || ''} {user.state || ''}</div>
                                    <div class="address-country">{user.country || ''}</div>
                                </div>
                            {:else}
                                N/A
                            {/if}
                        </td>
                        <td class="user-role" data-label="역할">
                                <span class="role-badge role-{user.role?.toLowerCase() || 'user'}">
                                    {getRoleDisplayName(user.role)}
                                </span>
                        </td>
                        <td class="user-created" data-label="생성일">
                            {formatDate(user.createdDate)}
                        </td>
                        <td class="user-actions" data-label="작업">
                            <button
                                    class="action-btn view"
                                    on:click={() => openViewModal(user)}
                                    title="상세보기"
                                    disabled={loading}
                            >
                                👁️
                            </button>
                            <button
                                    class="action-btn edit"
                                    on:click={() => openEditModal(user)}
                                    title="수정"
                                    disabled={loading}
                            >
                                ✏️
                            </button>
                            <button
                                    class="action-btn delete"
                                    on:click={() => openDeleteModal(user)}
                                    title="삭제"
                                    disabled={loading}
                            >
                                🗑️
                            </button>
                        </td>
                    </tr>
                {:else}
                    <tr>
                        <td colspan="8" class="no-data">
                            {#if loading}
                                데이터를 불러오는 중...
                            {:else if searchTerm}
                                '{searchTerm}'에 대한 검색 결과가 없습니다.
                            {:else}
                                등록된 사용자가 없습니다.
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
                        on:click={() => changePage(currentPage - 1)}
                        disabled={currentPage === 0 || loading}
                >
                    이전
                </button>

                <span class="page-info">
                    페이지 {currentPage + 1} / {totalPages}
                </span>

                <button
                        class="page-btn"
                        on:click={() => changePage(currentPage + 1)}
                        disabled={currentPage >= totalPages - 1 || loading}
                >
                    다음
                </button>
            </div>
        {/if}
    </div>
</div>

<!-- ✅ Add User Modal - 새로운 필드 구조 -->
{#if showAddModal}
    <div class="modal-overlay" on:click={closeAllModals} transition:fade>
        <div class="modal-container add-user-modal" on:click|stopPropagation transition:scale>
            <div class="modal-header">
                <div class="modal-title">
                    <span class="modal-icon">👤</span>
                    사용자 추가
                </div>
                <button class="close-btn" on:click={closeAllModals}>✕</button>
            </div>

            <div class="modal-body">
                <div class="form-container">
                    <!-- 개인 정보 섹션 -->
                    <div class="form-section">
                        <h4 class="section-title">기본 정보</h4>

                        <div class="form-row">
                            <div class="form-group half-width">
                                <label class="form-label">이름 *</label>
                                <input
                                        type="text"
                                        placeholder="홍"
                                        bind:value={userForm.firstName}
                                        class="form-input"
                                        disabled={loading}
                                />
                            </div>
                            <div class="form-group half-width">
                                <label class="form-label">성 *</label>
                                <input
                                        type="text"
                                        placeholder="길동"
                                        bind:value={userForm.lastName}
                                        class="form-input"
                                        disabled={loading}
                                />
                            </div>
                        </div>

                        <div class="form-group full-width">
                            <label class="form-label">이메일 *</label>
                            <input
                                    type="email"
                                    placeholder="user@example.com"
                                    bind:value={userForm.email}
                                    class="form-input"
                                    disabled={loading}
                            />
                        </div>

                        <div class="form-group full-width">
                            <label class="form-label">전화번호</label>
                            <input
                                    type="tel"
                                    placeholder="010-1234-5678"
                                    bind:value={userForm.phoneNumber}
                                    class="form-input"
                                    disabled={loading}
                            />
                        </div>

                        <div class="form-group full-width">
                            <label class="form-label">비밀번호 *</label>
                            <input
                                    type="password"
                                    placeholder="최소 8자 이상"
                                    bind:value={userForm.password}
                                    class="form-input"
                                    disabled={loading}
                            />
                        </div>
                    </div>

                    <!-- 주소 정보 섹션 -->
                    <div class="form-section">
                        <h4 class="section-title">주소 정보</h4>

                        <div class="form-group full-width">
                            <label class="form-label">도로명 주소 *</label>
                            <input
                                    type="text"
                                    placeholder="예: 강남구 테헤란로 123"
                                    bind:value={userForm.street}
                                    class="form-input"
                                    disabled={loading}
                            />
                        </div>

                        <div class="form-row">
                            <div class="form-group half-width">
                                <label class="form-label">도시 *</label>
                                <input
                                        type="text"
                                        placeholder="서울"
                                        bind:value={userForm.city}
                                        class="form-input"
                                        disabled={loading}
                                />
                            </div>
                            <div class="form-group half-width">
                                <label class="form-label">국가 *</label>
                                <input
                                        type="text"
                                        placeholder="대한민국"
                                        bind:value={userForm.country}
                                        class="form-input"
                                        disabled={loading}
                                />
                            </div>
                        </div>

                        <div class="form-group full-width">
                            <label class="form-label">주/도 (선택사항)</label>
                            <input
                                    type="text"
                                    placeholder="예: 경기도"
                                    bind:value={userForm.state}
                                    class="form-input"
                                    disabled={loading}
                            />
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button class="btn secondary" on:click={closeAllModals} disabled={loading}>취소</button>
                <button class="btn primary" on:click={addUser} disabled={loading}>
                    {loading ? '처리 중...' : '추가'}
                </button>
            </div>
        </div>
    </div>
{/if}

<!-- ✅ Update User Modal - 새로운 필드 구조 -->
{#if showEditModal}
    <div class="modal-overlay" on:click={closeAllModals} transition:fade>
        <div class="modal-container update-user-modal" on:click|stopPropagation transition:scale>
            <div class="modal-header">
                <div class="modal-title">
                    <span class="modal-icon">✏️</span>
                    사용자 수정
                </div>
                <button class="close-btn" on:click={closeAllModals}>✕</button>
            </div>

            <div class="modal-body">
                <div class="form-container">
                    <!-- 개인 정보 섹션 -->
                    <div class="form-section">
                        <h4 class="section-title">기본 정보</h4>

                        <div class="form-row">
                            <div class="form-group half-width">
                                <label class="form-label">이름 *</label>
                                <input
                                        type="text"
                                        placeholder="홍"
                                        bind:value={userForm.firstName}
                                        class="form-input"
                                        disabled={loading}
                                />
                            </div>
                            <div class="form-group half-width">
                                <label class="form-label">성 *</label>
                                <input
                                        type="text"
                                        placeholder="길동"
                                        bind:value={userForm.lastName}
                                        class="form-input"
                                        disabled={loading}
                                />
                            </div>
                        </div>

                        <div class="form-group full-width">
                            <label class="form-label">이메일 *</label>
                            <input
                                    type="email"
                                    placeholder="user@example.com"
                                    bind:value={userForm.email}
                                    class="form-input"
                                    disabled={loading}
                            />
                        </div>

                        <div class="form-group full-width">
                            <label class="form-label">전화번호</label>
                            <input
                                    type="tel"
                                    placeholder="010-1234-5678"
                                    bind:value={userForm.phoneNumber}
                                    class="form-input"
                                    disabled={loading}
                            />
                        </div>

                        <div class="form-group full-width">
                            <label class="form-label">비밀번호</label>
                            <input
                                    type="password"
                                    placeholder="변경하지 않으려면 비워두세요"
                                    bind:value={userForm.password}
                                    class="form-input"
                                    disabled={loading}
                            />
                            <small class="form-help">비밀번호를 변경하지 않으려면 비워두세요.</small>
                        </div>
                    </div>

                    <!-- 주소 정보 섹션 -->
                    <div class="form-section">
                        <h4 class="section-title">주소 정보</h4>

                        <div class="form-group full-width">
                            <label class="form-label">도로명 주소 *</label>
                            <input
                                    type="text"
                                    placeholder="예: 강남구 테헤란로 123"
                                    bind:value={userForm.street}
                                    class="form-input"
                                    disabled={loading}
                            />
                        </div>

                        <div class="form-row">
                            <div class="form-group half-width">
                                <label class="form-label">도시 *</label>
                                <input
                                        type="text"
                                        placeholder="서울"
                                        bind:value={userForm.city}
                                        class="form-input"
                                        disabled={loading}
                                />
                            </div>
                            <div class="form-group half-width">
                                <label class="form-label">국가 *</label>
                                <input
                                        type="text"
                                        placeholder="대한민국"
                                        bind:value={userForm.country}
                                        class="form-input"
                                        disabled={loading}
                                />
                            </div>
                        </div>

                        <div class="form-group full-width">
                            <label class="form-label">주/도 (선택사항)</label>
                            <input
                                    type="text"
                                    placeholder="예: 경기도"
                                    bind:value={userForm.state}
                                    class="form-input"
                                    disabled={loading}
                            />
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button class="btn secondary" on:click={closeAllModals} disabled={loading}>취소</button>
                <button class="btn primary" on:click={updateUserAction} disabled={loading}>
                    {loading ? '처리 중...' : '수정'}
                </button>
            </div>
        </div>
    </div>
{/if}

<!-- ✅ View User Modal - 새로운 필드 구조 -->
{#if showViewModal && selectedUser}
    <div class="modal-overlay" on:click={closeAllModals} transition:fade>
        <div class="modal-container view-user-modal" on:click|stopPropagation transition:scale>
            <div class="modal-header">
                <div class="modal-title">
                    <span class="modal-icon">👁️</span>
                    사용자 정보
                </div>
                <button class="close-btn" on:click={closeAllModals}>✕</button>
            </div>

            <div class="modal-body">
                <div class="view-content">
                    <!-- 기본 정보 -->
                    <div class="view-section">
                        <h4 class="view-section-title">기본 정보</h4>

                        <div class="view-item">
                            <span class="view-label">사용자 ID:</span>
                            <span class="view-value">{selectedUser.id}</span>
                        </div>

                        <div class="view-item">
                            <span class="view-label">이름:</span>
                            <span class="view-value">{selectedUser.firstName || 'N/A'}</span>
                        </div>

                        <div class="view-item">
                            <span class="view-label">성:</span>
                            <span class="view-value">{selectedUser.lastName || 'N/A'}</span>
                        </div>

                        <div class="view-item">
                            <span class="view-label">전체 이름:</span>
                            <span class="view-value">{getFullName(selectedUser)}</span>
                        </div>

                        <div class="view-item">
                            <span class="view-label">이메일:</span>
                            <span class="view-value">{selectedUser.email}</span>
                        </div>

                        <div class="view-item">
                            <span class="view-label">전화번호:</span>
                            <span class="view-value">{selectedUser.phoneNumber || 'N/A'}</span>
                        </div>

                        <div class="view-item">
                            <span class="view-label">역할:</span>
                            <span class="view-value role-badge role-{selectedUser.role?.toLowerCase() || 'user'}">
                                {getRoleDisplayName(selectedUser.role)}
                            </span>
                        </div>
                    </div>

                    <!-- 주소 정보 -->
                    <div class="view-section">
                        <h4 class="view-section-title">주소 정보</h4>

                        <div class="view-item">
                            <span class="view-label">도로명 주소:</span>
                            <span class="view-value">{selectedUser.street || 'N/A'}</span>
                        </div>

                        <div class="view-item">
                            <span class="view-label">도시:</span>
                            <span class="view-value">{selectedUser.city || 'N/A'}</span>
                        </div>

                        <div class="view-item">
                            <span class="view-label">주/도:</span>
                            <span class="view-value">{selectedUser.state || 'N/A'}</span>
                        </div>

                        <div class="view-item">
                            <span class="view-label">국가:</span>
                            <span class="view-value">{selectedUser.country || 'N/A'}</span>
                        </div>

                        <div class="view-item">
                            <span class="view-label">전체 주소:</span>
                            <span class="view-value">
                                {#if selectedUser.street || selectedUser.city || selectedUser.country}
                                    {selectedUser.street || ''} {selectedUser.city || ''} {selectedUser.state || ''} {selectedUser.country || ''}
                                {:else}
                                    N/A
                                {/if}
                            </span>
                        </div>
                    </div>

                    <!-- 시스템 정보 -->
                    <div class="view-section">
                        <h4 class="view-section-title">시스템 정보</h4>

                        <div class="view-item">
                            <span class="view-label">생성일:</span>
                            <span class="view-value">
                                {selectedUser.createdAt ? new Date(selectedUser.createdAt).toLocaleString('ko-KR') : 'N/A'}
                            </span>
                        </div>

                        <div class="view-item">
                            <span class="view-label">수정일:</span>
                            <span class="view-value">
                                {selectedUser.updatedAt ? new Date(selectedUser.updatedAt).toLocaleString('ko-KR') : 'N/A'}
                            </span>
                        </div>

                        <div class="view-item">
                            <span class="view-label">마지막 로그인:</span>
                            <span class="view-value">
                                {selectedUser.lastLoginDate ? new Date(selectedUser.lastLoginDate).toLocaleString('ko-KR') : 'N/A'}
                            </span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button class="btn primary" on:click={closeAllModals}>닫기</button>
            </div>
        </div>
    </div>
{/if}

<!-- Delete Confirmation Modal -->
{#if showDeleteModal && selectedUser}
    <div class="modal-overlay" on:click={closeAllModals} transition:fade>
        <div class="modal-container delete-confirmation-modal" on:click|stopPropagation transition:scale>
            <div class="modal-header">
                <div class="modal-title">
                    <span class="modal-icon">🗑️</span>
                    삭제 확인
                </div>
                <button class="close-btn" on:click={closeAllModals}>✕</button>
            </div>

            <div class="modal-body">
                <div class="delete-content">
                    <p class="delete-message">
                        사용자 <strong>{getFullName(selectedUser)}</strong>({selectedUser.email})을(를) 정말 삭제하시겠습니까?<br>
                        <span class="delete-warning">이 작업은 되돌릴 수 없습니다.</span>
                    </p>
                </div>
            </div>

            <div class="modal-footer">
                <button class="btn secondary" on:click={closeAllModals} disabled={loading}>취소</button>
                <button class="btn danger" on:click={deleteUserAction} disabled={loading}>
                    {loading ? '삭제 중...' : '삭제'}
                </button>
            </div>
        </div>
    </div>
{/if}

<style>
    /* 기본 컨테이너 */
    .user-management-container {
        padding: 2rem;
        background: #f8fafc;
        min-height: 100vh;
        font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
    }

    /* 페이지 헤더 */
    .page-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 2rem;
        padding-bottom: 1rem;
        border-bottom: 1px solid #e2e8f0;
    }

    .header-left .page-title {
        font-size: 1.875rem;
        font-weight: 700;
        color: #1e293b;
        margin: 0 0 0.25rem 0;
    }

    .header-left .page-subtitle {
        font-size: 0.875rem;
        color: #64748b;
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

    /* 알림 메시지 */
    .notification {
        position: fixed;
        top: 1rem;
        right: 1rem;
        background: white;
        border-radius: 8px;
        padding: 1rem;
        box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
        border-left: 4px solid;
        z-index: 1100;
        max-width: 400px;
        display: flex;
        align-items: center;
        gap: 0.75rem;
    }

    .notification-success {
        border-left-color: #10b981;
    }

    .notification-error {
        border-left-color: #dc2626;
    }

    .notification-warning {
        border-left-color: #d97706;
    }

    .notification-info {
        border-left-color: #3b82f6;
    }

    .notification-close {
        margin-left: auto;
        background: none;
        border: none;
        color: #6b7280;
        cursor: pointer;
        font-size: 14px;
        padding: 0;
        width: 20px;
        height: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .notification-close:hover {
        color: #374151;
    }

    /* 에러 메시지 */
    .error-message {
        background-color: #fee;
        border: 1px solid #fcc;
        color: #c33;
        padding: 12px;
        border-radius: 8px;
        margin-bottom: 20px;
        display: flex;
        align-items: center;
        gap: 8px;
    }

    .error-close {
        margin-left: auto;
        background: none;
        border: none;
        color: #c33;
        cursor: pointer;
        font-size: 16px;
    }

    /* 로딩 오버레이 */
    .loading-overlay {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(0, 0, 0, 0.3);
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        z-index: 9999;
        gap: 10px;
        color: white;
        font-size: 18px;
    }

    .loading-spinner {
        font-size: 32px;
        animation: spin 1s linear infinite;
    }

    @keyframes spin {
        from { transform: rotate(0deg); }
        to { transform: rotate(360deg); }
    }

    /* 관리 섹션 */
    .management-section {
        background: white;
        border-radius: 12px;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
        border: 1px solid #f1f5f9;
        overflow: hidden;
    }

    .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1.5rem;
        border-bottom: 1px solid #f1f5f9;
        background: #f8fafc;
    }

    .section-title {
        font-size: 1.25rem;
        font-weight: 600;
        color: #1e293b;
        margin: 0;
        display: flex;
        align-items: center;
        gap: 0.5rem;
    }

    .user-count {
        font-size: 0.875rem;
        color: #64748b;
        font-weight: 400;
    }

    .section-controls {
        display: flex;
        align-items: center;
        gap: 1rem;
    }

    .add-user-btn {
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
        font-size: 0.875rem;
    }

    .add-user-btn:hover:not(:disabled) {
        background: #2563eb;
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
    }

    .add-user-btn:disabled {
        opacity: 0.6;
        cursor: not-allowed;
        transform: none;
    }

    .search-container {
        position: relative;
    }

    .search-input {
        padding: 0.75rem 1rem 0.75rem 2.5rem;
        border: 1px solid #d1d5db;
        border-radius: 8px;
        width: 280px;
        font-size: 0.875rem;
        transition: all 0.2s ease;
    }

    .search-input:focus {
        outline: none;
        border-color: #3b82f6;
        box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
    }

    .search-input:disabled {
        background: #f9fafb;
        color: #6b7280;
    }

    .search-icon {
        position: absolute;
        left: 0.75rem;
        top: 50%;
        transform: translateY(-50%);
        color: #6b7280;
        pointer-events: none;
    }

    /* 테이블 */
    .table-container {
        overflow-x: auto;
        max-height: 70vh;
    }

    .users-table {
        width: 100%;
        border-collapse: collapse;
    }

    .users-table th {
        background: #f8fafc;
        padding: 1rem;
        text-align: left;
        font-weight: 600;
        color: #374151;
        border-bottom: 1px solid #e5e7eb;
        font-size: 0.875rem;
        position: sticky;
        top: 0;
        z-index: 10;
    }

    .users-table td {
        padding: 1rem;
        border-bottom: 1px solid #f3f4f6;
        color: #1f2937;
        font-size: 0.875rem;
        vertical-align: middle;
    }

    .user-row {
        transition: background-color 0.2s ease;
    }

    .user-row:hover {
        background: #f9fafb;
    }

    .user-id {
        font-weight: 500;
        color: #6b7280;
        font-family: 'JetBrains Mono', monospace;
    }

    .user-name {
        font-weight: 500;
        color: #1f2937;
    }

    .user-email {
        color: #6b7280;
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.8rem;
    }

    .user-phone {
        color: #6b7280;
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.8rem;
    }

    .user-actions {
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
        opacity: 0.5;
        cursor: not-allowed;
    }

    .action-btn.view {
        background: #eff6ff;
        color: #1d4ed8;
    }

    .action-btn.view:hover:not(:disabled) {
        background: #dbeafe;
        transform: translateY(-1px);
    }

    .action-btn.edit {
        background: #fef3c7;
        color: #d97706;
    }

    .action-btn.edit:hover:not(:disabled) {
        background: #fde68a;
        transform: translateY(-1px);
    }

    .action-btn.delete {
        background: #fee2e2;
        color: #dc2626;
    }

    .action-btn.delete:hover:not(:disabled) {
        background: #fecaca;
        transform: translateY(-1px);
    }

    /* 역할 배지 */
    .role-badge {
        display: inline-flex;
        align-items: center;
        padding: 0.25rem 0.75rem;
        border-radius: 12px;
        font-size: 0.75rem;
        font-weight: 500;
        text-transform: uppercase;
        letter-spacing: 0.025em;
    }

    .role-user {
        background-color: #e0f2fe;
        color: #0277bd;
    }

    .role-admin {
        background-color: #fce4ec;
        color: #c2185b;
    }

    /* 데이터 없음 상태 */
    .no-data {
        text-align: center;
        color: #6b7280;
        font-style: italic;
        padding: 3rem;
        background: #f9fafb;
    }

    /* 페이지네이션 */
    .pagination {
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 1rem;
        padding: 1.5rem;
        border-top: 1px solid #f1f5f9;
        background: #f8fafc;
    }

    .page-btn {
        padding: 0.5rem 1rem;
        border: 1px solid #d1d5db;
        background: white;
        border-radius: 6px;
        cursor: pointer;
        transition: all 0.2s ease;
        font-size: 0.875rem;
        color: #374151;
    }

    .page-btn:hover:not(:disabled) {
        background-color: #f3f4f6;
        border-color: #9ca3af;
    }

    .page-btn:disabled {
        opacity: 0.5;
        cursor: not-allowed;
    }

    .page-info {
        font-weight: 500;
        color: #6b7280;
        font-size: 0.875rem;
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
        max-width: 500px;
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
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .close-btn:hover {
        background: #e5e7eb;
        color: #374151;
    }

    .modal-body {
        padding: 1.5rem;
    }

    /* 폼 스타일 */
    .form-container {
        display: flex;
        flex-direction: column;
        gap: 1.25rem;
    }

    .form-group {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }

    .form-label {
        font-size: 0.875rem;
        font-weight: 500;
        color: #374151;
    }

    .form-input {
        padding: 0.75rem;
        border: 1px solid #d1d5db;
        border-radius: 6px;
        font-size: 0.875rem;
        transition: all 0.2s ease;
        background: white;
    }

    .form-input:focus {
        outline: none;
        border-color: #3b82f6;
        box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
    }

    .form-input:disabled {
        background: #f9fafb;
        color: #6b7280;
    }

    .form-input::placeholder {
        color: #9ca3af;
    }

    .form-help {
        font-size: 0.75rem;
        color: #6b7280;
        margin-top: 0.25rem;
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
        align-items: center;
        padding: 0.875rem;
        background: #f8fafc;
        border-radius: 8px;
        border: 1px solid #e2e8f0;
    }

    .view-label {
        font-weight: 500;
        color: #374151;
        font-size: 0.875rem;
    }

    .view-value {
        color: #1f2937;
        font-size: 0.875rem;
        text-align: right;
    }

    /* Delete Modal */
    .delete-content {
        text-align: center;
        padding: 1rem 0;
    }

    .delete-message {
        font-size: 1rem;
        color: #1f2937;
        line-height: 1.5;
        margin: 0;
    }

    .delete-warning {
        color: #dc2626;
        font-weight: 500;
        font-size: 0.875rem;
    }

    .modal-footer {
        display: flex;
        justify-content: flex-end;
        gap: 1rem;
        padding: 1.5rem;
        border-top: 1px solid #f1f5f9;
        background: #f8fafc;
    }

    .btn {
        padding: 0.75rem 1.5rem;
        border-radius: 6px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s ease;
        border: none;
        font-size: 0.875rem;
        min-width: 80px;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .btn:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }

    .btn.primary {
        background: #3b82f6;
        color: white;
    }

    .btn.primary:hover:not(:disabled) {
        background: #2563eb;
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
    }

    .btn.secondary {
        background: #f3f4f6;
        color: #374151;
        border: 1px solid #d1d5db;
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
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
    }

    /* 반응형 디자인 */
    @media (max-width: 1024px) {
        .section-header {
            flex-direction: column;
            align-items: stretch;
            gap: 1rem;
        }

        .section-controls {
            justify-content: space-between;
        }

        .search-input {
            width: 220px;
        }
    }

    @media (max-width: 768px) {
        .user-management-container {
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

        .section-controls {
            flex-direction: column;
            width: 100%;
            gap: 1rem;
        }

        .add-user-btn {
            width: 100%;
            justify-content: center;
        }

        .search-input {
            width: 100%;
        }

        .modal-container {
            margin: 1rem;
            max-width: none;
        }

        /* 모바일 테이블 스타일 */
        .users-table {
            display: block;
            overflow-x: auto;
            white-space: nowrap;
        }

        .users-table thead {
            display: none;
        }

        .users-table tbody {
            display: block;
        }

        .users-table tr {
            display: block;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            margin-bottom: 1rem;
            padding: 1rem;
            background: white;
        }

        .users-table td {
            display: block;
            border: none;
            padding: 0.5rem 0;
            text-align: left;
        }

        .users-table td:before {
            content: attr(data-label) ": ";
            font-weight: 600;
            color: #374151;
            display: inline-block;
            width: 100px;
        }

        .user-actions {
            margin-top: 1rem;
            padding-top: 1rem;
            border-top: 1px solid #f1f5f9;
        }

        .user-actions:before {
            content: none;
        }
    }

    @media (max-width: 480px) {
        .modal-header,
        .modal-body,
        .modal-footer {
            padding: 1rem;
        }

        .view-item {
            flex-direction: column;
            align-items: flex-start;
            gap: 0.5rem;
        }

        .view-value {
            text-align: left;
        }

        .modal-footer {
            flex-direction: column;
        }

        .btn {
            width: 100%;
        }
    }

    /* 접근성 개선 */
    @media (prefers-reduced-motion: reduce) {
        * {
            animation-duration: 0.01ms !important;
            animation-iteration-count: 1 !important;
            transition-duration: 0.01ms !important;
        }
    }

    /* 다크모드 지원 */
    @media (prefers-color-scheme: dark) {
        .user-management-container {
            background: #0f172a;
            color: #f1f5f9;
        }

        .management-section,
        .modal-container {
            background: #1e293b;
            border-color: #334155;
        }

        .section-header,
        .modal-footer,
        .pagination {
            background: #334155;
            border-color: #475569;
        }

        .users-table th {
            background: #334155;
            color: #f1f5f9;
            border-bottom-color: #475569;
        }

        .users-table td {
            color: #e2e8f0;
            border-bottom-color: #334155;
        }

        .user-row:hover {
            background: #374151;
        }

        .search-input,
        .form-input {
            background: #374151;
            border-color: #475569;
            color: #f1f5f9;
        }

        .search-input::placeholder,
        .form-input::placeholder {
            color: #94a3b8;
        }

        .no-data {
            background: #374151;
            color: #94a3b8;
        }

        .view-item {
            background: #374151;
            border-color: #475569;
        }

        .notification {
            background: #1e293b;
            color: #f1f5f9;
        }

        .error-message {
            background: #450a0a;
            border-color: #7f1d1d;
            color: #fca5a5;
        }
    }

    /* 포커스 접근성 */
    .add-user-btn:focus,
    .action-btn:focus,
    .btn:focus,
    .close-btn:focus,
    .page-btn:focus {
        outline: 2px solid #3b82f6;
        outline-offset: 2px;
    }

    .search-input:focus,
    .form-input:focus {
        outline: 2px solid #3b82f6;
        outline-offset: 2px;
    }

    /* 스크롤바 커스터마이징 */
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
</style>
<!-- src/routes/admin/dashboard/+page.svelte -->
<script>
    import { onMount } from 'svelte';
    import { fade, scale } from 'svelte/transition';

    // API 베이스 URL
    const API_BASE = 'http://localhost:8080/api';

    // 대시보드 데이터 상태
    let dashboardData = {
        totalUsers: 0,
        totalBooks: 0,
        totalBranches: 0,
        overdueBooks: 0,
        borrowedBooks: 0,
        returnedBooks: 0,
        activeLoans: 0
    };

    // 연체 대출자 목록
    let overdueBorrowers = [];

    // BookWorm 관리자 목록
    let bookwormAdmins = [];

    // 지점 네트워크 (목업 데이터 - 백엔드에 지점 API가 없음)
    let branchNetwork = [
        { id: 1, name: 'BookWorm - Main Branch', branchId: 'ID: 1', isOnline: true },
        { id: 2, name: 'BookWorm - Seoul Branch', branchId: 'ID: 2', isOnline: true },
        { id: 3, name: 'BookWorm - Busan Branch', branchId: 'ID: 3', isOnline: false }
    ];

    // 로딩 상태
    let loading = {
        statistics: false,
        overdueLoans: false,
        admins: false
    };

    // 에러 상태
    let errors = {
        statistics: null,
        overdueLoans: null,
        admins: null
    };

    // ==================== 백엔드 API 호출 함수들 ====================

    /**
     * 대시보드 통계 데이터 조회
     */
    async function fetchDashboardStatistics() {
        loading.statistics = true;
        errors.statistics = null;

        try {
            console.log('📊 대시보드 통계 조회 시작');

            // 1. 전체 사용자 수 조회
            const usersResponse = await fetch(`${API_BASE}/users`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`
                },
                credentials: 'include'
            });

            if (usersResponse.ok) {
                const usersResult = await usersResponse.json();
                if (usersResult.success && usersResult.data) {
                    dashboardData.totalUsers = Array.isArray(usersResult.data) ? usersResult.data.length : 0;
                }
            }

            // 2. 전체 도서 수 조회
            const booksCountResponse = await fetch(`${API_BASE}/books/count`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`
                },
                credentials: 'include'
            });

            if (booksCountResponse.ok) {
                const booksCountResult = await booksCountResponse.json();
                if (booksCountResult.success && typeof booksCountResult.data === 'number') {
                    dashboardData.totalBooks = booksCountResult.data;
                }
            }

            // 3. 활성 대출 수 조회
            const activeLoansResponse = await fetch(`${API_BASE}/loans/active`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`
                },
                credentials: 'include'
            });

            if (activeLoansResponse.ok) {
                const activeLoansResult = await activeLoansResponse.json();
                if (Array.isArray(activeLoansResult)) {
                    dashboardData.activeLoans = activeLoansResult.length;
                    dashboardData.borrowedBooks = activeLoansResult.length;
                }
            }

            // 4. 도서 통계 조회 (이용 가능한 경우)
            try {
                const bookStatsResponse = await fetch(`${API_BASE}/books/statistics`, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`
                    },
                    credentials: 'include'
                });

                if (bookStatsResponse.ok) {
                    const bookStatsResult = await bookStatsResponse.json();
                    if (bookStatsResult.success && bookStatsResult.data) {
                        // BookStatisticsResponse 데이터 구조에 따라 조정
                        const stats = bookStatsResult.data;
                        if (stats.totalBorrowed) dashboardData.borrowedBooks = stats.totalBorrowed;
                        if (stats.totalReturned) dashboardData.returnedBooks = stats.totalReturned;
                    }
                }
            } catch (e) {
                console.log('📊 도서 통계 API 미사용 (선택사항)');
            }

            // 기본값 설정
            dashboardData.totalBranches = branchNetwork.length;

            console.log('✅ 대시보드 통계 조회 완료:', dashboardData);

        } catch (error) {
            console.error('❌ 대시보드 통계 조회 실패:', error);
            errors.statistics = '통계 데이터를 불러올 수 없습니다.';

            // 목업 데이터로 폴백
            dashboardData = {
                totalUsers: 150,
                totalBooks: 1500,
                totalBranches: 3,
                overdueBooks: 25,
                borrowedBooks: 340,
                returnedBooks: 1160,
                activeLoans: 340
            };
        } finally {
            loading.statistics = false;
        }
    }

    /**
     * 연체 대출자 목록 조회
     */
    async function fetchOverdueBorrowers() {
        loading.overdueLoans = true;
        errors.overdueLoans = null;

        try {
            console.log('🚨 연체 대출 목록 조회 시작');

            const response = await fetch(`${API_BASE}/returns/overdue`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`
                },
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();

            if (Array.isArray(result)) {
                // 연체 대출 데이터를 사용자별로 그룹화
                const overdueMap = new Map();

                result.forEach(loan => {
                    const userId = loan.userId;
                    if (!overdueMap.has(userId)) {
                        overdueMap.set(userId, {
                            id: userId,
                            name: loan.userName || `User ${userId}`,
                            borrowedCount: 0,
                            status: 'overdue'
                        });
                    }
                    overdueMap.get(userId).borrowedCount++;
                });

                overdueBorrowers = Array.from(overdueMap.values()).slice(0, 5); // 상위 5명만
                dashboardData.overdueBooks = result.length;

                console.log('✅ 연체 대출자 목록 조회 완료:', overdueBorrowers.length);
            }

        } catch (error) {
            console.error('❌ 연체 대출자 조회 실패:', error);
            errors.overdueLoans = '연체 대출자 목록을 불러올 수 없습니다.';

            // 목업 데이터로 폴백
            overdueBorrowers = [
                { id: 1, name: 'John Doe', borrowedCount: 3, status: 'overdue' },
                { id: 2, name: 'Jane Smith', borrowedCount: 2, status: 'overdue' },
                { id: 3, name: 'Bob Wilson', borrowedCount: 1, status: 'overdue' }
            ];
        } finally {
            loading.overdueLoans = false;
        }
    }

    /**
     * 관리자 목록 조회
     */
    async function fetchAdminList() {
        loading.admins = true;
        errors.admins = null;

        try {
            console.log('👨‍💼 관리자 목록 조회 시작');

            const response = await fetch(`${API_BASE}/admin`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`
                },
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();

            if (result.success && Array.isArray(result.data)) {
                bookwormAdmins = result.data.map(admin => ({
                    id: admin.id,
                    name: admin.fullName || `${admin.firstName} ${admin.lastName}`,
                    adminId: `Admin ID: ${admin.id}`,
                    email: admin.email,
                    isActive: true // 활성 상태는 기본값으로 설정 (API에 따라 조정)
                }));

                console.log('✅ 관리자 목록 조회 완료:', bookwormAdmins.length);
            }

        } catch (error) {
            console.error('❌ 관리자 목록 조회 실패:', error);
            errors.admins = '관리자 목록을 불러올 수 없습니다.';

            // 목업 데이터로 폴백
            bookwormAdmins = [
                { id: 1, name: 'Admin User', adminId: 'Admin ID: 1', email: 'admin@bookworm.com', isActive: true },
                { id: 2, name: 'Super Admin', adminId: 'Admin ID: 2', email: 'super@bookworm.com', isActive: true }
            ];
        } finally {
            loading.admins = false;
        }
    }

    /**
     * 현재 시간 및 날짜 업데이트
     */
    let currentTime = '';
    let currentDate = '';

    function updateDateTime() {
        const now = new Date();
        currentTime = now.toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
        });
        currentDate = now.toLocaleDateString('en-US', {
            month: 'short',
            day: '2-digit',
            year: 'numeric'
        });
    }

    // 차트 데이터 계산
    $: totalBooks = dashboardData.borrowedBooks + dashboardData.returnedBooks;
    $: borrowedPercentage = totalBooks > 0 ? ((dashboardData.borrowedBooks / totalBooks) * 100).toFixed(1) : 0;
    $: returnedPercentage = totalBooks > 0 ? ((dashboardData.returnedBooks / totalBooks) * 100).toFixed(1) : 0;

    // ==================== 이벤트 핸들러 ====================

    function handleViewDetails(section) {
        console.log(`View details for: ${section}`);
        // 실제 구현에서는 해당 페이지로 라우팅
    }

    function handleRefreshData() {
        console.log('🔄 대시보드 데이터 새로고침');
        fetchDashboardStatistics();
        fetchOverdueBorrowers();
        fetchAdminList();
    }

    // ==================== 생명주기 ====================

    onMount(async () => {
        console.log('📊 관리자 대시보드 초기화');

        // 시간 업데이트 시작
        updateDateTime();
        const timeInterval = setInterval(updateDateTime, 60000); // 1분마다 업데이트

        // 대시보드 데이터 로드
        await Promise.all([
            fetchDashboardStatistics(),
            fetchOverdueBorrowers(),
            fetchAdminList()
        ]);

        // 컴포넌트 파괴 시 인터벌 정리
        return () => {
            clearInterval(timeInterval);
        };
    });
</script>

<svelte:head>
    <title>Dashboard - BookWorm Admin</title>
</svelte:head>

<div class="dashboard-container">
    <!-- 페이지 헤더 -->
    <div class="page-header">
        <h1 class="page-title">Admin Dashboard</h1>
        <div class="header-meta">
            <button class="refresh-btn" on:click={handleRefreshData} disabled={loading.statistics}>
                🔄 새로고침
            </button>
            <span class="current-time">{currentTime}</span>
            <span class="current-date">{currentDate}</span>
        </div>
    </div>

    <!-- 메인 대시보드 그리드 -->
    <div class="dashboard-grid">
        <!-- 왼쪽 섹션 -->
        <div class="left-section">
            <!-- 통계 카드들 -->
            <div class="stats-grid">
                <div class="stat-card" in:scale={{ delay: 100 }}>
                    <div class="stat-icon user-icon">👤</div>
                    <div class="stat-content">
                        <div class="stat-number">
                            {loading.statistics ? '...' : dashboardData.totalUsers.toString().padStart(4, '0')}
                        </div>
                        <div class="stat-label">Total Users</div>
                        {#if errors.statistics}
                            <div class="stat-error">❌ {errors.statistics}</div>
                        {/if}
                    </div>
                </div>

                <div class="stat-card" in:scale={{ delay: 200 }}>
                    <div class="stat-icon book-icon">📚</div>
                    <div class="stat-content">
                        <div class="stat-number">
                            {loading.statistics ? '...' : dashboardData.totalBooks.toString().padStart(5, '0')}
                        </div>
                        <div class="stat-label">Total Books</div>
                    </div>
                </div>

                <div class="stat-card" in:scale={{ delay: 300 }}>
                    <div class="stat-icon loan-icon">📖</div>
                    <div class="stat-content">
                        <div class="stat-number">
                            {loading.statistics ? '...' : dashboardData.activeLoans.toString().padStart(4, '0')}
                        </div>
                        <div class="stat-label">Active Loans</div>
                    </div>
                </div>
            </div>

            <!-- 차트 섹션 -->
            <div class="chart-section" in:fade={{ delay: 400 }}>
                <div class="chart-container">
                    <div class="pie-chart">
                        <svg viewBox="0 0 100 100" class="circular-chart">
                            <!-- 반납된 책 (회색) -->
                            <circle
                                    class="circle-bg"
                                    cx="50"
                                    cy="50"
                                    r="40"
                                    fill="none"
                                    stroke="#e5e7eb"
                                    stroke-width="20"
                            />
                            <!-- 대출된 책 (어두운 색) -->
                            <circle
                                    class="circle-progress"
                                    cx="50"
                                    cy="50"
                                    r="40"
                                    fill="none"
                                    stroke="#374151"
                                    stroke-width="20"
                                    stroke-dasharray="{borrowedPercentage} {returnedPercentage}"
                                    stroke-dashoffset="0"
                                    transform="rotate(-90 50 50)"
                            />
                        </svg>
                        <div class="chart-center">
                            <div class="chart-icon">📊</div>
                        </div>
                    </div>

                    <div class="chart-legend">
                        <div class="legend-item">
                            <div class="legend-dot borrowed"></div>
                            <span>Borrowed Books ({dashboardData.borrowedBooks})</span>
                        </div>
                        <div class="legend-item">
                            <div class="legend-dot returned"></div>
                            <span>Returned Books ({dashboardData.returnedBooks})</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 오른쪽 섹션 -->
        <div class="right-section">
            <!-- 연체 대출자 -->
            <div class="info-panel" in:fade={{ delay: 500 }}>
                <div class="panel-header">
                    <h3 class="panel-title">Overdue Borrowers</h3>
                    {#if loading.overdueLoans}
                        <span class="loading-indicator">⏳ Loading...</span>
                    {/if}
                </div>
                <div class="panel-content">
                    {#if errors.overdueLoans}
                        <div class="error-message">❌ {errors.overdueLoans}</div>
                    {:else if overdueBorrowers.length === 0}
                        <div class="no-data">✅ No overdue borrowers</div>
                    {:else}
                        {#each overdueBorrowers as borrower (borrower.id)}
                            <div class="borrower-item" in:scale={{ delay: 100 * borrower.id }}>
                                <div class="borrower-avatar">
                                    <div class="avatar-icon">👤</div>
                                </div>
                                <div class="borrower-info">
                                    <div class="borrower-name">{borrower.name}</div>
                                    <div class="borrower-count">Borrowed: {borrower.borrowedCount}</div>
                                </div>
                                <button class="action-btn" on:click={() => handleViewDetails('borrower')}>
                                    <span class="btn-icon">↗</span>
                                </button>
                            </div>
                        {/each}
                    {/if}
                </div>
            </div>

            <!-- BookWorm 관리자 -->
            <div class="info-panel" in:fade={{ delay: 600 }}>
                <div class="panel-header">
                    <h3 class="panel-title">BookWorm Admins</h3>
                    {#if loading.admins}
                        <span class="loading-indicator">⏳ Loading...</span>
                    {/if}
                </div>
                <div class="panel-content">
                    {#if errors.admins}
                        <div class="error-message">❌ {errors.admins}</div>
                    {:else}
                        {#each bookwormAdmins as admin (admin.id)}
                            <div class="admin-item" in:scale={{ delay: 100 * admin.id }}>
                                <div class="admin-avatar">
                                    <div class="avatar-icon">👨‍💼</div>
                                </div>
                                <div class="admin-info">
                                    <div class="admin-name">{admin.name}</div>
                                    <div class="admin-id">{admin.adminId}</div>
                                    <div class="admin-email">{admin.email}</div>
                                </div>
                                <div class="admin-status {admin.isActive ? 'active' : 'inactive'}">
                                    <div class="status-dot"></div>
                                    <span class="status-text">{admin.isActive ? 'Active' : 'Inactive'}</span>
                                </div>
                                <button class="action-btn" on:click={() => handleViewDetails('admin')}>
                                    <span class="btn-icon">↗</span>
                                </button>
                            </div>
                        {/each}
                    {/if}
                </div>
            </div>

            <!-- 지점 네트워크 -->
            <div class="info-panel" in:fade={{ delay: 700 }}>
                <div class="panel-header">
                    <h3 class="panel-title">Branch Network</h3>
                </div>
                <div class="panel-content">
                    {#each branchNetwork as branch (branch.id)}
                        <div class="branch-item" in:scale={{ delay: 50 * branch.id }}>
                            <div class="branch-icon">
                                <div class="icon">🏢</div>
                            </div>
                            <div class="branch-info">
                                <div class="branch-name">{branch.name}</div>
                                <div class="branch-id">{branch.branchId}</div>
                            </div>
                            <div class="branch-status {branch.isOnline ? 'online' : 'offline'}">
                                <div class="status-indicator"></div>
                            </div>
                            <button class="action-btn" on:click={() => handleViewDetails('branch')}>
                                <span class="btn-icon">↗</span>
                            </button>
                        </div>
                    {/each}
                </div>
            </div>
        </div>
    </div>
</div>

<style>
    .dashboard-container {
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

    .header-meta {
        display: flex;
        align-items: center;
        gap: 1rem;
    }

    .refresh-btn {
        padding: 0.5rem 1rem;
        background: #374151;
        color: white;
        border: none;
        border-radius: 6px;
        cursor: pointer;
        font-size: 0.875rem;
        transition: background-color 0.2s;
    }

    .refresh-btn:hover:not(:disabled) {
        background: #1f2937;
    }

    .refresh-btn:disabled {
        background: #9ca3af;
        cursor: not-allowed;
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

    /* 메인 그리드 */
    .dashboard-grid {
        display: grid;
        grid-template-columns: 1fr 400px;
        gap: 2rem;
        height: calc(100vh - 200px);
    }

    .left-section {
        display: flex;
        flex-direction: column;
        gap: 2rem;
    }

    .right-section {
        display: flex;
        flex-direction: column;
        gap: 1.5rem;
        overflow-y: auto;
    }

    /* 통계 카드 */
    .stats-grid {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 1.5rem;
    }

    .stat-card {
        background: white;
        border-radius: 12px;
        padding: 1.5rem;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        border: 1px solid #f1f5f9;
        display: flex;
        align-items: center;
        gap: 1rem;
        transition: all 0.2s ease;
    }

    .stat-card:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    }

    .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.5rem;
        flex-shrink: 0;
    }

    .user-icon {
        background: linear-gradient(135deg, #3b82f6, #1d4ed8);
    }

    .book-icon {
        background: linear-gradient(135deg, #10b981, #047857);
    }

    .loan-icon {
        background: linear-gradient(135deg, #f59e0b, #d97706);
    }

    .stat-content {
        flex: 1;
    }

    .stat-number {
        font-size: 1.5rem;
        font-weight: 700;
        color: #1e293b;
        line-height: 1.2;
    }

    .stat-label {
        font-size: 0.875rem;
        color: #64748b;
        margin-top: 0.25rem;
    }

    .stat-error {
        font-size: 0.75rem;
        color: #ef4444;
        margin-top: 0.25rem;
    }

    /* 차트 섹션 */
    .chart-section {
        background: white;
        border-radius: 12px;
        padding: 2rem;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        border: 1px solid #f1f5f9;
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .chart-container {
        display: flex;
        align-items: center;
        gap: 3rem;
    }

    .pie-chart {
        position: relative;
        width: 200px;
        height: 200px;
    }

    .circular-chart {
        width: 100%;
        height: 100%;
        transform: rotate(-90deg);
    }

    .circle-bg {
        stroke: #e5e7eb;
    }

    .circle-progress {
        stroke: #374151;
        stroke-linecap: round;
    }

    .chart-center {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        font-size: 2rem;
    }

    .chart-legend {
        display: flex;
        flex-direction: column;
        gap: 1rem;
    }

    .legend-item {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        font-size: 0.875rem;
        color: #374151;
    }

    .legend-dot {
        width: 12px;
        height: 12px;
        border-radius: 50%;
    }

    .legend-dot.borrowed {
        background: #374151;
    }

    .legend-dot.returned {
        background: #e5e7eb;
    }

    /* 정보 패널 */
    .info-panel {
        background: white;
        border-radius: 12px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        border: 1px solid #f1f5f9;
        overflow: hidden;
    }

    .panel-header {
        padding: 1.5rem 1.5rem 1rem;
        border-bottom: 1px solid #f1f5f9;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .panel-title {
        font-size: 1.125rem;
        font-weight: 600;
        color: #1e293b;
        margin: 0;
    }

    .loading-indicator {
        font-size: 0.875rem;
        color: #64748b;
    }

    .panel-content {
        max-height: 300px;
        overflow-y: auto;
    }

    .error-message {
        padding: 1rem 1.5rem;
        color: #ef4444;
        font-size: 0.875rem;
    }

    .no-data {
        padding: 1rem 1.5rem;
        color: #10b981;
        font-size: 0.875rem;
    }

    /* 아이템들 공통 스타일 */
    .borrower-item,
    .admin-item,
    .branch-item {
        display: flex;
        align-items: center;
        gap: 1rem;
        padding: 1rem 1.5rem;
        border-bottom: 1px solid #f8fafc;
        transition: background-color 0.2s ease;
    }

    .borrower-item:hover,
    .admin-item:hover,
    .branch-item:hover {
        background: #f8fafc;
    }

    .borrower-item:last-child,
    .admin-item:last-child,
    .branch-item:last-child {
        border-bottom: none;
    }

    /* 아바타 */
    .borrower-avatar,
    .admin-avatar,
    .branch-icon {
        width: 40px;
        height: 40px;
        border-radius: 8px;
        background: #f1f5f9;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
    }

    .avatar-icon,
    .icon {
        font-size: 1.25rem;
        color: #64748b;
    }

    /* 정보 */
    .borrower-info,
    .admin-info,
    .branch-info {
        flex: 1;
        min-width: 0;
    }

    .borrower-name,
    .admin-name,
    .branch-name {
        font-size: 0.875rem;
        font-weight: 500;
        color: #1e293b;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .borrower-count,
    .admin-id,
    .admin-email,
    .branch-id {
        font-size: 0.75rem;
        color: #64748b;
        margin-top: 0.25rem;
    }

    .admin-email {
        margin-top: 0.125rem;
    }

    /* 상태 */
    .admin-status {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        margin-right: 0.5rem;
    }

    .status-dot,
    .status-indicator {
        width: 8px;
        height: 8px;
        border-radius: 50%;
    }

    .admin-status.active .status-dot {
        background: #10b981;
    }

    .admin-status.inactive .status-dot {
        background: #ef4444;
    }

    .status-text {
        font-size: 0.75rem;
        color: #64748b;
    }

    .branch-status.online .status-indicator {
        background: #10b981;
    }

    .branch-status.offline .status-indicator {
        background: #ef4444;
    }

    /* 액션 버튼 */
    .action-btn {
        width: 32px;
        height: 32px;
        border-radius: 6px;
        background: #f8fafc;
        border: 1px solid #e2e8f0;
        color: #64748b;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.2s ease;
        flex-shrink: 0;
    }

    .action-btn:hover {
        background: #f1f5f9;
        border-color: #cbd5e1;
        color: #374151;
    }

    .btn-icon {
        font-size: 0.875rem;
        font-weight: 600;
    }

    /* 반응형 디자인 */
    @media (max-width: 1200px) {
        .dashboard-grid {
            grid-template-columns: 1fr;
            gap: 1.5rem;
        }

        .chart-container {
            flex-direction: column;
            gap: 2rem;
        }
    }

    @media (max-width: 768px) {
        .dashboard-container {
            padding: 1rem;
        }

        .stats-grid {
            grid-template-columns: 1fr;
            gap: 1rem;
        }

        .page-header {
            flex-direction: column;
            align-items: flex-start;
            gap: 1rem;
        }

        .header-meta {
            flex-direction: column;
            align-items: flex-start;
            gap: 0.5rem;
        }
    }

    /* 접근성 */
    @media (prefers-reduced-motion: reduce) {
        .stat-card,
        .action-btn,
        .borrower-item,
        .admin-item,
        .branch-item {
            transition: none;
        }
    }

    /* 포커스 스타일 */
    .refresh-btn:focus,
    .action-btn:focus {
        outline: 2px solid #3b82f6;
        outline-offset: 2px;
    }

    /* 로딩 상태 */
    .loading-indicator {
        animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
    }

    @keyframes pulse {
        0%, 100% {
            opacity: 1;
        }
        50% {
            opacity: 0.5;
        }
    }
</style>
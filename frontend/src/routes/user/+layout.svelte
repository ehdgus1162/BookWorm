<!-- src/routes/user/+layout.svelte -->
<script>
    import { onMount } from 'svelte';
    import { goto } from '$app/navigation';
    import { page } from '$app/stores';
    import { unifiedAuthApi, healthApi } from '$lib/api.js';

    // 현재 사용자 정보
    let currentUser = {
        id: null,
        name: '',
        email: '',
        role: 'USER'
    };
    let loading = true;
    let error = null;

    // 시간 상태
    let currentTime = '';
    let currentDate = '';
    let timeInterval;

    // 메뉴 항목들
    const navigationItems = [
        {
            path: '/user/dashboard',
            icon: '/images/bookworm-dashicon-modified-with-class.svg',
            alt: 'Dashboard'
        },
        {
            path: '/user/books',
            icon: '/images/bookworm-bookicon-modified-with-class.svg',
            alt: 'Books'
        },
        {
            path: '/user/loans',
            icon: '/images/bookworm-return-modified-with-class.svg',
            alt: 'Loans'
        }
    ];

    // 현재 페이지 경로
    $: currentPath = $page.url.pathname;

    // 페이지 제목 매핑
    const pageTitles = {
        '/user/dashboard': 'User Dashboard Form',
        '/user/books': 'User Borrow Books Form',
        '/user/loans': 'User Loans Management'
    };

    $: pageTitle = pageTitles[currentPath] || 'User Panel';

    // 컴포넌트 마운트
    onMount(async () => {
        console.log('👤 사용자 레이아웃 초기화');

        // 시간 업데이트 시작
        updateDateTime();
        timeInterval = setInterval(updateDateTime, 1000);

        // 백엔드 연결 상태 확인
        console.log('🔄 백엔드 연결 상태 확인 중...');
        const healthCheck = await healthApi.checkHealth();

        if (healthCheck.success) {
            console.log('✅ 백엔드 연결 정상');
            await checkAuthentication();
        } else {
            console.error('❌ 백엔드 연결 실패:', healthCheck.message);
            loading = false;
            // 연결 실패 시에도 인증 확인 시도 (로컬 개발 환경 고려)
            await checkAuthentication();
        }

        // 클린업
        return () => {
            if (timeInterval) {
                clearInterval(timeInterval);
            }
        };
    });

    // 사용자 인증 확인
    async function checkAuthentication() {
        try {
            console.log('🔐 사용자 인증 확인 시작...');

            const data = await unifiedAuthApi.getCurrentUser();
            console.log('📋 인증 응답 데이터:', data);

            if (data.success && data.data) {
                const userData = data.data;
                console.log('👤 사용자 데이터:', userData);

                // 사용자 권한 확인 (ADMIN이 아닌 경우도 허용)
                if (userData.role === 'USER' || userData.role === 'ADMIN') {
                    currentUser = {
                        id: userData.id,
                        name: userData.name || userData.firstName + ' ' + userData.lastName || 'User',
                        email: userData.email,
                        role: userData.role
                    };
                    console.log('✅ 사용자 인증 성공:', userData.email);
                } else {
                    console.warn('⚠️ 사용자 권한 없음. 현재 role:', userData.role);
                    goto('/login');
                    return;
                }
            } else {
                console.warn('⚠️ 인증 데이터 없음 또는 실패');
                goto('/login');
                return;
            }
        } catch (err) {
            console.error('❌ 사용자 인증 확인 실패:', err);

            // 구체적인 에러 메시지에 따른 처리
            if (err.message.includes('인증이 필요합니다')) {
                console.log('🔑 로그인이 필요합니다. 로그인 페이지로 이동...');
            } else if (err.message.includes('접근 권한이 없습니다')) {
                console.log('🚫 사용자 권한이 없습니다.');
            } else {
                console.log('🌐 네트워크 오류 또는 서버 문제일 수 있습니다.');
            }

            goto('/login');
            return;
        } finally {
            loading = false;
        }
    }

    // 사용자 로그아웃 처리
    async function handleLogout() {
        try {
            console.log('🚪 사용자 로그아웃 시작...');
            await unifiedAuthApi.logout();
            console.log('✅ 사용자 로그아웃 성공');
            goto('/login');
        } catch (err) {
            console.error('❌ 사용자 로그아웃 실패:', err);
            // 실패해도 로그인 페이지로 이동 (안전장치)
            goto('/login');
        }
    }

    // 시간 업데이트 함수
    function updateDateTime() {
        const now = new Date();
        currentTime = now.toLocaleTimeString('ko-KR', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        });
        currentDate = now.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        }).replace(/\./g, '-').replace(/-$/, '');
    }

    // 네비게이션 함수
    function navigateTo(path) {
        goto(path);
    }

    // 활성 메뉴 확인
    function isActive(path) {
        return currentPath === path;
    }

    // 에러 닫기
    function closeError() {
        error = null;
    }

    // 키보드 이벤트 처리 (전역)
    function handleKeydown(event) {
        if (event.key === 'Escape') {
            closeError();
        }
    }

    // 에러 설정 함수 (하위 컴포넌트에서 사용할 수 있도록)
    function setError(message) {
        error = message;
    }

    // 하위 컴포넌트에서 접근할 수 있도록 context 제공
    import { setContext } from 'svelte';

    setContext('userLayout', {
        currentUser,
        setError,
        closeError
    });
</script>

<svelte:head>
    <title>BookWorm Library - User Panel</title>
    <meta name="description" content="BookWorm Library Management System - User Dashboard">
</svelte:head>

<svelte:window on:keydown={handleKeydown} />

{#if loading}
    <!-- 로딩 화면 -->
    <div class="loading-screen">
        <div class="loading-spinner">
            <div class="spinner"></div>
            <p>사용자 인증 확인 중...</p>
        </div>
    </div>
{:else if currentUser?.id}
    <!-- 사용자 레이아웃 -->
    <div class="user-layout">
        <!-- 왼쪽 사이드바 -->
        <aside class="sidebar">
            <div class="logo">
                <img src="/images/bookworm-logo-white.svg" alt="BookWorm Logo" class="logo-img" />
            </div>

            <nav class="nav-menu">
                {#each navigationItems as item}
                    <button
                            class="nav-item"
                            class:active={isActive(item.path)}
                            on:click={() => navigateTo(item.path)}
                            title={item.alt}
                    >
                        <span class="nav-icon">
                            <img
                                    src={item.icon}
                                    alt={item.alt}
                                    class="nav-icon-img"
                                    width="25px"
                                    style={isActive(item.path) ? "filter: brightness(0)" : ""}
                            />
                        </span>
                    </button>
                {/each}
            </nav>

            <div class="user-section">
                <div class="user-avatar" on:click={() => navigateTo('/user/profile')} title="프로필">👤</div>
                <button class="logout-btn" on:click={handleLogout} title="로그아웃">
                    🚪
                </button>
            </div>
        </aside>

        <!-- 메인 콘텐츠 영역 -->
        <div class="main-content">
            <!-- 상단 헤더 -->
            <header class="content-header">
                <div class="header-left">
                    <div class="user-info">
                        <div class="user-avatar-small">👤</div>
                        <div class="user-details">
                            <span class="user-name">{currentUser.name}</span>
                            <span class="user-role">{currentUser.role === 'ADMIN' ? 'Admin' : 'User'}</span>
                        </div>
                    </div>
                </div>

                <div class="header-center">
                    <h1 class="page-title">{pageTitle}</h1>
                </div>

                <div class="header-right">
                    <div class="datetime">
                        <div class="current-time">{currentTime}</div>
                        <div class="current-date">{currentDate}</div>
                    </div>
                    <button class="settings-btn" on:click={() => navigateTo('/user/profile')} title="설정">⚙️</button>
                </div>
            </header>

            <!-- 에러 배너 -->
            {#if error}
                <div class="error-banner">
                    <span class="error-icon">⚠️</span>
                    <span class="error-message">{error}</span>
                    <button class="error-close" on:click={closeError}>✕</button>
                </div>
            {/if}

            <!-- 페이지별 콘텐츠 슬롯 -->
            <main class="page-content">
                <slot />
            </main>
        </div>

        <!-- 오른쪽 사이드바 -->
        <aside class="right-sidebar">
            <div class="bookworm-logo">
                <div class="logo-text">BOOKWORM</div>
            </div>
        </aside>
    </div>
{/if}

<style>
    /* ==================== 로딩 화면 ==================== */
    .loading-screen {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: linear-gradient(135deg, #000000 0%, #374151 50%, #6b7280 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 9999;
    }

    .loading-spinner {
        text-align: center;
        color: white;
    }

    .spinner {
        width: 50px;
        height: 50px;
        border: 3px solid rgba(255, 255, 255, 0.3);
        border-radius: 50%;
        border-top-color: white;
        animation: spin 1s ease-in-out infinite;
        margin: 0 auto 1rem;
    }

    @keyframes spin {
        to { transform: rotate(360deg); }
    }

    /* ==================== 전체 레이아웃 - 흑백 테마 ==================== */
    .user-layout {
        display: flex;
        min-height: 100vh;
        background: #f8f9fa;
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    }

    /* ==================== 사이드바 스타일 ==================== */
    .sidebar {
        width: 150px;
        background: #000000;
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 1rem 0;
        position: relative;
        z-index: 100;
    }

    .logo {
        margin-bottom: 4rem;
    }

    .logo-img {
        width: 80px;
        height: auto;
        filter: brightness(0) invert(1);
    }

    .nav-menu {
        display: flex;
        flex-direction: column;
        gap: 1rem;
        margin-bottom: auto;
    }

    .nav-item {
        width: 150px;
        height: 55px;
        background: none;
        border: none;
        color: white;
        cursor: pointer;
        transition: background-color 0.2s;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .nav-item:hover {
        background: rgba(255, 255, 255, 0.1);
    }

    .nav-item.active {
        background: #ffffff;
    }

    .nav-icon-img {
        transition: filter 0.2s;
    }

    .user-section {
        margin-top: auto;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 1rem;
    }

    .user-avatar {
        width: 48px;
        height: 48px;
        background: #374151;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.5rem;
        color: white;
        cursor: pointer;
        transition: background-color 0.2s;
    }

    .user-avatar:hover {
        background: #4b5563;
    }

    .logout-btn {
        width: 40px;
        height: 40px;
        background: rgba(239, 68, 68, 0.1);
        border: 1px solid rgba(239, 68, 68, 0.3);
        color: #fca5a5;
        border-radius: 50%;
        cursor: pointer;
        font-size: 1.2rem;
        transition: all 0.2s;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .logout-btn:hover {
        background: rgba(239, 68, 68, 0.2);
        border-color: rgba(239, 68, 68, 0.5);
        color: #f87171;
        transform: scale(1.05);
    }

    /* 오른쪽 사이드바 */
    .right-sidebar {
        width: 80px;
        background: #000000;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 1rem 0;
    }

    .bookworm-logo {
        writing-mode: vertical-rl;
        text-orientation: mixed;
        color: white;
        font-weight: 600;
        font-size: 1.25rem;
        letter-spacing: 0.1em;
    }

    /* ==================== 메인 콘텐츠 ==================== */
    .main-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        padding: 1.5rem;
    }

    .content-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 2rem;
        padding: 1rem;
        background: white;
        border-radius: 8px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        border: 1px solid #e5e7eb;
    }

    .header-left {
        display: flex;
        align-items: center;
    }

    .user-info {
        display: flex;
        align-items: center;
        gap: 0.75rem;
    }

    .user-avatar-small {
        width: 40px;
        height: 40px;
        background: #f3f4f6;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.25rem;
        border: 1px solid #d1d5db;
    }

    .user-details {
        display: flex;
        flex-direction: column;
    }

    .user-name {
        font-weight: 600;
        color: #111827;
    }

    .user-role {
        font-size: 0.875rem;
        color: #6b7280;
    }

    .header-center {
        flex: 1;
        text-align: center;
    }

    .page-title {
        font-size: 1.5rem;
        font-weight: 600;
        color: #111827;
        margin: 0;
    }

    .header-right {
        display: flex;
        align-items: center;
        gap: 1rem;
    }

    .datetime {
        text-align: right;
    }

    .current-time {
        font-size: 1.25rem;
        font-weight: 600;
        color: #111827;
    }

    .current-date {
        font-size: 0.875rem;
        color: #6b7280;
    }

    .settings-btn {
        width: 40px;
        height: 40px;
        background: white;
        border: 1px solid #d1d5db;
        border-radius: 8px;
        font-size: 1.25rem;
        cursor: pointer;
        transition: all 0.2s;
        color: #374151;
    }

    .settings-btn:hover {
        background: #f9fafb;
        border-color: #9ca3af;
    }

    /* ==================== 에러 배너 ==================== */
    .error-banner {
        display: flex;
        align-items: center;
        gap: 1rem;
        padding: 1rem;
        background: #fef2f2;
        border: 1px solid #fecaca;
        border-radius: 8px;
        margin-bottom: 1.5rem;
        color: #991b1b;
        animation: slideDown 0.3s ease-out;
    }

    @keyframes slideDown {
        from {
            opacity: 0;
            transform: translateY(-10px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
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
        color: #991b1b;
        cursor: pointer;
        font-size: 1.25rem;
        padding: 0.25rem;
        border-radius: 4px;
        transition: background-color 0.2s;
    }

    .error-close:hover {
        background: rgba(153, 27, 27, 0.1);
    }

    /* ==================== 페이지 콘텐츠 ==================== */
    .page-content {
        flex: 1;
        position: relative;
        min-height: 0;
        overflow: auto;
    }

    /* ==================== 반응형 디자인 ==================== */
    @media (max-width: 1024px) {
        .user-layout {
            flex-direction: column;
        }

        .sidebar,
        .right-sidebar {
            width: 100%;
            height: auto;
            flex-direction: row;
            justify-content: space-between;
            padding: 0.5rem 1rem;
        }

        .nav-menu {
            flex-direction: row;
            margin-bottom: 0;
        }

        .nav-item {
            width: auto;
            height: 40px;
            padding: 0 1rem;
        }

        .user-section {
            flex-direction: row;
            margin-top: 0;
        }

        .bookworm-logo {
            writing-mode: initial;
            text-orientation: initial;
        }

        .content-header {
            flex-direction: column;
            gap: 1rem;
            text-align: center;
        }

        .header-left,
        .header-right {
            width: 100%;
            justify-content: center;
        }
    }

    @media (max-width: 768px) {
        .main-content {
            padding: 1rem;
        }

        .content-header {
            padding: 0.75rem;
        }

        .page-title {
            font-size: 1.25rem;
        }

        .user-info {
            flex-direction: column;
            text-align: center;
            gap: 0.5rem;
        }

        .datetime {
            text-align: center;
        }
    }

    @media (max-width: 480px) {
        .nav-item {
            height: 35px;
            padding: 0 0.5rem;
        }

        .nav-icon-img {
            width: 20px !important;
        }

        .user-avatar,
        .logout-btn {
            width: 35px;
            height: 35px;
            font-size: 1rem;
        }

        .settings-btn {
            width: 35px;
            height: 35px;
            font-size: 1rem;
        }
    }

    /* ==================== 접근성 및 기타 ==================== */
    @media (prefers-reduced-motion: reduce) {
        .nav-item,
        .user-avatar,
        .logout-btn,
        .settings-btn {
            transition: none;
        }

        .spinner {
            animation: none;
        }

        .error-banner {
            animation: none;
        }
    }

    /* 고대비 모드 지원 */
    @media (prefers-contrast: high) {
        .sidebar {
            border-right: 2px solid #ffffff;
        }

        .right-sidebar {
            border-left: 2px solid #ffffff;
        }

        .content-header {
            border: 2px solid #000000;
        }
    }

    /* 포커스 스타일 */
    .nav-item:focus,
    .user-avatar:focus,
    .logout-btn:focus,
    .settings-btn:focus,
    .error-close:focus {
        outline: 2px solid #000000;
        outline-offset: 2px;
    }

    /* 프린트 스타일 */
    @media print {
        .sidebar,
        .right-sidebar,
        .error-banner,
        .logout-btn,
        .settings-btn {
            display: none;
        }

        .user-layout {
            flex-direction: column;
        }

        .main-content {
            padding: 0;
        }
    }
</style>
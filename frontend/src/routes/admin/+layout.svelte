<!-- src/routes/admin/+layout.svelte -->
<script>
    import { onMount } from 'svelte';
    import { goto } from '$app/navigation';
    import { page } from '$app/stores';
    import { unifiedAuthApi, healthApi } from '$lib/api.js';

    // 현재 사용자 정보
    let currentUser = null;
    let loading = true;

    // 현재 시간
    let currentTime = new Date();
    let timeInterval;

    // 반응형 변수들을 먼저 선언
    $: timeDisplay = currentTime.toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: true
    });

    $: dateDisplay = currentTime.toLocaleDateString('en-US', {
        month: 'short',
        day: '2-digit',
        year: 'numeric'
    });

    // 메뉴 항목들
    const menuItems = [
        {
            href: '/admin/dashboard',
            icon: '📊',
            label: 'Dashboard',
            description: '대시보드 및 통계'
        },
        // {
        //     href: '/admin/catalog',
        //     icon: '📚',
        //     label: 'Catalog',
        //     description: '도서 카탈로그'
        // },
        {
            href: '/admin/books',
            icon: '📖',
            label: 'Books',
            description: '도서 관리'
        },
        {
            href: '/admin/borrowed-books',
            icon: '📋',
            label: 'Borrowed Books',
            description: '대출 도서 관리'
        },
        {
            href: '/admin/users',
            icon: '👥',
            label: 'Users',
            description: '사용자 관리'
        },
        // {
        //     href: '/admin/branches',
        //     icon: '🏢',
        //     label: 'Branches',
        //     description: '지점 관리'
        // }
    ];

    // 현재 페이지 확인
    $: currentPath = $page.url.pathname;

    // 컴포넌트 마운트
    onMount(async () => {
        // 시간 업데이트 인터벌 설정
        timeInterval = setInterval(() => {
            currentTime = new Date();
        }, 1000);

        // 백엔드 연결 상태 먼저 확인
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

        // 컴포넌트가 파괴될 때 인터벌 정리
        return () => {
            if (timeInterval) {
                clearInterval(timeInterval);
            }
        };
    });

    // +layout.svelte의 checkAuthentication 함수 수정

    async function checkAuthentication() {
        try {
            console.log('🔐 관리자 인증 확인 시작...');

            // ✅ 관리자 전용 API 대신 통합 API 사용 (세션 기반 인증이 정상 작동함)
            const data = await unifiedAuthApi.getCurrentUser();

            console.log('📋 인증 응답 데이터:', data);

            if (data.success && data.data) {
                const userData = data.data;

                console.log('👤 사용자 데이터:', userData);
                console.log('🔍 role 값:', userData.role);

                // ✅ 통합 API 응답에서 관리자 권한 확인
                if (userData.role === 'ADMIN') {
                    currentUser = userData;
                    console.log('✅ 관리자 인증 성공:', userData.email);
                } else {
                    console.warn('⚠️ 관리자 권한 없음. 현재 role:', userData.role);
                    goto('/admin/login');
                    return;
                }
            } else {
                console.warn('⚠️ 인증 데이터 없음 또는 실패');
                goto('/admin/login');
                return;
            }
        } catch (err) {
            console.error('❌ 관리자 인증 확인 실패:', err);

            // 구체적인 에러 메시지에 따른 처리
            if (err.message.includes('인증이 필요합니다')) {
                console.log('🔑 로그인이 필요합니다. 로그인 페이지로 이동...');
            } else if (err.message.includes('접근 권한이 없습니다')) {
                console.log('🚫 관리자 권한이 없습니다.');
            } else {
                console.log('🌐 네트워크 오류 또는 서버 문제일 수 있습니다.');
            }

            goto('/admin/login');
            return;
        } finally {
            loading = false;
        }
    }

    // 관리자 로그아웃 처리 ⭐ 수정사항
    async function handleLogout() {
        try {
            console.log('🚪 관리자 로그아웃 시작...');

            // ⭐ adminAuthApi.logout() 사용
            await unifiedAuthApi.logout();

            console.log('✅ 관리자 로그아웃 성공');
            goto('/admin/login');
        } catch (err) {
            console.error('❌ 관리자 로그아웃 실패:', err);
            // 실패해도 로그인 페이지로 이동 (안전장치)
            goto('/admin/login');
        }
    }

    // 메뉴 항목이 활성화되어 있는지 확인
    function isActiveMenu(href) {
        return currentPath === href || currentPath.startsWith(href + '/');
    }
</script>

<svelte:head>
    <!-- 기존 CSS 파일들 로드 -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/base/variables.css">
    <link rel="stylesheet" href="/css/base/reset.css">
    <link rel="stylesheet" href="/css/base/typography.css">
    <link rel="stylesheet" href="/css/layout/containers.css">
    <link rel="stylesheet" href="/css/components/panels.css">
    <link rel="stylesheet" href="/css/components/forms.css">
    <link rel="stylesheet" href="/css/components/buttons.css">
    <link rel="stylesheet" href="/css/components/alerts.css">
    <link rel="stylesheet" href="/css/utilities/spacing.css">
</svelte:head>

{#if loading}
    <!-- 로딩 화면 -->
    <div class="loading-screen">
        <div class="loading-spinner">
            <div class="spinner"></div>
            <p>관리자 인증 확인 중...</p>
        </div>
    </div>
{:else if currentUser}
    <!-- 관리자 레이아웃 -->
    <div class="admin-layout">
        <!-- 사이드바 -->
        <aside class="sidebar">
            <!-- 로고 -->
            <div class="logo">
                <div class="logo-icon">📚</div>
                <div class="logo-text">
                    <span class="logo-title">BookWorm</span>
                    <span class="logo-subtitle">ADMIN</span>
                </div>
            </div>

            <!-- 관리자 프로필 -->
            <div class="admin-profile">
                <div class="profile-avatar">
                    {currentUser.firstName?.[0] || 'A'}{currentUser.lastName?.[0] || 'D'}
                </div>
                <div class="profile-info">
                    <div class="profile-name">
                        {currentUser.firstName || 'Admin'} {currentUser.lastName || 'User'}
                    </div>
                    <div class="profile-email">{currentUser.email}</div>
                </div>
            </div>

            <!-- 메뉴 -->
            <nav class="menu">
                {#each menuItems as item}
                    <a
                            href={item.href}
                            class="menu-item {isActiveMenu(item.href) ? 'active' : ''}"
                            title={item.description}
                    >
                        <span class="menu-icon">{item.icon}</span>
                        <div class="menu-content">
                            <span class="menu-label">{item.label}</span>
                            <span class="menu-description">{item.description}</span>
                        </div>
                    </a>
                {/each}
            </nav>

            <!-- 시스템 정보 -->
            <div class="system-info">
                <div class="info-item">
                    <span class="info-label">Version</span>
                    <span class="info-value">v2.1.0</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Server</span>
                    <span class="info-value">Online</span>
                </div>
            </div>

            <!-- 로그아웃 -->
            <div class="sidebar-footer">
                <button class="logout-btn" on:click={handleLogout}>
                    <span class="logout-icon">🚪</span>
                    <span class="logout-label">Log Out</span>
                </button>
            </div>
        </aside>

        <!-- 메인 콘텐츠 영역 -->
        <div class="main-content">
            <!-- 상단 헤더 -->
            <header class="top-header">
                <div class="header-left">
                    <button class="mobile-menu-btn">☰</button>
                    <div class="breadcrumb">
                        <span class="breadcrumb-home">🏠</span>
                        <span class="breadcrumb-separator">></span>
                        <span class="breadcrumb-current">
                            {menuItems.find(item => isActiveMenu(item.href))?.label || 'Admin'}
                        </span>
                    </div>
                </div>

                <div class="header-right">
                    <!-- 알림 버튼 -->
                    <button class="notification-btn" title="알림">
                        <span class="notification-icon">🔔</span>
                        <span class="notification-badge">3</span>
                    </button>

                    <!-- 설정 버튼 -->
                    <button class="settings-btn" title="설정">
                        <span class="settings-icon">⚙️</span>
                    </button>

                    <!-- 시간 표시 -->
                    <div class="time-display">
                        <span class="time">{timeDisplay}</span>
                        <span class="date">{dateDisplay}</span>
                    </div>
                </div>
            </header>

            <!-- 페이지 콘텐츠 -->
            <main class="page-content">
                <slot />
            </main>

            <!-- 하단 푸터 -->
            <footer class="bottom-footer">
                <div class="footer-content">
                    <span class="footer-text">
                        © 2024 BookWorm Library Management System. All rights reserved.
                    </span>
                    <div class="footer-links">
                        <a href="/admin/help" class="footer-link">도움말</a>
                        <a href="/admin/support" class="footer-link">지원</a>
                        <a href="/admin/about" class="footer-link">정보</a>
                    </div>
                </div>
            </footer>
        </div>
    </div>
{/if}

<style>
    /* 로딩 화면 */
    .loading-screen {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: linear-gradient(135deg, #1e3a8a 0%, #3730a3 50%, #581c87 100%);
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

    /* 관리자 레이아웃 */
    .admin-layout {
        display: flex;
        min-height: 100vh;
        background: #f8fafc;
        font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    }

    /* 사이드바 스타일 */
    .sidebar {
        width: 280px;
        background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
        color: white;
        display: flex;
        flex-direction: column;
        box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
        position: relative;
        z-index: 100;
    }

    .logo {
        display: flex;
        align-items: center;
        gap: 1rem;
        padding: 1.5rem;
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    }

    .logo-icon {
        font-size: 2rem;
        filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
    }

    .logo-text {
        display: flex;
        flex-direction: column;
    }

    .logo-title {
        font-size: 1.5rem;
        font-weight: 700;
        color: white;
        line-height: 1;
    }

    .logo-subtitle {
        font-size: 0.75rem;
        color: #64748b;
        font-weight: 500;
        letter-spacing: 0.1em;
        margin-top: 2px;
    }

    /* 관리자 프로필 */
    .admin-profile {
        display: flex;
        align-items: center;
        gap: 1rem;
        padding: 1.5rem;
        background: rgba(255, 255, 255, 0.05);
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    }

    .profile-avatar {
        width: 48px;
        height: 48px;
        border-radius: 50%;
        background: linear-gradient(135deg, #3b82f6, #1d4ed8);
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
        font-size: 1.1rem;
        color: white;
        text-transform: uppercase;
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
    }

    .profile-info {
        flex: 1;
        min-width: 0;
    }

    .profile-name {
        font-weight: 600;
        font-size: 0.95rem;
        color: white;
        margin-bottom: 2px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .profile-email {
        font-size: 0.8rem;
        color: #94a3b8;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    /* 메뉴 스타일 */
    .menu {
        flex: 1;
        padding: 1rem 0;
        overflow-y: auto;
    }

    .menu-item {
        display: flex;
        align-items: center;
        gap: 1rem;
        padding: 1rem 1.5rem;
        color: #cbd5e1;
        text-decoration: none;
        transition: all 0.2s ease;
        border-left: 3px solid transparent;
        position: relative;
    }

    .menu-item:hover {
        background: rgba(255, 255, 255, 0.08);
        color: white;
        border-left-color: rgba(59, 130, 246, 0.5);
    }

    .menu-item.active {
        background: rgba(59, 130, 246, 0.15);
        color: white;
        border-left-color: #3b82f6;
    }

    .menu-item.active::before {
        content: '';
        position: absolute;
        right: 1rem;
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: #3b82f6;
        box-shadow: 0 0 10px rgba(59, 130, 246, 0.6);
    }

    .menu-icon {
        font-size: 1.3rem;
        width: 1.5rem;
        text-align: center;
        flex-shrink: 0;
    }

    .menu-content {
        display: flex;
        flex-direction: column;
        flex: 1;
        min-width: 0;
    }

    .menu-label {
        font-weight: 500;
        font-size: 0.95rem;
        line-height: 1.2;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .menu-description {
        font-size: 0.75rem;
        color: #64748b;
        margin-top: 2px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    /* 시스템 정보 */
    .system-info {
        padding: 1rem 1.5rem;
        border-top: 1px solid rgba(255, 255, 255, 0.1);
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    }

    .info-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 0.5rem;
    }

    .info-item:last-child {
        margin-bottom: 0;
    }

    .info-label {
        font-size: 0.8rem;
        color: #64748b;
    }

    .info-value {
        font-size: 0.8rem;
        color: #22c55e;
        font-weight: 500;
    }

    /* 로그아웃 버튼 */
    .sidebar-footer {
        padding: 1.5rem;
    }

    .logout-btn {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        padding: 0.75rem 1rem;
        background: rgba(239, 68, 68, 0.1);
        border: 1px solid rgba(239, 68, 68, 0.3);
        color: #fca5a5;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.2s ease;
        width: 100%;
        font-size: 0.9rem;
        font-weight: 500;
    }

    .logout-btn:hover {
        background: rgba(239, 68, 68, 0.2);
        border-color: rgba(239, 68, 68, 0.5);
        color: #f87171;
        transform: translateY(-1px);
    }

    .logout-icon {
        font-size: 1.1rem;
    }

    /* 메인 콘텐츠 영역 */
    .main-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        background: #ffffff;
    }

    /* 상단 헤더 */
    .top-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1rem 2rem;
        background: white;
        border-bottom: 1px solid #e2e8f0;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
        position: sticky;
        top: 0;
        z-index: 50;
    }

    .header-left {
        display: flex;
        align-items: center;
        gap: 1rem;
    }

    .mobile-menu-btn {
        display: none;
        background: none;
        border: none;
        font-size: 1.3rem;
        color: #64748b;
        cursor: pointer;
        padding: 0.5rem;
        border-radius: 6px;
        transition: all 0.2s ease;
    }

    .mobile-menu-btn:hover {
        background: #f1f5f9;
        color: #334155;
    }

    .breadcrumb {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        font-size: 0.9rem;
        color: #64748b;
    }

    .breadcrumb-home {
        font-size: 1rem;
    }

    .breadcrumb-separator {
        color: #cbd5e1;
    }

    .breadcrumb-current {
        color: #1e293b;
        font-weight: 500;
    }

    .header-right {
        display: flex;
        align-items: center;
        gap: 1rem;
    }

    .notification-btn,
    .settings-btn {
        position: relative;
        background: none;
        border: none;
        font-size: 1.2rem;
        color: #64748b;
        cursor: pointer;
        padding: 0.5rem;
        border-radius: 6px;
        transition: all 0.2s ease;
    }

    .notification-btn:hover,
    .settings-btn:hover {
        background: #f1f5f9;
        color: #334155;
    }

    .notification-badge {
        position: absolute;
        top: 0;
        right: 0;
        background: #ef4444;
        color: white;
        font-size: 0.7rem;
        font-weight: 600;
        padding: 2px 6px;
        border-radius: 10px;
        min-width: 18px;
        text-align: center;
        line-height: 1.2;
    }

    .time-display {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        gap: 2px;
    }

    .time {
        font-size: 0.9rem;
        font-weight: 600;
        color: #1e293b;
    }

    .date {
        font-size: 0.8rem;
        color: #64748b;
    }

    /* 페이지 콘텐츠 */
    .page-content {
        flex: 1;
        padding: 0;
        background: #f8fafc;
        min-height: 0;
        overflow: auto;
    }

    /* 하단 푸터 */
    .bottom-footer {
        background: white;
        border-top: 1px solid #e2e8f0;
        padding: 1rem 2rem;
    }

    .footer-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
        max-width: 100%;
    }

    .footer-text {
        font-size: 0.8rem;
        color: #64748b;
    }

    .footer-links {
        display: flex;
        gap: 1.5rem;
    }

    .footer-link {
        font-size: 0.8rem;
        color: #3b82f6;
        text-decoration: none;
        transition: color 0.2s ease;
    }

    .footer-link:hover {
        color: #1d4ed8;
        text-decoration: underline;
    }

    /* 반응형 디자인 */
    @media (max-width: 1024px) {
        .sidebar {
            width: 240px;
        }

        .menu-description {
            display: none;
        }

        .admin-profile {
            padding: 1rem;
        }

        .profile-avatar {
            width: 40px;
            height: 40px;
            font-size: 1rem;
        }
    }

    @media (max-width: 768px) {
        .admin-layout {
            flex-direction: row;
        }

        .sidebar {
            position: fixed;
            left: -280px;
            top: 0;
            bottom: 0;
            z-index: 200;
            transition: left 0.3s ease;
        }

        .sidebar.open {
            left: 0;
        }

        .main-content {
            width: 100%;
        }

        .mobile-menu-btn {
            display: block;
        }

        .top-header {
            padding: 1rem;
        }

        .header-right {
            gap: 0.5rem;
        }

        .time-display {
            display: none;
        }

        .footer-content {
            flex-direction: column;
            gap: 0.5rem;
            text-align: center;
        }

        .footer-links {
            gap: 1rem;
        }
    }

    @media (max-width: 480px) {
        .breadcrumb {
            font-size: 0.8rem;
        }

        .breadcrumb-current {
            max-width: 150px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .top-header {
            padding: 0.75rem;
        }

        .page-content {
            padding: 0;
        }

        .bottom-footer {
            padding: 0.75rem;
        }
    }

    /* 접근성 개선 */
    @media (prefers-reduced-motion: reduce) {
        .menu-item,
        .logout-btn,
        .notification-btn,
        .settings-btn,
        .mobile-menu-btn {
            transition: none;
        }

        .spinner {
            animation: none;
        }
    }

    /* 다크모드 지원 */
    @media (prefers-color-scheme: dark) {
        .admin-layout {
            background: #0f172a;
        }

        .main-content {
            background: #1e293b;
        }

        .top-header {
            background: #1e293b;
            border-bottom-color: #334155;
        }

        .bottom-footer {
            background: #1e293b;
            border-top-color: #334155;
        }

        .breadcrumb-current {
            color: #f1f5f9;
        }

        .time {
            color: #f1f5f9;
        }

        .footer-text {
            color: #94a3b8;
        }
    }
</style>
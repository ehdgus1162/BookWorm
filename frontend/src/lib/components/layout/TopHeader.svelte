<!-- src/lib/components/layout/TopHeader.svelte -->
<script>
    import { page } from '$app/stores';
    import { onMount, onDestroy } from 'svelte';
    import { createEventDispatcher } from 'svelte';

    export let currentUser = null;
    export let showMobileMenu = false;

    const dispatch = createEventDispatcher();

    // 현재 시간
    let currentTime = new Date();
    let timeInterval;

    // 반응형 변수들
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

    // 메뉴 항목들 (브레드크럼용)
    const menuItems = [
        { href: '/admin/dashboard', label: 'Dashboard' },
        { href: '/admin/catalog', label: 'Catalog' },
        { href: '/admin/books', label: 'Books' },
        { href: '/admin/borrowed-books', label: 'Borrowed Books' },
        { href: '/admin/users', label: 'Users' },
        { href: '/admin/branches', label: 'Branches' }
    ];

    // 현재 페이지 확인
    $: currentPath = $page.url.pathname;
    $: currentMenuItem = menuItems.find(item =>
        currentPath === item.href || currentPath.startsWith(item.href + '/')
    );

    // 컴포넌트 마운트
    onMount(() => {
        // 시간 업데이트 인터벌 설정
        timeInterval = setInterval(() => {
            currentTime = new Date();
        }, 1000);
    });

    onDestroy(() => {
        if (timeInterval) {
            clearInterval(timeInterval);
        }
    });

    function toggleMobileMenu() {
        showMobileMenu = !showMobileMenu;
        dispatch('toggleMobileMenu', showMobileMenu);
    }

    function handleNotificationClick() {
        dispatch('notificationClick');
    }

    function handleSettingsClick() {
        dispatch('settingsClick');
    }
</script>

<header class="top-header">
    <div class="header-left">
        <button
                class="mobile-menu-btn"
                class:active={showMobileMenu}
                on:click={toggleMobileMenu}
                aria-label="Toggle mobile menu"
        >
            ☰
        </button>
        <div class="breadcrumb">
            <span class="breadcrumb-home">🏠</span>
            <span class="breadcrumb-separator">></span>
            <span class="breadcrumb-current">
                {currentMenuItem?.label || 'Admin'}
            </span>
        </div>
    </div>

    <div class="header-right">
        <!-- 알림 버튼 -->
        <button
                class="notification-btn"
                title="알림"
                on:click={handleNotificationClick}
        >
            <span class="notification-icon">🔔</span>
            <span class="notification-badge">3</span>
        </button>

        <!-- 설정 버튼 -->
        <button
                class="settings-btn"
                title="설정"
                on:click={handleSettingsClick}
        >
            <span class="settings-icon">⚙️</span>
        </button>

        <!-- 시간 표시 -->
        <div class="time-display">
            <span class="time">{timeDisplay}</span>
            <span class="date">{dateDisplay}</span>
        </div>

        <!-- 사용자 정보 (모바일에서는 숨김) -->
        {#if currentUser}
            <div class="user-info">
                <div class="user-avatar">
                    {currentUser.firstName?.[0] || 'A'}{currentUser.lastName?.[0] || 'D'}
                </div>
                <div class="user-details">
                    <span class="user-name">
                        {currentUser.firstName || 'Admin'} {currentUser.lastName || 'User'}
                    </span>
                    <span class="user-role">Admin</span>
                </div>
            </div>
        {/if}
    </div>
</header>

<style>
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
        height: 72px;
        box-sizing: border-box;
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
        line-height: 1;
    }

    .mobile-menu-btn:hover {
        background: #f1f5f9;
        color: #334155;
    }

    .mobile-menu-btn.active {
        background: #e2e8f0;
        color: #1e293b;
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
        line-height: 1;
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
        transform: translate(25%, -25%);
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
        line-height: 1;
    }

    .date {
        font-size: 0.8rem;
        color: #64748b;
        line-height: 1;
    }

    .user-info {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        padding: 0.5rem;
        border-radius: 8px;
        background: #f8fafc;
        border: 1px solid #e2e8f0;
    }

    .user-avatar {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        background: linear-gradient(135deg, #3b82f6, #1d4ed8);
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
        font-size: 0.875rem;
        color: white;
        text-transform: uppercase;
        flex-shrink: 0;
    }

    .user-details {
        display: flex;
        flex-direction: column;
        min-width: 0;
    }

    .user-name {
        font-size: 0.875rem;
        font-weight: 500;
        color: #1e293b;
        line-height: 1.2;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .user-role {
        font-size: 0.75rem;
        color: #64748b;
        line-height: 1;
    }

    /* 반응형 디자인 */
    @media (max-width: 1024px) {
        .top-header {
            padding: 1rem 1.5rem;
        }

        .user-info {
            display: none;
        }
    }

    @media (max-width: 768px) {
        .top-header {
            padding: 1rem;
        }

        .mobile-menu-btn {
            display: block;
        }

        .header-right {
            gap: 0.5rem;
        }

        .time-display {
            display: none;
        }

        .notification-btn,
        .settings-btn {
            font-size: 1.1rem;
            padding: 0.4rem;
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

        .header-right {
            gap: 0.25rem;
        }
    }

    /* 접근성 개선 */
    @media (prefers-reduced-motion: reduce) {
        .mobile-menu-btn,
        .notification-btn,
        .settings-btn {
            transition: none;
        }
    }

    /* 포커스 스타일 */
    .mobile-menu-btn:focus,
    .notification-btn:focus,
    .settings-btn:focus {
        outline: 2px solid #3b82f6;
        outline-offset: 2px;
    }
</style>
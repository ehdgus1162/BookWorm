<!-- src/lib/components/layout/AdminSidebar.svelte -->
<script>
    import { page } from '$app/stores';
    import { createEventDispatcher } from 'svelte';

    export let currentUser = null;

    const dispatch = createEventDispatcher();

    // 메뉴 항목들
    const menuItems = [
        {
            href: '/admin/dashboard',
            icon: '📊',
            label: 'Dashboard',
            description: '대시보드 및 통계'
        },
        {
            href: '/admin/catalog',
            icon: '📚',
            label: 'Catalog',
            description: '도서 카탈로그'
        },
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
        {
            href: '/admin/branches',
            icon: '🏢',
            label: 'Branches',
            description: '지점 관리'
        }
    ];

    // 현재 페이지 확인
    $: currentPath = $page.url.pathname;

    // 메뉴 항목이 활성화되어 있는지 확인
    function isActiveMenu(href) {
        return currentPath === href || currentPath.startsWith(href + '/');
    }

    // 로그아웃 처리
    function handleLogout() {
        dispatch('logout');
    }
</script>

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
    {#if currentUser}
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
    {/if}

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

<style>
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
        height: 100vh;
        overflow-y: auto;
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
    }

    /* 접근성 개선 */
    @media (prefers-reduced-motion: reduce) {
        .menu-item,
        .logout-btn {
            transition: none;
        }
    }

    /* 스크롤바 스타일링 */
    .sidebar::-webkit-scrollbar {
        width: 6px;
    }

    .sidebar::-webkit-scrollbar-track {
        background: rgba(255, 255, 255, 0.05);
    }

    .sidebar::-webkit-scrollbar-thumb {
        background: rgba(255, 255, 255, 0.2);
        border-radius: 3px;
    }

    .sidebar::-webkit-scrollbar-thumb:hover {
        background: rgba(255, 255, 255, 0.3);
    }
</style>
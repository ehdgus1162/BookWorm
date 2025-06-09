<!-- frontend/src/routes/login/+page.svelte -->
<script>
    import { goto } from '$app/navigation';
    import { authApi } from '$lib/api.js';

    // 폼 데이터
    let email = '';
    let password = '';

    // 상태 관리
    let isLoading = false;
    let errorMessage = '';
    let successMessage = '';

    // 통합 로그인 처리 (일반 사용자 + 관리자)
    async function handleLogin() {
        // 기본 유효성 검사
        if (!email || !password) {
            errorMessage = '모든 필드를 입력해주세요.';
            return;
        }

        // 이메일 형식 검증
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            errorMessage = '올바른 이메일 형식을 입력해주세요.';
            return;
        }

        // 로딩 시작
        isLoading = true;
        errorMessage = '';
        successMessage = '';

        try {
            // API 함수 사용
            const data = await authApi.login(email, password);

            if (data.success) {
                const user = data.data;

                // 역할에 따라 다른 메시지 및 리다이렉트
                if (user.role === 'ADMIN') {
                    successMessage = '관리자 로그인 성공! 관리자 대시보드로 이동합니다...';
                    console.log('관리자 로그인 성공:', data);

                    setTimeout(() => {
                        goto('/admin/dashboard'); // 관리자 대시보드
                    }, 2000);
                } else if (user.role === 'USER') {
                    successMessage = '로그인 성공! 대시보드로 이동합니다...';
                    console.log('사용자 로그인 성공:', data);

                    setTimeout(() => {
                        goto('/dashboard'); // 일반 사용자 대시보드
                    }, 2000);
                } else {
                    // 예상치 못한 역할
                    errorMessage = '알 수 없는 사용자 역할입니다.';
                    console.error('알 수 없는 역할:', user.role);
                }
            } else {
                errorMessage = data.message || '로그인에 실패했습니다. 다시 시도해주세요.';
            }
        } catch (error) {
            console.error('로그인 에러:', error);
            errorMessage = error.message || '서버 연결에 실패했습니다. 다시 시도해주세요.';
        } finally {
            isLoading = false;
        }
    }

    /**
     * @param {Event} event
     */
    function handleSubmit(event) {
        event.preventDefault();
        handleLogin();
    }

    // 현재 사용자 정보 확인 (페이지 로드 시)
    async function checkCurrentUser() {
        try {
            const data = await authApi.getCurrentUser();

            if (data.success) {
                // 이미 로그인된 사용자인 경우 역할에 따라 리다이렉트
                console.log('이미 로그인된 사용자:', data.data);

                if (data.data.role === 'ADMIN') {
                    goto('/admin/dashboard');
                } else {
                    goto('/dashboard');
                }
            }
        } catch (error) {
            // 로그인되지 않은 상태는 정상 - 로그인 페이지 유지
            console.log('로그인 필요');
        }
    }

    // 페이지 로드 시 현재 사용자 상태 확인
    checkCurrentUser();
</script>

<svelte:head>
    <title>BookWorm 관리자 - 로그인</title>

    <!-- 기존 CSS 파일들 로드 -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/base/variables.css">
    <link rel="stylesheet" href="/css/base/reset.css">
    <link rel="stylesheet" href="/css/base/typography.css">
    <link rel="stylesheet" href="/css/layout/containers.css">
    <link rel="stylesheet" href="/css/layout/cards.css">
    <link rel="stylesheet" href="/css/components/panels.css">
    <link rel="stylesheet" href="/css/components/headers.css">
    <link rel="stylesheet" href="/css/components/brand.css">
    <link rel="stylesheet" href="/css/components/forms.css">
    <link rel="stylesheet" href="/css/components/buttons.css">
    <link rel="stylesheet" href="/css/components/alerts.css">
    <link rel="stylesheet" href="/css/utilities/spacing.css">
    <link rel="stylesheet" href="/css/utilities/text.css">
    <link rel="stylesheet" href="/css/utilities/layout.css">
    <link rel="stylesheet" href="/css/pages/login.css">
</svelte:head>

<div class="admin-login-page">
    <div class="container container--full-height container--centered">
        <div class="card--auth">
            <!-- 관리자 로그인 폼 패널 (왼쪽) -->
            <div class="panel panel--secondary">
                <!-- 알림 컨테이너 -->
                <div id="alertContainer" class="u-margin-bottom-md">
                    {#if successMessage}
                        <div class="alert alert-success" role="alert">
                            {successMessage}
                        </div>
                    {/if}

                    {#if errorMessage}
                        <div class="alert alert-danger" role="alert">
                            {errorMessage}
                            <button type="button" class="btn-close" on:click={() => errorMessage = ''}>&times;</button>
                        </div>
                    {/if}
                </div>

                <!-- 헤더 컴포넌트 -->
                <header class="header">
                    <div class="admin-badge">
                        <span class="admin-badge__icon">🛡️</span>
                        <span class="admin-badge__text">ADMIN</span>
                    </div>
                    <img src="/images/bookworm-logo.svg" alt="BookWorm Logo" class="header__icon-img">
                    <h2 class="header__title">관리자 로그인</h2>
                </header>
                <p class="header__subtitle">관리자 계정으로 로그인해주세요</p>

                <!-- 폼 컴포넌트 -->
                <form class="form" on:submit={handleSubmit}>
                    <div class="form__group">
                        <input
                                type="email"
                                class="form__control"
                                id="email"
                                name="email"
                                placeholder="관리자 이메일"
                                required
                                autocomplete="email"
                                bind:value={email}
                                disabled={isLoading}
                        >
                    </div>

                    <div class="form__group">
                        <input
                                type="password"
                                class="form__control"
                                id="password"
                                name="password"
                                placeholder="비밀번호"
                                required
                                autocomplete="current-password"
                                bind:value={password}
                                disabled={isLoading}
                        >
                    </div>

                    <div class="form__link">
                        <a href="/admin/forgot-password">비밀번호를 잊으셨나요?</a>
                    </div>

                    <button
                            type="submit"
                            class="btn btn--primary btn--large btn--full-width"
                            disabled={isLoading}
                    >
                        {#if isLoading}
                            로그인 중...
                        {:else}
                            관리자 로그인
                        {/if}
                    </button>
                </form>

                <!-- 관리자 전용 안내 -->
                <div class="admin-notice">
                    <p class="admin-notice__text">
                        <strong>관리자 전용 페이지입니다.</strong><br>
                        일반 사용자는 <a href="/login">일반 로그인</a>을 이용해주세요.
                    </p>
                </div>
            </div>

            <!-- 브랜딩 패널 (오른쪽) -->
            <div class="panel panel--primary panel--mobile-hidden">
                <div class="brand">
                    <img src="/images/bookworm-logo-white.svg" alt="BookWorm Logo" class="brand__logo">
                    <h1 class="brand__name">BookWorm</h1>
                    <p class="brand__subtitle">ADMIN PANEL</p>
                    <p class="brand__prompt">관리자 전용 시스템입니다</p>
                    <!-- 관리자 등록 링크 제거 -->
                </div>
            </div>
        </div>
    </div>
</div>

<style>
    /* 전체 페이지 스타일 */
    .admin-login-page {
        min-height: 100vh;
        background: linear-gradient(135deg, #1e3a8a 0%, #3730a3 50%, #581c87 100%);
    }

    /* 관리자 배지 */
    .admin-badge {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: var(--spacing-sm);
        margin-bottom: var(--spacing-md);
        padding: var(--spacing-sm) var(--spacing-lg);
        background: linear-gradient(135deg, #dc2626, #991b1b);
        color: white;
        border-radius: var(--border-radius-full);
        font-weight: 600;
        font-size: var(--font-size-sm);
        text-transform: uppercase;
        letter-spacing: 0.05em;
        box-shadow: 0 4px 15px rgba(220, 38, 38, 0.3);
        width: fit-content;
        margin-left: auto;
        margin-right: auto;
    }

    .admin-badge__icon {
        font-size: 1.2em;
    }

    /* 관리자 안내 */
    .admin-notice {
        margin-top: var(--spacing-lg);
        padding: var(--spacing-md);
        background: #f8fafc;
        border: 1px solid #e2e8f0;
        border-radius: var(--border-radius-lg);
        text-align: center;
    }

    .admin-notice__text {
        font-size: var(--font-size-sm);
        color: var(--text-secondary);
        line-height: 1.5;
    }

    .admin-notice__text a {
        color: var(--primary-color);
        text-decoration: none;
        font-weight: 500;
    }

    .admin-notice__text a:hover {
        text-decoration: underline;
    }

    /* 기존 스타일 오버라이드 */
    .card--auth {
        max-width: 1100px;
        width: 100%;
        display: grid;
        grid-template-columns: 1fr 1fr;
        min-height: 800px;
        border-radius: var(--border-radius-xl);
        overflow: hidden;
        box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
    }

    /* 관리자 패널 브랜딩 색상 */
    .panel--primary {
        background: linear-gradient(135deg, #1e40af, #3730a3);
    }

    /* 폼 스타일 개선 */
    :global(.admin-login-page .form__control) {
        border: 2px solid #e2e8f0;
        border-radius: var(--border-radius-lg);
        padding: var(--spacing-md);
        font-size: var(--font-size-sm);
        transition: all 0.2s ease;
    }

    :global(.admin-login-page .form__control:focus) {
        border-color: #3730a3;
        box-shadow: 0 0 0 3px rgba(55, 48, 163, 0.1);
    }

    /* 버튼 스타일 */
    :global(.admin-login-page .btn--primary) {
        background: linear-gradient(135deg, #3730a3, #1e40af);
        border: none;
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 0.05em;
    }

    :global(.admin-login-page .btn--primary:hover) {
        background: linear-gradient(135deg, #312e81, #1e3a8a);
        transform: translateY(-1px);
        box-shadow: 0 10px 25px rgba(55, 48, 163, 0.3);
    }

    /* 반응형 */
    @media (max-width: 768px) {
        .card--auth {
            grid-template-columns: 1fr;
            max-width: 400px;
            min-height: auto;
        }

        .admin-badge {
            font-size: var(--font-size-xs);
            padding: var(--spacing-sm) var(--spacing-md);
        }

        .admin-notice {
            margin-top: var(--spacing-md);
            padding: var(--spacing-sm);
        }

        .panel {
            min-height: auto;
        }
    }

    /* 버튼 로딩 상태 */
    button:disabled {
        opacity: 0.6;
        cursor: not-allowed;
        transform: none !important;
    }

    /* 알림 닫기 버튼 */
    .btn-close {
        background: none;
        border: none;
        color: inherit;
        font-size: 1.2em;
        cursor: pointer;
        float: right;
        line-height: 1;
    }
</style>
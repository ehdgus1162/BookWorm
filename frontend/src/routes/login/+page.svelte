<!-- frontend/src/routes/login/+page.svelte -->
<script>
    import { goto } from '$app/navigation';

    // 폼 데이터
    let email = '';
    let password = '';

    // 상태 관리
    let isLoading = false;
    let errorMessage = '';
    let successMessage = '';

    // 통합 로그인 처리 (일반 사용자 + 관리자) - 수정된 버전
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
            // 🔥 form-data 형식으로 수정
            const response = await fetch('http://localhost:8080/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'  // form-data
                },
                credentials: 'include', // 세션 쿠키 포함
                body: new URLSearchParams({
                    email: email,
                    password: password
                })
            });

            const data = await response.json();

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
                        goto('/user/dashboard'); // 일반 사용자 대시보드
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
            errorMessage = '서버 연결에 실패했습니다. 다시 시도해주세요.';
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
            const response = await fetch('http://localhost:8080/api/auth/me', {
                credentials: 'include'
            });

            if (response.ok) {
                const data = await response.json();
                if (data.success) {
                    // 이미 로그인된 사용자인 경우 역할에 따라 리다이렉트
                    console.log('이미 로그인된 사용자:', data.data);

                    if (data.data.role === 'ADMIN') {
                        goto('/admin/dashboard');
                    } else {
                        goto('/dashboard');
                    }
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
    <title>BookWorm Library - Sign In</title>

    <!-- 기존 CSS 파일들 로드 (Spring Boot에서) -->
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

<div class="login-page">
    <div class="container container--full-height container--centered">
        <div class="card--auth">
            <!-- 로그인 폼 패널 (왼쪽) -->
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
                    <img src="/images/bookworm-logo.svg" alt="BookWorm Logo" class="header__icon-img">
                    <h2 class="header__title">Welcome Back</h2>
                </header>
                <p class="header__subtitle">Please enter your credentials to log in</p>

                <!-- 폼 컴포넌트 -->
                <form class="form" on:submit={handleSubmit}>
                    <div class="form__group">
                        <input
                                type="email"
                                class="form__control"
                                id="email"
                                name="email"
                                placeholder="Email"
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
                                placeholder="Password"
                                required
                                autocomplete="current-password"
                                bind:value={password}
                                disabled={isLoading}
                        >
                    </div>

                    <div class="form__link">
                        <a href="/forgot-password">Forgot password?</a>
                    </div>

                    <button
                            type="submit"
                            class="btn btn--primary btn--large btn--full-width"
                            disabled={isLoading}
                    >
                        {#if isLoading}
                            SIGNING IN...
                        {:else}
                            SIGN IN
                        {/if}
                    </button>
                </form>

                <!-- 관리자 계정 안내 -->
                <div class="admin-info">
                    <details class="admin-details">
                        <summary class="admin-summary">👤 테스트 계정 정보</summary>
                        <div class="admin-content">
                            <p><strong>관리자 계정:</strong></p>
                            <p>📧 이메일: admin@bookworm.com</p>
                            <p>🔐 비밀번호: Admin123!</p>
                            <p class="admin-note">* 개발/테스트용 관리자 계정</p>

                            <hr style="margin: 1rem 0; border: none; border-top: 1px solid #e2e8f0;">

                            <p><strong>일반 사용자:</strong></p>
                            <p>일반 회원가입을 통해 생성된 계정으로 로그인</p>
                            <p class="admin-note">* role: "USER"로 설정됨</p>
                        </div>
                    </details>
                </div>
            </div>

            <!-- 브랜딩 패널 (오른쪽) -->
            <div class="panel panel--primary panel--mobile-hidden">
                <!-- 브랜딩 컴포넌트 -->
                <div class="brand">
                    <img src="/images/bookworm-logo-white.svg" alt="BookWorm Logo" class="brand__logo">
                    <h1 class="brand__name">BookWorm</h1>
                    <p class="brand__subtitle">LIBRARY</p>
                    <p class="brand__prompt">New to our platform? Sign Up now.</p>
                    <a href="/signup" class="btn btn--outline-white btn--large">SIGN UP</a>
                </div>
            </div>
        </div>
    </div>
</div>

<style>
    /* 전체 페이지에 body 클래스와 동일한 스타일 적용 */
    .login-page {
        min-height: 100vh;
        background: var(--background-color, #f8fafc);
    }

    /* 기존 스타일에서 필요한 부분만 추가 */
    .card--auth {
        max-width: 1100px;
        width: 100%;
        display: grid;
        grid-template-columns: 1fr 1fr;
        min-height: 800px;
    }

    /* 관리자 정보 표시 */
    .admin-info {
        margin-top: var(--spacing-lg);
        padding: var(--spacing-md);
        background: #f8fafc;
        border: 1px solid #e2e8f0;
        border-radius: var(--border-radius-lg);
    }

    .admin-details {
        cursor: pointer;
    }

    .admin-summary {
        font-weight: 600;
        color: var(--text-primary);
        list-style: none;
        padding: var(--spacing-sm);
        border-radius: var(--border-radius-md);
        background: #fff;
        border: 1px solid #cbd5e1;
        transition: all 0.2s ease;
    }

    .admin-summary:hover {
        background: #f1f5f9;
        border-color: #94a3b8;
    }

    .admin-summary::-webkit-details-marker {
        display: none;
    }

    .admin-content {
        margin-top: var(--spacing-sm);
        padding: var(--spacing-md);
        background: #fff;
        border-radius: var(--border-radius-md);
        border: 1px solid #e2e8f0;
    }

    .admin-content p {
        margin-bottom: var(--spacing-xs);
        font-size: var(--font-size-sm);
        color: var(--text-secondary);
    }

    .admin-content strong {
        color: var(--text-primary);
    }

    .admin-note {
        font-style: italic;
        color: var(--text-muted) !important;
        border-top: 1px solid #f1f5f9;
        padding-top: var(--spacing-sm);
        margin-top: var(--spacing-sm);
    }

    /* 반응형 */
    @media (max-width: 768px) {
        .card--auth {
            grid-template-columns: 1fr;
            max-width: 400px;
            min-height: auto;
        }

        .admin-info {
            margin-top: var(--spacing-md);
            padding: var(--spacing-sm);
        }

        .admin-content {
            padding: var(--spacing-sm);
        }

        .panel {
            min-height: auto;
        }

        :global(.brand__name) {
            font-size: var(--font-size-3xl);
        }

        :global(.header__title) {
            font-size: var(--font-size-2xl);
        }
    }

    /* 버튼 로딩 상태 */
    button:disabled {
        opacity: 0.6;
        cursor: not-allowed;
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
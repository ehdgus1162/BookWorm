<!-- frontend/src/routes/admin/signup/+page.svelte -->
<script>
    import { goto } from '$app/navigation';
    import { authApi } from '$lib/api.js';  // 이 줄 추가

    // 폼 데이터 - 관리자는 더 간단한 정보만 수집
    let formData = {
        name: '',
        email: '',
        password: '',
        confirmPassword: '',
        adminCode: '' // 관리자 등록 코드
    };

    // 상태 관리
    let isLoading = false;
    let errorMessage = '';
    let successMessage = '';
    let emailChecked = false;
    let emailValid = false;
    let emailStatusMessage = '';
    let emailStatusType = '';

    // 비밀번호 요구사항 상태
    let passwordRequirements = {
        length: false,
        uppercase: false,
        lowercase: false,
        number: false,
        special: false
    };

    // 비밀번호 일치 여부
    let passwordsMatch = false;

    // 비밀번호 실시간 검증
    function validatePassword() {
        const password = formData.password;

        passwordRequirements = {
            length: password.length >= 8,
            uppercase: /[A-Z]/.test(password),
            lowercase: /[a-z]/.test(password),
            number: /\d/.test(password),
            special: /[!@#$%^&*(),.?":{}|<>]/.test(password)
        };

        // 비밀번호 확인 일치 여부
        passwordsMatch = formData.password === formData.confirmPassword && formData.confirmPassword !== '';
    }

    // 비밀번호 확인 검증
    function validatePasswordConfirm() {
        passwordsMatch = formData.password === formData.confirmPassword && formData.confirmPassword !== '';
    }
    // 이메일 중복확인 - API 유틸리티 사용
    async function checkEmailDuplicate() {
        const email = formData.email.trim();

        if (!email) {
            showEmailStatus('이메일을 입력해주세요.', 'error');
            return;
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            showEmailStatus('올바른 이메일 형식을 입력해주세요.', 'error');
            return;
        }

        showEmailStatus('이메일 중복을 확인하고 있습니다...', 'loading');

        try {
            // API 유틸리티 사용 - 훨씬 간단함!
            const data = await authApi.checkEmail(email);

            if (data.success) {
                if (data.data) {
                    // 이메일이 이미 존재함
                    showEmailStatus('이미 사용 중인 이메일입니다.', 'error');
                    emailChecked = false;
                    emailValid = false;
                } else {
                    // 이메일 사용 가능
                    showEmailStatus('사용 가능한 이메일입니다.', 'success');
                    emailChecked = true;
                    emailValid = true;
                }
            } else {
                showEmailStatus(data.message || '중복확인 중 오류가 발생했습니다.', 'error');
                emailChecked = false;
                emailValid = false;
            }
        } catch (error) {
            console.error('이메일 중복확인 에러:', error);
            // API 유틸리티에서 더 정확한 오류 메시지 제공
            showEmailStatus(error.message || '중복확인 중 오류가 발생했습니다.', 'error');
            emailChecked = false;
            emailValid = false;
        }
    }

    // 이메일 상태 메시지 표시
    function showEmailStatus(message, type) {
        emailStatusMessage = message;
        emailStatusType = type;
    }

    // 폼 검증
    function validateForm() {
        // 필수 필드 확인
        const requiredFields = ['name', 'email', 'password', 'confirmPassword', 'adminCode'];

        for (const field of requiredFields) {
            if (!formData[field] || formData[field].trim() === '') {
                errorMessage = `${getFieldLabel(field)}을(를) 입력해주세요.`;
                return false;
            }
        }

        // 이메일 형식 검증
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(formData.email)) {
            errorMessage = '올바른 이메일 형식을 입력해주세요.';
            return false;
        }

        // 비밀번호 검증
        if (!Object.values(passwordRequirements).every(req => req)) {
            errorMessage = '비밀번호가 요구사항을 만족하지 않습니다.';
            return false;
        }

        // 비밀번호 확인 검증
        if (!passwordsMatch) {
            errorMessage = '비밀번호가 일치하지 않습니다.';
            return false;
        }

        // 이메일 중복확인 체크
        if (!emailChecked) {
            errorMessage = '이메일 중복확인을 먼저 진행해주세요.';
            return false;
        }

        return true;
    }

    // 필드 라벨 반환
    function getFieldLabel(field) {
        const labels = {
            name: '이름',
            email: '이메일',
            password: '비밀번호',
            confirmPassword: '비밀번호 확인',
            adminCode: '관리자 등록 코드'
        };
        return labels[field] || field;
    }

    // 회원가입 처리 - API 유틸리티 사용
    async function handleSignup() {
        if (!validateForm()) {
            return;
        }

        isLoading = true;
        errorMessage = '';
        successMessage = '';

        try {
            // API 유틸리티 사용 - 훨씬 간단함!
            const data = await authApi.signup(formData);

            if (data.success) {
                successMessage = '회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.';
                console.log('회원가입 성공:', data);

                setTimeout(() => {
                    goto('/login');
                }, 2000);
            } else {
                errorMessage = data.message || '회원가입 중 오류가 발생했습니다.';
            }
        } catch (error) {
            console.error('회원가입 에러:', error);
            // API 유틸리티에서 더 정확한 오류 메시지 제공
            errorMessage = error.message || '서버 연결에 실패했습니다. 다시 시도해주세요.';
        } finally {
            isLoading = false;
        }
    }

    // 폼 제출 이벤트
    function handleSubmit(event) {
        event.preventDefault();
        handleSignup();
    }

    // 이메일 입력 시 중복확인 상태 초기화
    function handleEmailInput() {
        emailChecked = false;
        emailValid = false;
        emailStatusMessage = '';
    }
</script>

<svelte:head>
    <title>BookWorm 관리자 - 등록</title>

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
    <link rel="stylesheet" href="/css/pages/signup.css">
</svelte:head>

<div class="admin-signup-page">
    <div class="container container--full-height container--centered">
        <div class="card--auth">
            <!-- 브랜딩 패널 (왼쪽) -->
            <div class="panel panel--primary">
                <div class="brand">
                    <img src="/images/bookworm-logo-white.svg" alt="BookWorm Logo" class="brand__logo">
                    <h1 class="brand__name">BookWorm</h1>
                    <p class="brand__subtitle">ADMIN PANEL</p>
                    <p class="brand__prompt">이미 관리자 계정이 있으신가요?</p>
                    <a href="/admin/login" class="btn btn--outline-white btn--large">관리자 로그인</a>
                </div>
            </div>

            <!-- 관리자 회원가입 폼 패널 (오른쪽) -->
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
                    <h2 class="header__title">관리자 등록</h2>
                </header>
                <p class="header__subtitle">관리자 계정 생성을 위한 정보를 입력해주세요</p>

                <!-- 관리자 회원가입 폼 -->
                <form class="form" on:submit={handleSubmit}>
                    <!-- 이름 -->
                    <div class="form__group">
                        <input
                                type="text"
                                class="form__control"
                                placeholder="관리자 이름"
                                bind:value={formData.name}
                                disabled={isLoading}
                                required
                        >
                    </div>

                    <!-- 이메일 -->
                    <div class="form__group">
                        <div class="input-group">
                            <input
                                    type="email"
                                    class="form__control"
                                    placeholder="관리자 이메일"
                                    bind:value={formData.email}
                                    on:input={handleEmailInput}
                                    disabled={isLoading}
                                    required
                            >
                            <button
                                    type="button"
                                    class="btn btn--secondary"
                                    on:click={checkEmailDuplicate}
                                    disabled={isLoading}
                            >
                                중복확인
                            </button>
                        </div>
                        {#if emailStatusMessage}
                            <div class="status-message {emailStatusType}">
                                {emailStatusMessage}
                            </div>
                        {/if}
                    </div>

                    <!-- 비밀번호 -->
                    <div class="form__group">
                        <input
                                type="password"
                                class="form__control"
                                placeholder="비밀번호"
                                bind:value={formData.password}
                                on:input={validatePassword}
                                disabled={isLoading}
                                required
                        >
                        <div class="password-requirements">
                            <div class="requirement" class:valid={passwordRequirements.length}>
                                <span>{passwordRequirements.length ? '✓' : '✗'}</span> 최소 8자 이상
                            </div>
                            <div class="requirement" class:valid={passwordRequirements.uppercase}>
                                <span>{passwordRequirements.uppercase ? '✓' : '✗'}</span> 대문자 포함
                            </div>
                            <div class="requirement" class:valid={passwordRequirements.lowercase}>
                                <span>{passwordRequirements.lowercase ? '✓' : '✗'}</span> 소문자 포함
                            </div>
                            <div class="requirement" class:valid={passwordRequirements.number}>
                                <span>{passwordRequirements.number ? '✓' : '✗'}</span> 숫자 포함
                            </div>
                            <div class="requirement" class:valid={passwordRequirements.special}>
                                <span>{passwordRequirements.special ? '✓' : '✗'}</span> 특수문자 포함
                            </div>
                        </div>
                    </div>

                    <!-- 비밀번호 확인 -->
                    <div class="form__group">
                        <input
                                type="password"
                                class="form__control"
                                placeholder="비밀번호 확인"
                                bind:value={formData.confirmPassword}
                                on:input={validatePasswordConfirm}
                                disabled={isLoading}
                                required
                        >
                        {#if formData.confirmPassword}
                            <div class="password-match" class:valid={passwordsMatch} class:invalid={!passwordsMatch}>
                                {#if passwordsMatch}
                                    <span>✓</span> 비밀번호가 일치합니다
                                {:else}
                                    <span>✗</span> 비밀번호가 일치하지 않습니다
                                {/if}
                            </div>
                        {/if}
                    </div>

                    <!-- 관리자 등록 코드 -->
                    <div class="form__group">
                        <input
                                type="text"
                                class="form__control admin-code-input"
                                placeholder="관리자 등록 코드"
                                bind:value={formData.adminCode}
                                disabled={isLoading}
                                required
                        >
                        <div class="admin-code-notice">
                            <p>관리자 등록 코드는 시스템 관리자에게 문의하여 받을 수 있습니다.</p>
                        </div>
                    </div>

                    <!-- 제출 버튼 -->
                    <button
                            type="submit"
                            class="btn btn--primary btn--large btn--full-width"
                            disabled={isLoading}
                    >
                        {#if isLoading}
                            관리자 등록 중...
                        {:else}
                            관리자 등록
                        {/if}
                    </button>
                </form>

                <!-- 일반 사용자 안내 -->
                <div class="user-notice">
                    <p class="user-notice__text">
                        일반 사용자이신가요? <a href="/signup">일반 회원가입</a>을 이용해주세요.
                    </p>
                </div>
            </div>
        </div>
    </div>
</div>

<style>
    /* 전체 페이지 스타일 */
    .admin-signup-page {
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

    /* 기존 스타일들 */
    .input-group {
        display: flex;
        gap: 0;
        border-radius: var(--border-radius-lg);
        overflow: hidden;
        border: 2px solid #e8e8e8;
    }

    .input-group .form__control {
        border: none;
        border-radius: 0;
        flex: 1;
    }

    .input-group .form__control:focus {
        box-shadow: none;
    }

    .input-group .btn {
        border-radius: 0;
        border: none;
        border-left: 1px solid var(--border-color);
        font-size: var(--font-size-xs);
        padding: var(--spacing-xs) var(--spacing-md);
        white-space: nowrap;
    }

    .password-requirements {
        margin-top: var(--spacing-sm);
        font-size: var(--font-size-xs);
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: var(--spacing-xs);
    }

    .requirement {
        display: flex;
        align-items: center;
        gap: var(--spacing-sm);
        color: var(--text-muted);
        transition: var(--transition);
    }

    .requirement.valid {
        color: #28a745;
    }

    .requirement:not(.valid) {
        color: #dc3545;
    }

    .requirement span {
        font-weight: bold;
        width: 12px;
        text-align: center;
    }

    /* 비밀번호 일치 확인 */
    .password-match {
        font-size: var(--font-size-xs);
        margin-top: var(--spacing-xs);
        display: flex;
        align-items: center;
        gap: var(--spacing-sm);
        transition: var(--transition);
    }

    .password-match.valid {
        color: #28a745;
    }

    .password-match.invalid {
        color: #dc3545;
    }

    .password-match span {
        font-weight: bold;
    }

    .status-message {
        font-size: var(--font-size-xs);
        margin-top: var(--spacing-xs);
        display: flex;
        align-items: center;
        gap: var(--spacing-sm);
        transition: var(--transition);
    }

    .status-message.success {
        color: #28a745;
    }

    .status-message.error {
        color: #dc3545;
    }

    .status-message.loading {
        color: var(--text-secondary);
    }

    /* 관리자 코드 입력 */
    .admin-code-input {
        font-family: 'Courier New', monospace;
        font-weight: 600;
        letter-spacing: 0.1em;
        text-transform: uppercase;
    }

    .admin-code-notice {
        margin-top: var(--spacing-sm);
        padding: var(--spacing-sm);
        background: #fff3cd;
        border: 1px solid #ffeaa7;
        border-radius: var(--border-radius-md);
        font-size: var(--font-size-xs);
        color: #856404;
        text-align: center;
    }

    /* 일반 사용자 안내 */
    .user-notice {
        margin-top: var(--spacing-lg);
        padding: var(--spacing-md);
        background: #f8fafc;
        border: 1px solid #e2e8f0;
        border-radius: var(--border-radius-lg);
        text-align: center;
    }

    .user-notice__text {
        font-size: var(--font-size-sm);
        color: var(--text-secondary);
        line-height: 1.5;
    }

    .user-notice__text a {
        color: var(--primary-color);
        text-decoration: none;
        font-weight: 500;
    }

    .user-notice__text a:hover {
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
    :global(.admin-signup-page .form__control) {
        border: 2px solid #e2e8f0;
        border-radius: var(--border-radius-lg);
        padding: var(--spacing-md);
        font-size: var(--font-size-sm);
        transition: all 0.2s ease;
    }

    :global(.admin-signup-page .form__control:focus) {
        border-color: #3730a3;
        box-shadow: 0 0 0 3px rgba(55, 48, 163, 0.1);
    }

    /* 버튼 스타일 */
    :global(.admin-signup-page .btn--primary) {
        background: linear-gradient(135deg, #3730a3, #1e40af);
        border: none;
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 0.05em;
    }

    :global(.admin-signup-page .btn--primary:hover) {
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

        .password-requirements {
            grid-template-columns: 1fr;
        }

        .input-group .btn {
            font-size: var(--font-size-xs);
            padding: var(--spacing-xs);
        }

        .admin-badge {
            font-size: var(--font-size-xs);
            padding: var(--spacing-sm) var(--spacing-md);
        }

        .admin-code-notice,
        .user-notice {
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
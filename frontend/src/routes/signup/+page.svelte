<!-- frontend/src/routes/signup/+page.svelte -->
<script>
    import { goto } from '$app/navigation';

    // 폼 데이터
    let formData = {
        firstName: '',
        lastName: '',
        email: '',
        phoneNumber: '',
        password: '',
        country: '',
        city: '',
        state: '',
        street: ''
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
    }

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
            const response = await fetch(`http://localhost:8080/api/auth/check-email?email=${encodeURIComponent(email)}`, {
                credentials: 'include'
            });

            const data = await response.json();

            // 이 부분이 완전히 누락되어 있었음!
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
            showEmailStatus('중복확인 중 오류가 발생했습니다.', 'error');
            emailChecked = false;
            emailValid = false;
        }
    }

    // 이메일 상태 메시지 표시
    /**
     * @param {string} message
     * @param {string} type
     */
    function showEmailStatus(message, type) {
        emailStatusMessage = message;
        emailStatusType = type;
    }

    // 폼 검증
    function validateForm() {
        // 필수 필드 확인
        const requiredFields = ['firstName', 'lastName', 'email', 'phoneNumber', 'password', 'country', 'city', 'street'];

        for (const field of requiredFields) {
            if (!formData[/** @type {keyof typeof formData} */(field)] || formData[/** @type {keyof typeof formData} */(field)].trim() === '') {
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

        // 전화번호 형식 검증
        const phoneRegex = /^010-\d{4}-\d{4}$/;
        if (!phoneRegex.test(formData.phoneNumber)) {
            errorMessage = '전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)';
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
    /**
     * @param {string} field
     * @returns {string}
     */
    function getFieldLabel(field) {
        const labels = {
            firstName: '이름',
            lastName: '성',
            email: '이메일',
            phoneNumber: '전화번호',
            password: '비밀번호',
            country: '국가',
            city: '도시',
            street: '주소'
        };
        return labels[/** @type {keyof typeof labels} */(field)] || field;
    }

    // 회원가입 처리
    async function handleSignup() {
        if (!validateForm()) {
            return;
        }

        isLoading = true;
        errorMessage = '';
        successMessage = '';

        try {
            const response = await fetch('http://localhost:8080/api/auth/signup', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify(formData)
            });

            const data = await response.json();

            // 이 부분도 완전히 누락되어 있었음!
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
            errorMessage = '서버 연결에 실패했습니다. 다시 시도해주세요.';
        } finally {
            isLoading = false;
        }
    }

    // 폼 제출 이벤트
    /**
     * @param {Event} event
     */
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
    <title>BookWorm Library - Sign Up</title>

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

<div class="signup-page">
    <div class="container container--full-height container--centered">
        <div class="card--auth">
            <!-- 브랜딩 패널 (왼쪽) -->
            <div class="panel panel--primary">
                <div class="brand">
                    <img src="/images/bookworm-logo-white.svg" alt="BookWorm Logo" class="brand__logo">
                    <h1 class="brand__name">BookWorm</h1>
                    <p class="brand__subtitle">LIBRARY</p>
                    <p class="brand__prompt">Already have Account? Sign In now.</p>
                    <a href="/login" class="btn btn--outline-white btn--large">SIGN IN</a>
                </div>
            </div>

            <!-- 회원가입 폼 패널 (오른쪽) -->
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
                    <h2 class="header__title">Sign Up</h2>
                </header>
                <p class="header__subtitle">Please provide your information to sign up.</p>

                <!-- 회원가입 폼 -->
                <form class="form" on:submit={handleSubmit}>
                    <!-- 이름 행 -->
                    <div class="form__row">
                        <div class="form__group">
                            <input
                                    type="text"
                                    class="form__control"
                                    placeholder="First Name"
                                    bind:value={formData.firstName}
                                    disabled={isLoading}
                                    required
                            >
                        </div>
                        <div class="form__group">
                            <input
                                    type="text"
                                    class="form__control"
                                    placeholder="Last Name"
                                    bind:value={formData.lastName}
                                    disabled={isLoading}
                                    required
                            >
                        </div>
                    </div>

                    <!-- 이메일 -->
                    <div class="form__group">
                        <div class="input-group">
                            <input
                                    type="email"
                                    class="form__control"
                                    placeholder="Email"
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

                    <!-- 전화번호 -->
                    <div class="form__group">
                        <input
                                type="text"
                                class="form__control"
                                placeholder="Phone Number (010-1234-5678)"
                                bind:value={formData.phoneNumber}
                                disabled={isLoading}
                                required
                        >
                    </div>

                    <!-- 비밀번호 -->
                    <div class="form__group">
                        <input
                                type="password"
                                class="form__control"
                                placeholder="Password"
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

                    <!-- 국가 -->
                    <div class="form__group">
                        <select
                                class="form__select"
                                bind:value={formData.country}
                                disabled={isLoading}
                                required
                        >
                            <option value="">국가 선택</option>
                            <option value="대한민국">대한민국</option>
                            <option value="미국">미국</option>
                            <option value="일본">일본</option>
                            <option value="중국">중국</option>
                            <option value="영국">영국</option>
                        </select>
                    </div>

                    <!-- 도시/주 행 -->
                    <div class="form__row">
                        <div class="form__group">
                            <input
                                    type="text"
                                    class="form__control"
                                    placeholder="City"
                                    bind:value={formData.city}
                                    disabled={isLoading}
                                    required
                            >
                        </div>
                        <div class="form__group">
                            <input
                                    type="text"
                                    class="form__control"
                                    placeholder="State/Province"
                                    bind:value={formData.state}
                                    disabled={isLoading}
                            >
                        </div>
                    </div>

                    <!-- 주소 -->
                    <div class="form__group">
                        <input
                                type="text"
                                class="form__control"
                                placeholder="Street Address"
                                bind:value={formData.street}
                                disabled={isLoading}
                                required
                        >
                    </div>

                    <!-- 제출 버튼 -->
                    <button
                            type="submit"
                            class="btn btn--primary btn--large btn--full-width"
                            disabled={isLoading}
                    >
                        {#if isLoading}
                            SIGNING UP...
                        {:else}
                            SIGN UP
                        {/if}
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>

<style>
    /* 전체 페이지 스타일 */
    .signup-page {
        min-height: 100vh;
        background: var(--background-color, #f8fafc);
    }

    /* 기존 스타일들 */
    .form__row {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: var(--spacing-md);
    }

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

    /* 폼 스타일 오버라이드 */
    :global(.signup-page .form__control) {
        border-radius: var(--border-radius-lg);
        border: 2px solid #e8e8e8;
    }

    :global(.signup-page .form__control:focus) {
        border-color: var(--primary-color);
    }

    .form__select {
        width: 100%;
        padding: var(--spacing-sm) var(--spacing-md);
        font-size: var(--font-size-sm);
        border-radius: var(--border-radius-lg);
        border: 2px solid #e8e8e8;
        background: var(--secondary-color);
        color: var(--text-primary);
        transition: var(--transition);
    }

    .form__select:focus {
        outline: none;
        border-color: var(--primary-color);
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

    /* 반응형 */
    @media (max-width: 768px) {
        .form__row {
            grid-template-columns: 1fr;
            gap: var(--spacing-sm);
        }

        .password-requirements {
            grid-template-columns: 1fr;
        }

        .input-group .btn {
            font-size: var(--font-size-xs);
            padding: var(--spacing-xs);
        }
    }
</style>
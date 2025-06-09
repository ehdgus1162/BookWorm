<!-- frontend/src/routes/+page.svelte -->
<script>
    import { onMount } from 'svelte';

    let apiTest = {
        loading: false,
        result: null,
        error: null
    };

    // API 테스트 함수
    async function testAPI() {
        apiTest.loading = true;
        apiTest.error = null;
        apiTest.result = null;

        try {
            // Spring Boot API 테스트 - 실제 이메일 중복확인 API 호출
            const response = await fetch('/api/auth/check-email?email=test@example.com', {
                credentials: 'include' // 세션 쿠키 포함
            });

            const data = await response.json();
            apiTest.result = data;

            console.log('API 테스트 성공:', data);
        } catch (error) {
            apiTest.error = error.message;
            console.error('API 테스트 실패:', error);
        } finally {
            apiTest.loading = false;
        }
    }

    onMount(() => {
        console.log('Svelte 페이지 로드됨');
    });
</script>

<svelte:head>
    <title>BookWorm Library - Svelte Test</title>
</svelte:head>

<div class="container">
    <h1>🔥 BookWorm Library - Svelte 연동 테스트</h1>

    <div class="test-section">
        <h2>Spring Boot API 연결 테스트</h2>
        <p>이메일 중복확인 API를 테스트합니다.</p>

        <button on:click={testAPI} disabled={apiTest.loading}>
            {#if apiTest.loading}
                테스트 중...
            {:else}
                API 테스트 실행
            {/if}
        </button>

        {#if apiTest.result}
            <div class="result success">
                <h3>✅ API 연결 성공!</h3>
                <pre>{JSON.stringify(apiTest.result, null, 2)}</pre>
            </div>
        {/if}

        {#if apiTest.error}
            <div class="result error">
                <h3>❌ API 연결 실패</h3>
                <p>{apiTest.error}</p>
                <p><strong>해결 방법:</strong></p>
                <ul>
                    <li>Spring Boot 애플리케이션이 8080 포트에서 실행 중인지 확인</li>
                    <li>CORS 설정이 올바른지 확인</li>
                    <li>브라우저 개발자 도구의 Network 탭에서 요청 확인</li>
                </ul>
            </div>
        {/if}
    </div>

    <div class="navigation">
        <h2>🎯 페이지 테스트</h2>
        <p>다음 페이지들을 테스트해보세요:</p>
        <ul>
            <li><a href="/login">로그인 페이지 (Svelte 버전)</a></li>
            <li><a href="/signup">회원가입 페이지 (Svelte 버전)</a></li>
        </ul>
        <p><strong>기존 시스템:</strong> <a href="http://localhost:8080/web/login" target="_blank">로그인 (Thymeleaf)</a></p>
    </div>
</div>

<style>
    .container {
        max-width: 800px;
        margin: 0 auto;
        padding: 2rem;
        font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    }

    h1 {
        color: #2563eb;
        text-align: center;
        margin-bottom: 2rem;
    }

    .test-section {
        background: #f8fafc;
        padding: 1.5rem;
        border-radius: 8px;
        margin: 1rem 0;
    }

    button {
        background: #2563eb;
        color: white;
        border: none;
        padding: 0.75rem 1.5rem;
        border-radius: 6px;
        cursor: pointer;
        font-size: 1rem;
        margin: 1rem 0;
    }

    button:disabled {
        background: #94a3b8;
        cursor: not-allowed;
    }

    button:hover:not(:disabled) {
        background: #1d4ed8;
    }

    .result {
        margin: 1rem 0;
        padding: 1rem;
        border-radius: 6px;
        border-left: 4px solid;
    }

    .result.success {
        background: #f0fdf4;
        border-color: #22c55e;
        color: #15803d;
    }

    .result.error {
        background: #fef2f2;
        border-color: #ef4444;
        color: #dc2626;
    }

    pre {
        background: #1e293b;
        color: #e2e8f0;
        padding: 1rem;
        border-radius: 4px;
        overflow-x: auto;
        font-size: 0.875rem;
    }

    .navigation {
        background: #e0f2fe;
        padding: 1.5rem;
        border-radius: 8px;
        margin-top: 2rem;
    }

    .navigation ul {
        margin: 0.5rem 0;
    }

    .navigation a {
        color: #0369a1;
        text-decoration: none;
        font-weight: 500;
    }

    .navigation a:hover {
        text-decoration: underline;
    }
</style>
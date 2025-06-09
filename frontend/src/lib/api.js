// src/lib/api.js
// API 요청을 위한 공통 유틸리티 함수

// 환경 변수에서 API 기본 URL 가져오기
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * API 요청을 위한 공통 fetch 함수
 * @param {string} endpoint - API 엔드포인트 (예: '/api/auth/login')
 * @param {RequestInit} options - fetch 옵션
 * @returns {Promise<Response>} - fetch 응답
 */
export async function apiRequest(endpoint, options = {}) {
    // 기본 헤더 설정
    const defaultHeaders = {
        'Content-Type': 'application/json',
    };

    // 기본 옵션 설정
    const defaultOptions = {
        credentials: 'include', // 세션 쿠키 포함
        headers: defaultHeaders,
    };

    // 옵션 병합
    const mergedOptions = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultHeaders,
            ...options.headers,
        },
    };

    // 완전한 URL 생성
    const url = `${API_BASE_URL}${endpoint}`;

    console.log(`API 요청: ${mergedOptions.method || 'GET'} ${url}`);

    try {
        const response = await fetch(url, mergedOptions);

        // 응답 상태 로깅
        console.log(`API 응답: ${response.status} ${response.statusText}`);

        return response;
    } catch (error) {
        console.error(`API 요청 실패: ${error.message}`);
        throw error;
    }
}

/**
 * JSON 응답을 처리하는 헬퍼 함수
 * @param {Response} response - fetch 응답
 * @returns {Promise<any>} - 파싱된 JSON 데이터
 */
export async function handleJsonResponse(response) {
    const contentType = response.headers.get('content-type');

    const rawText = await response.clone().text(); // 응답 내용 복사해서 원시 텍스트 확보
    console.log('📋 Raw Response Body:', rawText);

    if (!contentType || !contentType.includes('application/json')) {
        throw new Error(`서버가 JSON이 아닌 응답을 반환했습니다: ${rawText}`);
    }

    let data;

    // JSON 파싱 시도
    try {
        data = JSON.parse(rawText);
    } catch (parseError) {
        console.error('❌ JSON 파싱 실패:', rawText);
        throw new Error(`JSON 파싱 오류: ${parseError.message}`);
    }

    // 파싱 성공 후 응답 상태 확인
    if (!response.ok) {
        // HTTP 상태 코드별 상세 에러 메시지
        if (response.status === 401) {
            throw new Error('인증이 필요합니다.');
        } else if (response.status === 403) {
            throw new Error('접근 권한이 없습니다.');
        } else if (response.status === 404) {
            throw new Error('요청한 리소스를 찾을 수 없습니다.');
        }

        throw new Error(data.message || `HTTP error! status: ${response.status}`);
    }

    return data;
}

/**
 * 🎯 통합 인증 API (일반 사용자 + 관리자 모두 지원)
 * AuthController의 통합 로그인 시스템 사용
 *
 * ✅ 백엔드 로그에서 확인: /api/auth/me는 정상 작동
 * ✅ 세션 기반 인증이 올바르게 처리됨
 */
export const unifiedAuthApi = {
    /**
     * 통합 로그인 (일반 사용자 + 관리자)
     * @param {string} email
     * @param {string} password
     */
    async login(email, password) {
        // 🔥 Spring Security 폼 로그인에 맞춘 form-data 형식
        const response = await apiRequest('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'  // JSON 대신 form-data
            },
            body: new URLSearchParams({  // JSON.stringify 대신 URLSearchParams
                email: email,
                password: password
            })
        });

        return handleJsonResponse(response);
    },
    /**
     * 현재 사용자 정보 조회 (일반 사용자 + 관리자 모두 지원)
     * ⭐ 핵심: 이 API는 백엔드에서 정상 작동 확인됨
     */
    async getCurrentUser() {
        const response = await apiRequest('/api/auth/me');
        return handleJsonResponse(response);
    },

    /**
     * 통합 로그아웃
     */
    async logout() {
        const response = await apiRequest('/api/auth/logout', {
            method: 'POST',
        });

        return handleJsonResponse(response);
    },

    /**
     * 이메일 중복 확인 (통합)
     * @param {string} email
     */
    async checkEmail(email) {
        const response = await apiRequest(`/api/auth/check-email?email=${encodeURIComponent(email)}`);
        return handleJsonResponse(response);
    },

    /**
     * 회원가입 (일반 사용자)
     * @param {object} userData - 회원가입 데이터
     */
    async signup(userData) {
        const response = await apiRequest('/api/auth/signup', {
            method: 'POST',
            body: JSON.stringify(userData),
        });

        return handleJsonResponse(response);
    }
};

/**
 * 일반 사용자 인증 관련 API 함수들 (하위 호환성 유지)
 */
export const authApi = {
    /**
     * 일반 사용자 로그인
     * @param {string} email
     * @param {string} password
     */
    async login(email, password) {
        return unifiedAuthApi.login(email, password);
    },

    /**
     * 일반 사용자 회원가입
     * @param {object} userData - 회원가입 데이터
     */
    async signup(userData) {
        return unifiedAuthApi.signup(userData);
    },

    /**
     * 이메일 중복 확인 (일반 사용자)
     * @param {string} email
     */
    async checkEmail(email) {
        return unifiedAuthApi.checkEmail(email);
    },

    /**
     * 현재 일반 사용자 정보 조회
     */
    async getCurrentUser() {
        return unifiedAuthApi.getCurrentUser();
    },

    /**
     * 일반 사용자 로그아웃
     */
    async logout() {
        return unifiedAuthApi.logout();
    }
};

/**
 * 관리자 인증 관련 API 함수들
 * ⚠️ 주의: /api/admin/auth/me는 현재 세션 인식 문제가 있음
 * 대신 unifiedAuthApi 사용 권장
 */
export const adminAuthApi = {
    /**
     * 관리자 로그인
     * @param {string} email
     * @param {string} password
     */
    async login(email, password) {
        const response = await apiRequest('/api/admin/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password }),
        });

        return handleJsonResponse(response);
    },

    /**
     * 관리자 회원가입
     * @param {object} adminData - 관리자 회원가입 데이터
     */
    async signup(adminData) {
        const response = await apiRequest('/api/admin/auth/signup', {
            method: 'POST',
            body: JSON.stringify(adminData),
        });

        return handleJsonResponse(response);
    },

    /**
     * 관리자 이메일 중복 확인
     * @param {string} email
     */
    async checkEmail(email) {
        const response = await apiRequest(`/api/admin/auth/check-email?email=${encodeURIComponent(email)}`);
        return handleJsonResponse(response);
    },

    /**
     * 현재 관리자 정보 조회
     * ⚠️ 현재 세션 인식 문제로 401 에러 발생
     * 대신 unifiedAuthApi.getCurrentUser() 사용 권장
     */
    async getCurrentAdmin() {
        const response = await apiRequest('/api/admin/auth/me');
        return handleJsonResponse(response);
    },

    /**
     * 관리자 로그아웃
     * ⚠️ 현재 세션 인식 문제 가능성
     * 대신 unifiedAuthApi.logout() 사용 권장
     */
    async logout() {
        const response = await apiRequest('/api/admin/auth/logout', {
            method: 'POST',
        });

        return handleJsonResponse(response);
    }
};

/**
 * 헬스체크 API
 */
export const healthApi = {
    /**
     * 백엔드 연결 상태 확인
     */
    async checkHealth() {
        try {
            const response = await apiRequest('/api/auth/health');
            const data = await handleJsonResponse(response);
            return { success: true, message: data.message };
        } catch (error) {
            console.error('헬스체크 실패:', error);
            return { success: false, message: error.message };
        }
    }
};

/**
 * 도서 관리 API - 백엔드 구조에 맞춰 수정
 * 백엔드 필드: {id, title, language, type, quantity, status, statusDescription, registeredBy, registeredAt, isAvailable, canBorrow}
 */
export const bookApi = {
    /**
     * 도서 목록 조회 (페이징)
     * @param {number} page - 페이지 번호
     * @param {number} size - 페이지 크기
     * @param {string} sort - 정렬 방식 (백엔드에서 무시되고 createdAt.desc 고정)
     */
    async getBooks(page = 0, size = 20, sort = 'title,asc') {
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString()
            // sort는 백엔드에서 고정이므로 제거
        });

        const response = await apiRequest(`/api/books?${params}`);
        return handleJsonResponse(response);
    },

    /**
     * 도서 검색 - 백엔드 BookSearchRequest 구조에 맞춤
     * 백엔드 필드: {titleKeyword, type, language, status, page, size}
     * @param {object} searchRequest - 프론트엔드 검색 조건
     */
    async searchBooks(searchRequest) {
        // 프론트엔드 → 백엔드 필드명 매핑
        const backendRequest = {
            titleKeyword: searchRequest.keyword || null,        // keyword → titleKeyword
            type: searchRequest.category || null,              // category → type
            language: searchRequest.language || null,
            status: searchRequest.status || null,
            page: searchRequest.page || 0,
            size: searchRequest.size || 20
            // author, sort 필드는 백엔드에서 지원하지 않으므로 제거
        };

        console.log('🔍 백엔드로 전송되는 검색 요청:', backendRequest);

        const response = await apiRequest('/api/books/search', {
            method: 'POST',
            body: JSON.stringify(backendRequest),
        });

        return handleJsonResponse(response);
    },

    /**
     * 도서 상세 조회
     * @param {number} id - 도서 ID
     */
    async getBook(id) {
        const response = await apiRequest(`/api/books/${id}`);
        return handleJsonResponse(response);
    },

    /**
     * 새 도서 등록 - 백엔드 CreateBookRequest 구조에 맞춤
     * 백엔드 필드: {title, language, type, quantity}
     * @param {object} bookData - 프론트엔드 도서 데이터
     */
    async createBook(bookData) {
        // 프론트엔드 → 백엔드 필드명 매핑
        const backendRequest = {
            title: bookData.title,
            language: bookData.language || 'English',
            type: bookData.category || bookData.type || 'Fiction',    // category → type
            quantity: bookData.totalCopies || bookData.quantity || 1  // totalCopies → quantity
            // author, isbn, publisher, publicationYear, description은 백엔드에서 지원하지 않음
        };

        console.log('📝 백엔드로 전송되는 도서 등록 요청:', backendRequest);

        const response = await apiRequest('/api/books', {
            method: 'POST',
            body: JSON.stringify(backendRequest),
        });

        return handleJsonResponse(response);
    },

    /**
     * 도서 정보 수정 - 백엔드 UpdateBookRequest 구조에 맞춤
     * 백엔드 필드: {title, language, type, quantity}
     * @param {number} id - 도서 ID
     * @param {object} bookData - 프론트엔드 수정할 도서 데이터
     */
    async updateBook(id, bookData) {
        // 프론트엔드 → 백엔드 필드명 매핑
        const backendRequest = {
            title: bookData.title,
            language: bookData.language || 'English',
            type: bookData.category || bookData.type || 'Fiction',    // category → type
            quantity: bookData.totalCopies || bookData.quantity || 1  // totalCopies → quantity
            // 다른 필드들은 백엔드에서 지원하지 않음
        };

        console.log('✏️ 백엔드로 전송되는 도서 수정 요청:', backendRequest);

        const response = await apiRequest(`/api/books/${id}`, {
            method: 'PUT',
            body: JSON.stringify(backendRequest),
        });

        return handleJsonResponse(response);
    },

    /**
     * 도서 삭제
     * @param {number} id - 도서 ID
     */
    async deleteBook(id) {
        const response = await apiRequest(`/api/books/${id}`, {
            method: 'DELETE',
        });

        return handleJsonResponse(response);
    },

    /**
     * 도서 상태 변경
     * @param {number} id - 도서 ID
     * @param {object} statusRequest - 상태 변경 요청 {status}
     */
    async changeBookStatus(id, statusRequest) {
        const response = await apiRequest(`/api/books/${id}/status`, {
            method: 'PATCH',
            body: JSON.stringify(statusRequest),
        });

        return handleJsonResponse(response);
    },

    /**
     * 도서 재고 추가 - 백엔드 AddStockRequest 구조에 맞춤
     * 백엔드 필드: {additionalQuantity}
     * @param {number} id - 도서 ID
     * @param {object} stockRequest - 재고 추가 요청
     */
    async addBookStock(id, stockRequest) {
        // 백엔드는 additionalQuantity 필드 기대
        const backendRequest = {
            additionalQuantity: stockRequest.quantity || stockRequest.additionalQuantity || 1
        };

        console.log('📦 백엔드로 전송되는 재고 추가 요청:', backendRequest);

        const response = await apiRequest(`/api/books/${id}/stock`, {
            method: 'PATCH',
            body: JSON.stringify(backendRequest),
        });

        return handleJsonResponse(response);
    },

    /**
     * 이용 가능한 도서 목록 조회
     */
    async getAvailableBooks() {
        const response = await apiRequest('/api/books/available');
        return handleJsonResponse(response);
    },

    /**
     * 대출 중인 도서 목록 조회
     */
    async getBorrowedBooks() {
        const response = await apiRequest('/api/books/borrowed');
        return handleJsonResponse(response);
    },

    /**
     * 도서 통계 조회
     */
    async getBookStatistics() {
        const response = await apiRequest('/api/books/statistics');
        return handleJsonResponse(response);
    },

    /**
     * 도서 등록/수정용 옵션 조회
     */
    async getBookOptions() {
        const response = await apiRequest('/api/books/options');
        return handleJsonResponse(response);
    },

    /**
     * 전체 도서 수 조회
     */
    async getTotalBookCount() {
        const response = await apiRequest('/api/books/count');
        return handleJsonResponse(response);
    }
};
/**
 * 관리자별 도서 조회 API
 */
export const bookAdminApi = {
    /**
     * 현재 로그인한 관리자가 등록한 도서 조회
     */
    async getMyBooks() {
        const response = await apiRequest('/api/books/admin/my-books');
        return handleJsonResponse(response);
    },

    /**
     * 특정 관리자가 등록한 도서 조회
     * @param {string} email - 관리자 이메일
     */
    async getBooksByAdmin(email) {
        const response = await apiRequest(`/api/books/admin/by-email?email=${encodeURIComponent(email)}`);
        return handleJsonResponse(response);
    }
};

/**
 * 사용자 관리 API
 */
export const userApi = {
    /**
     * 모든 사용자 목록 조회
     */
    async getAllUsers() {
        const response = await apiRequest('/api/users');
        return handleJsonResponse(response);
    },

    /**
     * 페이징 적용 사용자 목록 조회
     * @param {object} pageable - 페이징 옵션
     */
    async getAllUsersPaged(pageable = { page: 0, size: 20, sort: 'createdDate,desc' }) {
        const params = new URLSearchParams({
            page: pageable.page.toString(),
            size: pageable.size.toString(),
            sort: pageable.sort
        });

        const response = await apiRequest(`/api/users/paged?${params}`);
        return handleJsonResponse(response);
    },

    /**
     * 특정 사용자 조회
     * @param {number} id - 사용자 ID
     */
    async getUserById(id) {
        const response = await apiRequest(`/api/users/${id}`);
        return handleJsonResponse(response);
    },

    /**
     * 사용자 정보 업데이트
     * @param {number} id - 사용자 ID
     * @param {object} userData - 업데이트할 사용자 데이터
     */
    async updateUser(id, userData) {
        const response = await apiRequest(`/api/users/${id}`, {
            method: 'PUT',
            body: JSON.stringify(userData),
        });

        return handleJsonResponse(response);
    },

    /**
     * 사용자 삭제
     * @param {number} id - 사용자 ID
     */
    async deleteUser(id) {
        const response = await apiRequest(`/api/users/${id}`, {
            method: 'DELETE',
        });

        return handleJsonResponse(response);
    }
};

/**
 * 관리자 관리 API
 */
export const adminManagementApi = {
    /**
     * 모든 관리자 목록 조회
     */
    async getAllAdmins() {
        const response = await apiRequest('/api/admin');
        return handleJsonResponse(response);
    },

    /**
     * 특정 관리자 조회
     * @param {number} id - 관리자 ID
     */
    async getAdminById(id) {
        const response = await apiRequest(`/api/admin/${id}`);
        return handleJsonResponse(response);
    }
};

// 편의 함수들
export const utils = {
    /**
     * 사용자가 관리자인지 확인
     * @param {object} user - 사용자 객체
     */
    isAdmin(user) {
        return user && user.role === 'ADMIN';
    },

    /**
     * API 응답이 성공인지 확인
     * @param {object} response - API 응답
     */
    isSuccess(response) {
        return response && response.success === true;
    },

    /**
     * 에러 메시지 추출
     * @param {Error|object} error - 에러 객체
     */
    getErrorMessage(error) {
        if (error?.response?.data?.message) {
            return error.response.data.message;
        }
        if (error?.message) {
            return error.message;
        }
        return '알 수 없는 오류가 발생했습니다.';
    }
};
// src/lib/types/index.ts

// === 사용자 관련 타입들 ===

export interface User {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    phoneNumber: string;
    country: string;
    city: string;
    state?: string;
    street: string;
    role?: 'USER' | 'ADMIN';
    createdAt: string;
    updatedAt: string;
}

// === API 응답 타입들 ===

export interface ApiResponse<T = any> {
    success: boolean;
    message?: string;
    data?: T;
    error?: string;
    timestamp?: string;
}

export interface ApiError {
    code: string;
    message: string;
    details?: any;
    timestamp: string;
}

// === 인증 관련 타입들 ===

export interface LoginData {
    email: string;
    password: string;
}

export interface SignupData {
    firstName: string;
    lastName: string;
    email: string;
    phoneNumber: string;
    password: string;
    country: string;
    city: string;
    state?: string;
    street: string;
}

export interface AdminSignupData {
    name: string;
    email: string;
    password: string;
    confirmPassword: string;
    adminCode: string;
}

export interface PasswordRequirements {
    length: boolean;
    uppercase: boolean;
    lowercase: boolean;
    number: boolean;
    special: boolean;
}

// === 도서 관련 타입들 ===

export enum BookStatus {
    AVAILABLE = 'AVAILABLE',
    UNAVAILABLE = 'UNAVAILABLE',
    MAINTENANCE = 'MAINTENANCE',
    DELETED = 'DELETED'
}

export interface Book {
    id: number;
    title: string;
    author: string;
    isbn: string;
    category: string;
    publisher: string;
    publishedYear: number;
    totalStock: number;
    availableStock: number;
    description?: string;
    registeredByEmail: string;
    status: BookStatus;
    createdAt: string;
    updatedAt: string;
}

export interface BookResponse {
    id: number;
    title: string;
    author: string;
    isbn: string;
    category: string;
    publisher: string;
    publishedYear: number;
    totalStock: number;
    availableStock: number;
    description?: string;
    status: BookStatus;
    registeredBy: string;
    createdAt: string;
    updatedAt: string;
}

export interface BookPageResponse {
    content: BookResponse[];
    totalElements: number;
    totalPages: number;
    currentPage: number;
    size: number;
    first: boolean;
    last: boolean;
    numberOfElements: number;
}

export interface CreateBookRequest {
    title: string;
    author: string;
    isbn: string;
    category: string;
    publisher: string;
    publishedYear: number;
    totalStock: number;
    description?: string;
}

export interface UpdateBookRequest {
    title: string;
    author: string;
    isbn: string;
    category: string;
    publisher: string;
    publishedYear: number;
    description?: string;
}

export interface BookSearchRequest {
    keyword?: string;
    category?: string;
    author?: string;
    publisher?: string;
    publishedYear?: number;
    status?: BookStatus;
    page?: number;
    size?: number;
}

export interface BookStatusChangeRequest {
    status: BookStatus;
    reason?: string;
}

export interface AddStockRequest {
    quantity: number;
    reason?: string;
}

export interface BookOptionsResponse {
    categories: string[];
    publishers: string[];
    years: number[];
    statuses: BookStatus[];
}

export interface BookStatisticsResponse {
    totalBooks: number;
    availableBooks: number;
    borrowedBooks: number;
    unavailableBooks: number;
    totalCategories: number;
    mostBorrowedBooks: BookResponse[];
    recentlyAddedBooks: BookResponse[];
}

export interface BookDetail {
    id: number;
    name: string;
    type: string;
    language: string;
    isbn?: string;
    author?: string;
    publisher?: string;
}

// === 대출 관련 타입들 ===

export enum RentalStatus {
    ACTIVE = 'ACTIVE',
    RETURNED = 'RETURNED',
    OVERDUE = 'OVERDUE',
    LOST = 'LOST',
    CANCELLED = 'CANCELLED'
}

export interface BookRental {
    id: number;
    bookId: number;
    userEmail: string;
    rentalDate: string;
    dueDate: string;
    returnDate?: string;
    status: RentalStatus;
    extensionCount: number;
    notes?: string;
    createdAt: string;
    updatedAt: string;
}

export interface RentalResponse {
    id: number;
    bookId: number;
    bookTitle: string;
    bookAuthor: string;
    bookIsbn: string;
    userEmail: string;
    userName?: string;
    rentalDate: string;
    dueDate: string;
    returnDate?: string;
    status: RentalStatus;
    extensionCount: number;
    remainingDays?: number;
    notes?: string;
    createdAt: string;
    updatedAt: string;
}

export interface RentalRequest {
    bookId: number;
    rentalDays: number;
    notes?: string;
}

export interface ReturnRequest {
    condition?: string;
    notes?: string;
}

export interface ExtendRentalRequest {
    extensionDays: number;
    reason?: string;
}

export interface AdminBorrowedBook {
    id: string;
    userId: string;
    userEmail: string;
    userName: string;
    amount: string;
    totalBooks: number;
    dueDate: string;
    rentalDate: string;
    dateTime: string;
    status: RentalStatus;
    bookDetails: BookDetail[];
}

export interface RentalStatistics {
    totalRentals: number;
    activeRentals: number;
    overdueRentals: number;
    returnedRentals: number;
    averageRentalDays: number;
    mostBorrowedBooks: BookResponse[];
    topBorrowers: UserRentalSummary[];
}

export interface UserRentalSummary {
    userEmail: string;
    userName: string;
    totalRentals: number;
    activeRentals: number;
    overdueRentals: number;
    lastRentalDate: string;
}

// === 알림 및 UI 관련 타입들 ===

export interface Notification {
    id: number;
    type: 'success' | 'danger' | 'warning' | 'info';
    message: string;
    duration?: number;
    timestamp?: string;
}

export interface TableColumn {
    key: string;
    label: string;
    sortable?: boolean;
    width?: string;
    align?: 'left' | 'center' | 'right';
}

export interface PaginationInfo {
    currentPage: number;
    totalPages: number;
    pageSize: number;
    totalElements: number;
    hasNext: boolean;
    hasPrevious: boolean;
}

export interface SortInfo {
    field: string;
    direction: 'asc' | 'desc';
}

export interface FilterInfo {
    field: string;
    value: any;
    operator: 'eq' | 'ne' | 'like' | 'gt' | 'gte' | 'lt' | 'lte' | 'in';
}

// === 폼 관련 타입들 ===

export interface FormState<T = any> {
    data: T;
    errors: Record<string, string>;
    loading: boolean;
    success: boolean;
    dirty?: boolean;
    valid?: boolean;
}

export interface ValidationResult {
    valid: boolean;
    errors: Record<string, string>;
}

export interface ValidationError {
    field: string;
    message: string;
    code: string;
    value?: any;
}

export type ValidationRule<T = any> = (value: T) => string | true;
export type ValidationRules<T = any> = Partial<Record<keyof T, ValidationRule<T[keyof T]>[]>>;

// === 선택 옵션 타입들 ===

export interface SelectOption {
    value: string;
    label: string;
    disabled?: boolean;
}

export interface SearchOption extends SelectOption {
    category?: string;
    description?: string;
}

// === 설정 및 환경 타입들 ===

export interface AppConfig {
    apiBaseUrl: string;
    environment: 'development' | 'production' | 'test';
    features: {
        enableNotifications: boolean;
        enableAnalytics: boolean;
        enableDebugMode: boolean;
    };
    limits: {
        maxRentalDays: number;
        maxRentalCount: number;
        maxExtensionCount: number;
        defaultPageSize: number;
    };
}

// === 이벤트 타입들 ===

export interface CustomEvent<T = any> {
    type: string;
    detail: T;
    timestamp: string;
}

export interface RentalEvent extends CustomEvent {
    detail: {
        rentalId: number;
        bookId: number;
        userEmail: string;
        action: 'rent' | 'return' | 'extend' | 'overdue';
        previousStatus?: RentalStatus;
        newStatus: RentalStatus;
    };
}

export interface BookEvent extends CustomEvent {
    detail: {
        bookId: number;
        action: 'create' | 'update' | 'delete' | 'status_change' | 'stock_change';
        changes?: Partial<Book>;
        adminEmail: string;
    };
}

// === 유틸리티 타입들 ===

export type Optional<T, K extends keyof T> = Omit<T, K> & Partial<Pick<T, K>>;
export type RequiredFields<T, K extends keyof T> = T & Required<Pick<T, K>>;
export type PartialExcept<T, K extends keyof T> = Partial<T> & Pick<T, K>;

// === 컴포넌트 Props 타입들 ===

export interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    title?: string;
    size?: 'small' | 'medium' | 'large' | 'fullscreen';
    closeOnBackdrop?: boolean;
    closeOnEscape?: boolean;
}

export interface ButtonProps {
    variant?: 'primary' | 'secondary' | 'danger' | 'warning' | 'success' | 'outline';
    size?: 'small' | 'medium' | 'large';
    disabled?: boolean;
    loading?: boolean;
    fullWidth?: boolean;
    type?: 'button' | 'submit' | 'reset';
}

export interface InputProps {
    value: string;
    placeholder?: string;
    disabled?: boolean;
    required?: boolean;
    type?: 'text' | 'email' | 'password' | 'number' | 'tel' | 'url';
    error?: string;
    helpText?: string;
}

export interface TableProps<T = any> {
    data: T[];
    columns: TableColumn[];
    loading?: boolean;
    pagination?: PaginationInfo;
    sortInfo?: SortInfo;
    onSort?: (field: string) => void;
    onPageChange?: (page: number) => void;
    onRowClick?: (row: T) => void;
    emptyMessage?: string;
}

// === 스토어 상태 타입들 ===

export interface AuthState {
    user: User | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    error: string | null;
}

export interface NotificationState {
    notifications: Notification[];
    maxNotifications: number;
}

export interface UIState {
    sidebarOpen: boolean;
    theme: 'light' | 'dark' | 'auto';
    language: string;
    mobileMenuOpen: boolean;
}

// === 라우팅 관련 타입들 ===

export interface RouteData {
    title: string;
    description?: string;
    requiresAuth?: boolean;
    requiredRole?: 'USER' | 'ADMIN';
    breadcrumbs?: Breadcrumb[];
}

export interface Breadcrumb {
    label: string;
    href?: string;
    icon?: string;
}

// === 검색 및 필터 타입들 ===

export interface SearchParams {
    query?: string;
    filters?: FilterInfo[];
    sort?: SortInfo;
    pagination?: Pick<PaginationInfo, 'currentPage' | 'pageSize'>;
}

export interface SearchResult<T = any> {
    items: T[];
    totalCount: number;
    hasMore: boolean;
    searchTime: number;
    suggestions?: string[];
}

// === 대시보드 관련 타입들 ===

export interface DashboardWidget {
    id: string;
    title: string;
    type: 'chart' | 'stat' | 'table' | 'list';
    data: any;
    config?: any;
    position: {
        x: number;
        y: number;
        width: number;
        height: number;
    };
}

export interface DashboardStats {
    totalUsers: number;
    totalBooks: number;
    activeRentals: number;
    overdueRentals: number;
    newUsersToday: number;
    newBooksToday: number;
    popularBooks: BookResponse[];
    recentActivity: ActivityItem[];
}

export interface ActivityItem {
    id: string;
    type: 'rental' | 'return' | 'book_added' | 'user_registered';
    description: string;
    timestamp: string;
    user?: string;
    metadata?: any;
}
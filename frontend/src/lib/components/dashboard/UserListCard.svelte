<!-- src/lib/components/dashboard/UserListCard.svelte -->
<script>
    import { createEventDispatcher } from 'svelte';
    import ActionIcon from '$lib/components/ui/ActionIcon.svelte';

    export let title = 'User List';
    export let users = [];
    export let loading = false;
    export let maxItems = 5;
    export let showActions = true;

    const dispatch = createEventDispatcher();

    // 표시할 사용자 목록 (최대 개수 제한)
    $: displayUsers = users.slice(0, maxItems);

    function handleViewUser(user) {
        dispatch('viewUser', user);
    }

    function handleViewAll() {
        dispatch('viewAll');
    }

    // 스켈레톤 로딩용 더미 배열
    $: skeletonItems = Array(maxItems).fill(null);
</script>

<div class="user-list-card">
    <div class="card-header">
        <h3 class="card-title">{title}</h3>
        {#if !loading && users.length > maxItems}
            <button class="view-all-btn" on:click={handleViewAll}>
                View All ({users.length})
            </button>
        {/if}
    </div>

    <div class="card-content">
        {#if loading}
            <!-- 로딩 스켈레톤 -->
            {#each skeletonItems as _, index}
                <div class="user-item skeleton" aria-hidden="true">
                    <div class="user-avatar skeleton-avatar"></div>
                    <div class="user-info">
                        <div class="skeleton-line skeleton-name"></div>
                        <div class="skeleton-line skeleton-detail"></div>
                    </div>
                    <div class="skeleton-action"></div>
                </div>
            {/each}
        {:else if displayUsers.length === 0}
            <!-- 빈 상태 -->
            <div class="empty-state">
                <div class="empty-icon">👥</div>
                <p class="empty-message">No users found</p>
            </div>
        {:else}
            <!-- 사용자 목록 -->
            {#each displayUsers as user, index (user.id)}
                <div class="user-item" style="animation-delay: {index * 50}ms;">
                    <div class="user-avatar">
                        {#if user.avatar}
                            <img src={user.avatar} alt="{user.name}'s avatar" class="avatar-image" />
                        {:else}
                            <div class="avatar-initials">
                                {user.name?.charAt(0)?.toUpperCase() || '?'}
                            </div>
                        {/if}
                    </div>

                    <div class="user-info">
                        <div class="user-name">{user.name || 'Unknown User'}</div>
                        <div class="user-detail">
                            {#if user.email}
                                {user.email}
                            {:else if user.borrowedCount !== undefined}
                                Borrowed: {user.borrowedCount}
                            {:else}
                                No details available
                            {/if}
                        </div>
                    </div>

                    {#if showActions}
                        <div class="user-action">
                            <ActionIcon
                                    icon="↗"
                                    variant="default"
                                    size="small"
                                    tooltip="View details"
                                    on:click={() => handleViewUser(user)}
                            />
                        </div>
                    {/if}
                </div>
            {/each}
        {/if}
    </div>
</div>

<style>
    .user-list-card {
        background: white;
        border-radius: 12px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        border: 1px solid #f1f5f9;
        overflow: hidden;
        height: fit-content;
    }

    .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1.5rem 1.5rem 1rem;
        border-bottom: 1px solid #f1f5f9;
    }

    .card-title {
        font-size: 1.125rem;
        font-weight: 600;
        color: #1e293b;
        margin: 0;
    }

    .view-all-btn {
        background: none;
        border: none;
        color: #3b82f6;
        font-size: 0.875rem;
        font-weight: 500;
        cursor: pointer;
        padding: 0.25rem 0.5rem;
        border-radius: 4px;
        transition: all 0.2s ease;
    }

    .view-all-btn:hover {
        background: #eff6ff;
        color: #1d4ed8;
    }

    .card-content {
        padding: 0;
        max-height: 400px;
        overflow-y: auto;
    }

    .user-item {
        display: flex;
        align-items: center;
        gap: 1rem;
        padding: 1rem 1.5rem;
        border-bottom: 1px solid #f8fafc;
        transition: all 0.2s ease;
        animation: fadeInUp 0.3s ease-out forwards;
        opacity: 0;
        transform: translateY(10px);
    }

    .user-item:last-child {
        border-bottom: none;
    }

    .user-item:hover:not(.skeleton) {
        background: #f8fafc;
    }

    @keyframes fadeInUp {
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    .user-avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        overflow: hidden;
        flex-shrink: 0;
        position: relative;
    }

    .avatar-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }

    .avatar-initials {
        width: 100%;
        height: 100%;
        background: linear-gradient(135deg, #3b82f6, #1d4ed8);
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
        font-size: 1rem;
        color: white;
        text-transform: uppercase;
    }

    .user-info {
        flex: 1;
        min-width: 0;
    }

    .user-name {
        font-weight: 500;
        font-size: 0.875rem;
        color: #1e293b;
        margin-bottom: 0.125rem;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .user-detail {
        font-size: 0.75rem;
        color: #64748b;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .user-action {
        flex-shrink: 0;
    }

    /* 스켈레톤 로딩 */
    .user-item.skeleton {
        animation: none;
        opacity: 1;
        transform: none;
    }

    .skeleton-avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
        background-size: 200% 100%;
        animation: shimmer 1.5s infinite;
    }

    .skeleton-line {
        height: 12px;
        background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
        background-size: 200% 100%;
        animation: shimmer 1.5s infinite;
        border-radius: 6px;
    }

    .skeleton-name {
        width: 80%;
        margin-bottom: 6px;
    }

    .skeleton-detail {
        width: 60%;
    }

    .skeleton-action {
        width: 24px;
        height: 24px;
        background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
        background-size: 200% 100%;
        animation: shimmer 1.5s infinite;
        border-radius: 4px;
    }

    @keyframes shimmer {
        0% {
            background-position: -200% 0;
        }
        100% {
            background-position: 200% 0;
        }
    }

    /* 빈 상태 */
    .empty-state {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 2rem 1rem;
        text-align: center;
        color: #64748b;
    }

    .empty-icon {
        font-size: 2rem;
        margin-bottom: 0.75rem;
        opacity: 0.5;
    }

    .empty-message {
        font-size: 0.875rem;
        margin: 0;
    }

    /* 반응형 */
    @media (max-width: 768px) {
        .card-header {
            padding: 1rem;
            flex-direction: column;
            align-items: flex-start;
            gap: 0.5rem;
        }

        .user-item {
            padding: 0.75rem 1rem;
            gap: 0.75rem;
        }

        .user-avatar {
            width: 36px;
            height: 36px;
        }

        .avatar-initials {
            font-size: 0.875rem;
        }

        .user-name {
            font-size: 0.8rem;
        }

        .user-detail {
            font-size: 0.7rem;
        }

        .skeleton-avatar {
            width: 36px;
            height: 36px;
        }
    }

    /* 접근성 */
    @media (prefers-reduced-motion: reduce) {
        .user-item {
            animation: none;
            opacity: 1;
            transform: none;
        }

        .skeleton-avatar,
        .skeleton-line,
        .skeleton-action {
            animation: none;
        }
    }

    /* 다크모드 */
    @media (prefers-color-scheme: dark) {
        .user-list-card {
            background: #1e293b;
            border-color: #334155;
        }

        .card-header {
            border-bottom-color: #334155;
        }

        .card-title {
            color: #f1f5f9;
        }

        .view-all-btn {
            color: #60a5fa;
        }

        .view-all-btn:hover {
            background: #1e3a8a;
            color: #93c5fd;
        }

        .user-item {
            border-bottom-color: #334155;
        }

        .user-item:hover:not(.skeleton) {
            background: #334155;
        }

        .user-name {
            color: #f1f5f9;
        }

        .user-detail {
            color: #94a3b8;
        }

        .empty-state {
            color: #94a3b8;
        }

        .skeleton-avatar,
        .skeleton-line,
        .skeleton-action {
            background: linear-gradient(90deg, #334155 25%, #475569 50%, #334155 75%);
            background-size: 200% 100%;
        }
    }

    /* 프린트 */
    @media print {
        .user-list-card {
            box-shadow: none;
            border: 1px solid #000;
            break-inside: avoid;
        }

        .user-action {
            display: none;
        }

        .view-all-btn {
            display: none;
        }
    }

    /* 스크롤바 스타일링 */
    .card-content::-webkit-scrollbar {
        width: 4px;
    }

    .card-content::-webkit-scrollbar-track {
        background: #f8fafc;
    }

    .card-content::-webkit-scrollbar-thumb {
        background: #cbd5e1;
        border-radius: 2px;
    }

    .card-content::-webkit-scrollbar-thumb:hover {
        background: #94a3b8;
    }

    /* 포커스 스타일 */
    .view-all-btn:focus {
        outline: 2px solid #3b82f6;
        outline-offset: 2px;
    }
</style>
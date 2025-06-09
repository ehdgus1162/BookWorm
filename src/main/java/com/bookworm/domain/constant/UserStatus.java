package com.bookworm.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    DELETED
}
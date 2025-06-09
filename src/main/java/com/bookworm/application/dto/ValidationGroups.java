package com.bookworm.application.dto;

import jakarta.validation.groups.Default;

/**
 * Bean Validation 그룹 정의
 */
public interface ValidationGroups {

    interface Create extends Default {}

    interface Update extends Default {}

    interface Delete extends Default {}
}
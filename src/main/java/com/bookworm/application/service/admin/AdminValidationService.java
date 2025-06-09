package com.bookworm.application.service.admin;

// BookWorm - A library management system
public interface AdminValidationService {

    boolean existsByEmail(String email);
}
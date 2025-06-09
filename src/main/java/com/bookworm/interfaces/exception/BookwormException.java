package com.bookworm.interfaces.exception;

// 기본 예외 클래스
public abstract class BookwormException extends RuntimeException {
  public BookwormException(String message) {
    super(message);
  }
}


package com.bookworm.domain.exception;

public class PhoneNumberValidationException extends LibraryBusinessException {
  public PhoneNumberValidationException(String message) {
    super("PHONE_NUMBER_VALIDATION_ERROR", message);
  }

  public PhoneNumberValidationException(String message, Throwable cause) {
    super("PHONE_NUMBER_VALIDATION_ERROR", message, cause);
  }
}

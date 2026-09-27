package com.urlshortener.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	public record ApiError(int status, String message, LocalDateTime timestamp) {
		public static ApiError of(HttpStatus status, String message) {
			return new ApiError(status.value(), message, LocalDateTime.now());
		}
	}

	@ExceptionHandler(UrlNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(UrlNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of(HttpStatus.NOT_FOUND, ex.getMessage()));
	}

	@ExceptionHandler(UrlAlreadyExistsException.class)
	public ResponseEntity<ApiError> handleAlreadyExists(UrlAlreadyExistsException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.of(HttpStatus.CONFLICT, ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.findFirst()
				.orElse("Validation failed");
		return ResponseEntity.badRequest().body(ApiError.of(HttpStatus.BAD_REQUEST, message));
	}
}

package com.sidequest.sidequest.controller;

import com.sidequest.sidequest.dto.ApiErrorResponseDTO;
import com.sidequest.sidequest.dto.ApiFieldErrorDTO;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return buildResponse(status, status.name(), resolveMessage(ex.getReason(), status.getReasonPhrase()), List.of());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        List<ApiFieldErrorDTO> fieldErrors = ex.getConstraintViolations().stream()
                .map(violation -> ApiFieldErrorDTO.builder()
                        .field(resolveFieldName(violation.getPropertyPath().toString()))
                        .code(resolveConstraintCode(violation))
                        .message(violation.getMessage())
                        .build())
                .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", fieldErrors);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        List<ApiFieldErrorDTO> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> ApiFieldErrorDTO.builder()
                        .field(fieldError.getField())
                        .code(resolveFieldErrorCode(fieldError.getCode()))
                        .message(resolveMessage(fieldError.getDefaultMessage(), "Validation failed"))
                        .build())
                .toList();

        return buildResponse(HttpStatus.valueOf(status.value()), "VALIDATION_ERROR", "Validation failed", fieldErrors);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), List.of(ApiFieldErrorDTO.builder()
                .field(ex.getParameterName())
                .code("REQUIRED")
                .message(ex.getMessage())
                .build()));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Malformed request body", List.of());
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            org.springframework.beans.TypeMismatchException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        String fieldName = ex instanceof MethodArgumentTypeMismatchException mismatch ? mismatch.getName() : "value";
        return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", resolveMessage(ex.getMessage(), "Invalid request"), List.of(ApiFieldErrorDTO.builder()
                .field(fieldName)
                .code("TYPE_MISMATCH")
                .message(resolveMessage(ex.getMessage(), "Invalid request"))
                .build()));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        if (body instanceof ApiErrorResponseDTO) {
            return ResponseEntity.status(statusCode).headers(headers).body(body);
        }

        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    private ResponseEntity<Object> buildResponse(
            HttpStatus status,
            String code,
            String message,
            List<ApiFieldErrorDTO> fieldErrors
    ) {
        return ResponseEntity.status(status).body((Object) ApiErrorResponseDTO.builder()
                .code(code)
                .message(message)
                .fieldErrors(fieldErrors)
                .build());
    }

    private String resolveFieldName(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return "request";
        }

        String[] parts = rawPath.split("\\.");
        return parts.length == 0 ? rawPath : parts[parts.length - 1];
    }

    private String resolveConstraintCode(ConstraintViolation<?> violation) {
        if (violation == null || violation.getConstraintDescriptor() == null || violation.getConstraintDescriptor().getAnnotation() == null) {
            return "VALIDATION";
        }

        return violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName().toUpperCase();
    }

    private String resolveFieldErrorCode(String code) {
        return code == null || code.isBlank() ? "VALIDATION" : code.toUpperCase();
    }

    private String resolveMessage(String message, String fallback) {
        return message == null || message.isBlank() ? fallback : message;
    }
}

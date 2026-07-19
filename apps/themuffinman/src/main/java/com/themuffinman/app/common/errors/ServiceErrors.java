package com.themuffinman.app.common.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class ServiceErrors {

    private ServiceErrors() {
    }

    public static ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    public static CodedResponseStatusException notFound(String code, String message) {
        return new CodedResponseStatusException(HttpStatus.NOT_FOUND, code, message);
    }

    public static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    public static CodedResponseStatusException badRequest(String code, String message) {
        return new CodedResponseStatusException(HttpStatus.BAD_REQUEST, code, message);
    }

    public static ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    public static CodedResponseStatusException conflict(String code, String message) {
        return new CodedResponseStatusException(HttpStatus.CONFLICT, code, message);
    }

    public static ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    public static ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }

    public static ResponseStatusException serviceUnavailable(String message) {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    public static CodedResponseStatusException serviceUnavailable(String code, String message) {
        return new CodedResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, code, message);
    }

    public static ResponseStatusException tooManyRequests(String message) {
        return new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, message);
    }
}

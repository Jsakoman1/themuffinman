package com.themuffinman.app.common.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CodedResponseStatusException extends ResponseStatusException {
    private final String code;

    public CodedResponseStatusException(HttpStatus status, String code, String reason) {
        super(status, reason);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

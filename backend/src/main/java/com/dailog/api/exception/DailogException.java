package com.dailog.api.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

@Getter
public abstract class DailogException extends RuntimeException {

    private final ObjectNode validation = new ObjectMapper().createObjectNode();

    public DailogException(String message) {
        super(message);
    }

    public DailogException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract int getStatusCode();

    public void addValidation(String fieldName, String message) {
        validation.put(fieldName, message);
    }
}

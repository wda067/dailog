package com.dailog.api.config.handler;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    //@ExceptionHandler(Exception.class)
    //public ResponseEntity<Map<String, String>> handleException(Exception e) {
    //    log.error("Unhandled exception: ", e);
    //    Map<String, String> errorResponse = new HashMap<>();
    //    errorResponse.put("error", e.getMessage());
    //    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    //}
    //
    //@ExceptionHandler(MethodArgumentNotValidException.class)
    //public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    //    Map<String, String> errors = new HashMap<>();
    //    ex.getBindingResult().getAllErrors().forEach((error) -> {
    //        String fieldName = ((FieldError) error).getField();
    //        String errorMessage = error.getDefaultMessage();
    //        errors.put(fieldName, errorMessage);
    //    });
    //    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    //}
}

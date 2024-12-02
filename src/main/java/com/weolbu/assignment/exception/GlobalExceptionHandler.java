package com.weolbu.assignment.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e){
        return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<String> handleEmailAlreadyExistsException(EmailAlreadyExistsException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    @ExceptionHandler(PhoneAlreadyExistsException.class)
    public ResponseEntity<String> handlePhoneAlreadyExistsException(PhoneAlreadyExistsException e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException e){
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()){
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(errors);
    }

}
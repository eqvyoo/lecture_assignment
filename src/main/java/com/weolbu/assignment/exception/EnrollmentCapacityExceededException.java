package com.weolbu.assignment.exception;

public class EnrollmentCapacityExceededException extends RuntimeException {
    public EnrollmentCapacityExceededException(String message) {
        super(message);
    }
}
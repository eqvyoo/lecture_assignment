package com.weolbu.assignment.exception;

public class AlreadyEnrolledException extends RuntimeException{
    public AlreadyEnrolledException(String message){
        super(message);
    }
}

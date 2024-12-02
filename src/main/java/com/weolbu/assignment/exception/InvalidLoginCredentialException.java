package com.weolbu.assignment.exception;

public class InvalidLoginCredentialException extends RuntimeException{
    public InvalidLoginCredentialException(String message){
        super(message);
    }
}

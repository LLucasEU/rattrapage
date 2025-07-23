package com.nobass.server.exceptions;

public class UserMovieListNotFoundException extends RuntimeException {
    public UserMovieListNotFoundException(String message) {
        super(message);
    }
} 
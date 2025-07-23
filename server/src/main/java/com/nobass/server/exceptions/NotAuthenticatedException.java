package com.nobass.server.exceptions;

public class NotAuthenticatedException extends RuntimeException {
    public NotAuthenticatedException() {
        super("Non authentifié");
    }
} 
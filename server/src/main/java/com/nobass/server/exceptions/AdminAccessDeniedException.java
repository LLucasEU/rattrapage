package com.nobass.server.exceptions;

public class AdminAccessDeniedException extends RuntimeException {
    public AdminAccessDeniedException() {
        super("Accès refusé - Administrateur requis");
    }
} 
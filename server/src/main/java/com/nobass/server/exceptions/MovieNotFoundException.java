package com.nobass.server.exceptions;

public class MovieNotFoundException extends RuntimeException {
    public MovieNotFoundException() {
        super("Film non trouvé");
    }
}

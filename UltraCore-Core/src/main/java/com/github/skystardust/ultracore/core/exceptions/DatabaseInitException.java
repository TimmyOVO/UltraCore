package com.github.skystardust.ultracore.core.exceptions;

public class DatabaseInitException extends Exception {
    public DatabaseInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseInitException(String message) {
        super(message);
    }
}

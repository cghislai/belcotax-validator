package com.charlyghislain.belcotax.util;

public class BelcotaxValidationException extends Exception {
    public BelcotaxValidationException() {
    }

    public BelcotaxValidationException(String message) {
        super(message);
    }

    public BelcotaxValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BelcotaxValidationException(Throwable cause) {
        super(cause);
    }

    public BelcotaxValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

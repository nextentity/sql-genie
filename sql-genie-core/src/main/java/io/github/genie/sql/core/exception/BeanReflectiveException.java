package io.github.genie.sql.core.exception;

public class BeanReflectiveException extends RuntimeException {
    public BeanReflectiveException() {
    }

    public BeanReflectiveException(String message) {
        super(message);
    }

    public BeanReflectiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanReflectiveException(Throwable cause) {
        super(cause);
    }

    public BeanReflectiveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

package io.github.genie.sql.core.exception;

public class SqlExecuteException extends RuntimeException{
    public SqlExecuteException() {
    }

    public SqlExecuteException(String message) {
        super(message);
    }

    public SqlExecuteException(String message, Throwable cause) {
        super(message, cause);
    }

    public SqlExecuteException(Throwable cause) {
        super(cause);
    }

    public SqlExecuteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

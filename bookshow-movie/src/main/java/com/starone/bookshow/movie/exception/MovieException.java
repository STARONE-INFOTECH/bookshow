package com.starone.bookshow.movie.exception;

public class MovieException extends RuntimeException {
    private final String messageKey;
    private final Object[] args;

    public MovieException(String messageKey, Object... args) {
        super(String.format(messageKey, args));
        this.messageKey = messageKey;
        this.args = args;
    }

}

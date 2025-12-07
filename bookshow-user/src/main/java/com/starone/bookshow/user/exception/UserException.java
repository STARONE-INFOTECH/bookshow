package com.starone.bookshow.user.exception;

public class UserException extends RuntimeException {
    
    private final String messageKey;
    private final Object[] args;

    public UserException(String messageKey, Object[] args) {
        super(String.format(messageKey, args));
        this.messageKey = messageKey;
        this.args = args;
    }

}

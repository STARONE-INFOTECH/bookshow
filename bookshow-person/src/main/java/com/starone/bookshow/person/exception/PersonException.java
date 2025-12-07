package com.starone.bookshow.person.exception;

public class PersonException extends RuntimeException {
    private final String messageKey;
    private final Object[] args;

    public PersonException(String messageKey, Object... args) {
        super(String.format(messageKey, args));
        this.messageKey = messageKey;
        this.args = args;
    }

}

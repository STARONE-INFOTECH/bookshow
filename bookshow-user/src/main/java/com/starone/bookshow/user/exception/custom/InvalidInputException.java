package com.starone.bookshow.user.exception.custom;

import com.starone.bookshow.user.exception.UserException;

public class InvalidInputException extends UserException {

    public InvalidInputException(String messageKey, Object... args) {
        super("error.invalid_input", args);
    }

}

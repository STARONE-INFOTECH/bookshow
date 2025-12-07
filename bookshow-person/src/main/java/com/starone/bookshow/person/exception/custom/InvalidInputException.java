package com.starone.bookshow.person.exception.custom;

import com.starone.bookshow.person.exception.PersonException;

public class InvalidInputException extends PersonException {

    public InvalidInputException(Object... args) {
        super("error.invalid_input", args);
    }

}

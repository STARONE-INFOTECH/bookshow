package com.starone.bookshow.movie.exception.custom;

import com.starone.bookshow.movie.exception.MovieException;

public class InvalidInputException extends MovieException {

    public InvalidInputException(Object... args) {
        super("error.invalid_input", args);
    }

}

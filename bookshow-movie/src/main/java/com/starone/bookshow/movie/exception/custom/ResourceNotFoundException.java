package com.starone.bookshow.movie.exception.custom;

import com.starone.bookshow.movie.exception.MovieException;

public class ResourceNotFoundException extends MovieException{

    public ResourceNotFoundException(Object... args) {
        super("error.resource_not_found", args);
    }

}

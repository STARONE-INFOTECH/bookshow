package com.starone.bookshow.person.exception.custom;

import com.starone.bookshow.person.exception.PersonException;

public class ResourceNotFoundException extends PersonException{

    public ResourceNotFoundException(Object... args) {
        super("error.resource_not_found", args);
    }

}

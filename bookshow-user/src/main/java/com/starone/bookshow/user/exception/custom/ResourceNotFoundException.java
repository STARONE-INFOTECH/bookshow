package com.starone.bookshow.user.exception.custom;

import com.starone.bookshow.user.exception.UserException;

public class ResourceNotFoundException extends UserException{

    public ResourceNotFoundException(String messageKey, Object... args) {
        super("error.resource_not_found", args);
    }

}

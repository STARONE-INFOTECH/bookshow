package com.starone.bookshow.person.exception;

import com.starone.springcommon.exceptions.BaseException;
import com.starone.springcommon.exceptions.errorcodes.ErrorCode;

public class PersonException extends BaseException{

    public PersonException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PersonException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public PersonException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public PersonException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

}

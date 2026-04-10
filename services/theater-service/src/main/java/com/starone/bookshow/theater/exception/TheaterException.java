package com.starone.bookshow.theater.exception;

import com.starone.springcommon.exceptions.BaseException;
import com.starone.springcommon.exceptions.errorcodes.ErrorCode;

public class TheaterException extends BaseException {

    public TheaterException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TheaterException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public TheaterException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public TheaterException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

}

package com.starone.bookshow.movie.exception;

import com.starone.springcommon.exceptions.BaseException;
import com.starone.springcommon.exceptions.errorcodes.ErrorCode;

public class MovieException extends BaseException {

    public MovieException(ErrorCode errorCode) {
        super(errorCode);
    }

    public MovieException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public MovieException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public MovieException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

}

package com.starone.bookshow.show.exception;

import com.starone.springcommon.exceptions.BaseException;
import com.starone.springcommon.exceptions.errorcodes.ErrorCode;

public class ShowException extends BaseException{

    public ShowException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ShowException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ShowException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public ShowException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

}

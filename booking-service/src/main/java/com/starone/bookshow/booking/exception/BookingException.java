package com.starone.bookshow.booking.exception;

import com.starone.springcommon.exceptions.BaseException;
import com.starone.springcommon.exceptions.errorcodes.ErrorCode;

public class BookingException extends BaseException {

    public BookingException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BookingException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BookingException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public BookingException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

}

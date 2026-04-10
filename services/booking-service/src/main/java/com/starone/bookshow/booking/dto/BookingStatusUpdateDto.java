package com.starone.bookshow.booking.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingStatusUpdateDto {
    private String status;                   // CONFIRMED, FAILED
    private String paymentId;
    private String gatewayResponse;          // raw response
}

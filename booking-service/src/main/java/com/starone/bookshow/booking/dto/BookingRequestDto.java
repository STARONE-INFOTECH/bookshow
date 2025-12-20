package com.starone.bookshow.booking.dto;

import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingRequestDto {
    private UUID showId;

    private List<String> seatNumbers;  // e.g., ["A1", "A2", "B5"]

    // Optional: customer details if not from authenticated user
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Optional: coupon/promo code
    private String promoCode;
}

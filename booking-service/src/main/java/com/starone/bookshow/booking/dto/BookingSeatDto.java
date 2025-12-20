package com.starone.bookshow.booking.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingSeatDto {
private String seatNumber;
    private String seatType;
    private String priceCategory;
    private double seatPrice;
}

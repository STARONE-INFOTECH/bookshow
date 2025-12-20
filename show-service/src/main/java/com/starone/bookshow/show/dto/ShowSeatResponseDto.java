package com.starone.bookshow.show.dto;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShowSeatResponseDto {
    private UUID id;
    private String seatNumber;
    private String seatType;
    private String priceCategory;
    private double price;
    private String status;  // AVAILABLE, LOCKED, BOOKED
}

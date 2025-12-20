package com.starone.bookshow.booking.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingResponseDto {
    private UUID bookingId;
    private String bookingReference; // e.g., BKN-2025-12345

    private UUID userId;

    private UUID showId;
    private String movieTitle;
    private String theaterName;
    private String screenName;
    private LocalDateTime showStartTime;
    private String showType;

    private LocalDateTime bookingTime;
    private LocalDateTime paymentTime;

    private double totalAmount;
    private double discountAmount;
    private double finalAmount;

    private String status; // PENDING, CONFIRMED, etc.

    private String paymentId; // gateway transaction ID
    private String ticketQrCodeUrl; // generated after confirmation

    private List<BookingSeatDto> bookedSeats;

    private String customerName;
    private String customerEmail;
    private String customerPhone;
}

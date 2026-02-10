package com.starone.bookshow.booking.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.starone.bookshow.booking.dto.BookingCancellationRequestDto;
import com.starone.bookshow.booking.dto.BookingRequestDto;
import com.starone.bookshow.booking.dto.BookingResponse;
import com.starone.bookshow.booking.dto.PaymentConfirmRequestDto;

public interface IBookingService {
    /**
     * Create a new booking - locks seats and returns pending booking
     */
    BookingResponse createBooking(UUID userId, BookingRequestDto requestDto);

    /**
     * Confirm payment success - marks booking CONFIRMED, generates ticket
     */
    BookingResponse confirmPayment(UUID bookingId, PaymentConfirmRequestDto paymentDto);

    /**
     * Handle payment failure
     */
    void handlePaymentFailure(UUID bookingId, String reason);

    /**
     * Cancel booking (user or timeout) - releases seats
     */
    BookingResponse cancelBooking(UUID bookingId, BookingCancellationRequestDto requestDto);

    /**
     * Get booking by ID (with full details)
     */
    BookingResponse getBookingById(UUID bookingId);

    /**
     * Get user's booking history
     */
    Page<BookingResponse> getBookingsByUser(UUID userId, Pageable pageable);

    /**
     * Get bookings for a show (admin)
     */
    Page<BookingResponse> getBookingsByShow(UUID showId, Pageable pageable);

    /**
     * Get booking by reference number (user lookup)
     */
    BookingResponse getBookingByReference(String bookingReference);
}

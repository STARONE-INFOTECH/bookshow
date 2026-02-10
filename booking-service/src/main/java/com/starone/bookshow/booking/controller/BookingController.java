package com.starone.bookshow.booking.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.starone.bookshow.booking.dto.BookingCancellationRequestDto;
import com.starone.bookshow.booking.dto.BookingResponse;
import com.starone.bookshow.booking.dto.PaymentConfirmRequestDto;
import com.starone.bookshow.booking.service.IBookingService;
import com.starone.springcommon.response.record.ApiResponse;
import com.starone.springcommon.response.util.ApiResponses;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final IBookingService bookingService;

    // ====================== USER ENDPOINTS ======================

    /**
     * Create a new booking (locks seats, returns pending booking for payment)
     */
    /*@PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookingResponseDto> createBooking(
            @AuthenticationPrincipal UUID userId,  // from JWT
            @Valid @RequestBody BookingRequestDto requestDto) {

        BookingResponseDto response = bookingService.createBooking(userId, requestDto);
        return ApiResponse.success(response);
    }*/

    /**
     * Get user's own booking history
     */
    /*@GetMapping("/my")
    public ApiResponse<Page<BookingResponseDto>> getMyBookings(
            @AuthenticationPrincipal UUID userId,
            @PageableDefault(size = 20, sort = "bookingTime,desc") Pageable pageable) {

        Page<BookingResponseDto> page = bookingService.getBookingsByUser(userId, pageable);
        return ApiResponse.success(page);
    }*/

    /**
     * Get specific booking by ID (user can only access own)
     */
    @GetMapping("/{bookingId}")
    public ApiResponse<BookingResponse> getBookingById(@PathVariable UUID bookingId) {
        BookingResponse response = bookingService.getBookingById(bookingId);
        return ApiResponses.success(response);
    }

    /**
     * Get booking by reference number (for ticket lookup)
     */
    @GetMapping("/reference/{bookingReference}")
    public ApiResponse<BookingResponse> getByReference(
            @PathVariable String bookingReference) {
        BookingResponse response = bookingService.getBookingByReference(bookingReference);
        return ApiResponses.success(response);
    }

    /**
     * Cancel booking (only if PENDING or within policy)
     */
    @PostMapping("/{bookingId}/cancel")
    public ApiResponse<BookingResponse> cancelBooking(
            @PathVariable UUID bookingId,
            @Valid @RequestBody BookingCancellationRequestDto requestDto) {

        BookingResponse response = bookingService.cancelBooking(bookingId, requestDto);
        return ApiResponses.success(response);
    }

    // ====================== PAYMENT GATEWAY WEBHOOK ======================

    /**
     * Payment gateway callback/webhook (no auth - verify signature inside service)
     */
    @PostMapping("/webhook/payment")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> paymentWebhook(@RequestBody PaymentConfirmRequestDto paymentDto) {
        // Service will verify signature, find booking, confirm or fail
        // bookingService.handlePaymentWebhook(paymentDto);
        return ApiResponses.success(null);
    }

    // ====================== ADMIN ENDPOINTS ======================

   // @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/show/{showId}")
    public ApiResponse<Page<BookingResponse>> getBookingsByShow(
            @PathVariable UUID showId,
            @PageableDefault(size = 50, sort = "bookingTime") Pageable pageable) {

        Page<BookingResponse> page = bookingService.getBookingsByShow(showId, pageable);
        return ApiResponses.success(page);
    }
}

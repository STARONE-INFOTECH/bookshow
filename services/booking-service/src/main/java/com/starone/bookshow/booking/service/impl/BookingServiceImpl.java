package com.starone.bookshow.booking.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starone.bookshow.booking.client.IShowClient;
import com.starone.bookshow.booking.client.IShowSeatClient;
import com.starone.bookshow.booking.dto.BookingCancellationRequestDto;
import com.starone.bookshow.booking.dto.BookingRequestDto;
import com.starone.bookshow.booking.dto.BookingResponse;
import com.starone.bookshow.booking.dto.PaymentConfirmRequestDto;
import com.starone.bookshow.booking.entity.Booking;
import com.starone.bookshow.booking.entity.BookingSeat;
import com.starone.bookshow.booking.exception.BookingException;
import com.starone.bookshow.booking.mapper.IBookingMapper;
import com.starone.bookshow.booking.mapper.IBookingSeatMapper;
import com.starone.bookshow.booking.repository.IBookingRepository;
import com.starone.bookshow.booking.repository.IBookingSeatRepository;
import com.starone.bookshow.booking.service.IBookingService;
import com.starone.bookshow.booking.util.TicketGenerator;
import com.starone.common.enums.BookingStatus;
import com.starone.springcommon.exceptions.errorcodes.ErrorCode;
import com.starone.springcommon.response.record.ShowResponse;
import com.starone.springcommon.response.record.ShowSeatResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements IBookingService {

    private final IBookingRepository bookingRepository;
    private final IBookingSeatRepository bookingSeatRepository;
    private final IBookingMapper bookingMapper;
    private final IBookingSeatMapper bookingSeatMapper;
    private final IShowSeatClient showSeatClient;  // internal seat locking
    private final IShowClient showClient;            // Feign to show-service

    // TicketGenerator can be a separate service/component
    private final TicketGenerator ticketGenerator;

    @Override
    public BookingResponse createBooking(UUID userId, BookingRequestDto requestDto) {
        // Validate show exists and get details
        ShowResponse show = showClient.getShowById(requestDto.getShowId());

        // Lock seats
        List<ShowSeatResponse> lockedSeats = showSeatClient.lockSeats(
                requestDto.getShowId(), requestDto.getSeatNumbers(), userId);

        // Calculate amount (from locked seats prices)
        double totalAmount = lockedSeats.stream()
                .mapToDouble(ShowSeatResponse::price)
                .sum();

        // Create booking
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setShowId(requestDto.getShowId());
        booking.setBookingTime(LocalDateTime.now());
        booking.setTotalAmount(totalAmount);
        booking.setFinalAmount(totalAmount);  // discount later
        booking.setStatus(BookingStatus.PENDING);
        booking.setBookingReference(generateBookingReference());

        // Create BookingSeat entries
        for (ShowSeatResponse lockedSeat : lockedSeats) {
            BookingSeat bs = new BookingSeat();
            bs.setBooking(booking);
            bs.setShowSeatId(lockedSeat.id());  
            bs.setSeatNumber(lockedSeat.seatNumber());
            bs.setSeatType(lockedSeat.seatType());
            bs.setPriceCategory(lockedSeat.priceCategory());
            bs.setSeatPrice(lockedSeat.price());
            booking.getBookedSeats().add(bs);
        }

        booking = bookingRepository.save(booking);

        return enrichBookingResponse(booking, show);
    }

    @Override
    public BookingResponse confirmPayment(UUID bookingId, PaymentConfirmRequestDto paymentDto) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BookingException(ErrorCode.BOOKING_NOT_PENDING, "Booking not pending");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentTime(LocalDateTime.now());
        booking.setPaymentId(paymentDto.getPaymentGatewayId());

        // Generate ticket (QR, PDF link)
        booking.setTicketQrCodeUrl(ticketGenerator.generateQrCode(booking));

        booking = bookingRepository.save(booking);

        ShowResponse show = showClient.getShowById(booking.getShowId());
        return enrichBookingResponse(booking, show);
    }

    @Override
    public void handlePaymentFailure(UUID bookingId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.FAILED);
            bookingRepository.save(booking);

            // Release locked seats
            List<String> seatNumbers = booking.getBookedSeats().stream()
                    .map(BookingSeat::getSeatNumber)
                    .toList();
            showSeatClient.releaseSeats(booking.getShowId(), seatNumbers);
        }
    }

    @Override
    public BookingResponse cancelBooking(UUID bookingId, BookingCancellationRequestDto requestDto) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            // Refund logic via payment-service
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);

        // Release seats
        List<String> seatNumbers = booking.getBookedSeats().stream()
                .map(BookingSeat::getSeatNumber)
                .toList();
        showSeatClient.releaseSeats(booking.getShowId(), seatNumbers);

        ShowResponse show = showClient.getShowById(booking.getShowId());
        return enrichBookingResponse(booking, show);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException(ErrorCode.BOOKING_NOT_FOUND));
        ShowResponse show = showClient.getShowById(booking.getShowId());
        return enrichBookingResponse(booking, show);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsByUser(UUID userId, Pageable pageable) {
        Page<Booking> page = bookingRepository.findByUserId(userId, pageable);
        return page.map(booking -> {
            ShowResponse show = showClient.getShowById(booking.getShowId());
            return enrichBookingResponse(booking, show);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsByShow(UUID showId, Pageable pageable) {
        Page<Booking> page = bookingRepository.findByShowId(showId, pageable);
        ShowResponse show = showClient.getShowById(showId);
        return page.map(booking -> enrichBookingResponse(booking, show));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new BookingException(ErrorCode.BOOKING_NOT_FOUND));
        ShowResponse show = showClient.getShowById(booking.getShowId());
        return enrichBookingResponse(booking, show);
    }

    private String generateBookingReference() {
        return "BKN-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BookingResponse enrichBookingResponse(Booking booking, ShowResponse show) {
        BookingResponse dto = bookingMapper.toResponseDto(booking);
        dto.setMovieTitle(show.movieTitle());
        dto.setTheaterName(show.theaterName());
        dto.setScreenName(show.screenName());
        dto.setShowStartTime(show.showStartTime());
        dto.setShowType(show.showType());
        return dto;
    }
}
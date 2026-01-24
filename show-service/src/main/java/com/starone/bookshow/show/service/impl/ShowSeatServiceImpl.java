package com.starone.bookshow.show.service.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starone.bookshow.show.entity.ShowSeat;
import com.starone.bookshow.show.mapper.IShowSeatMapper;
import com.starone.bookshow.show.repository.IShowRepository;
import com.starone.bookshow.show.repository.IShowSeatRepository;
import com.starone.bookshow.show.service.IShowSeatService;
import com.starone.common.enums.SeatStatus;
import com.starone.common.error.ErrorCodes;
import com.starone.common.exceptions.ConflictException;
import com.starone.common.exceptions.NotFoundException;
import com.starone.common.response.record.ShowSeatResponse;

import lombok.RequiredArgsConstructor;

@Service("showSeatService")
@RequiredArgsConstructor
@Transactional
public class ShowSeatServiceImpl implements IShowSeatService {

    private final IShowSeatRepository showSeatRepository;
    private final IShowRepository showRepository;
    private final IShowSeatMapper showSeatMapper;

    private static final int LOCK_DURATION_MINUTES = 10;

    @Override
    public List<ShowSeatResponse> lockSeats(UUID showId, List<String> seatNumbers, UUID userId) {
        validateShowExists(showId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusMinutes(LOCK_DURATION_MINUTES);

        int lockedCount = showSeatRepository.lockSeats(showId, seatNumbers, now, expiry, userId);

        if (lockedCount == 0) {
            throw new ConflictException(ErrorCodes.SEAT_NOT_AVAILABLE, "No seats were available to lock");
        }

        if (lockedCount < seatNumbers.size()) {
            // Partial success - rollback partial locks
            releaseSeats(showId, seatNumbers);
            throw new ConflictException(ErrorCodes.SEAT_NOT_AVAILABLE, "Only some seats were available. Try again.");
        }

        return showSeatRepository.findByShowIdAndSeatNumberIn(showId, seatNumbers)
                .stream()
                .map(showSeatMapper::toResponseDto)
                .toList();
    }

    @Override
    public void releaseSeats(UUID showId, List<String> seatNumbers) {
        List<ShowSeat> seats = showSeatRepository.findByShowIdAndSeatNumberIn(showId, seatNumbers);

        seats.forEach(seat -> {
            if (seat.getStatus() == SeatStatus.LOCKED) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setLockedAt(null);
                seat.setLockedUntil(null);
                seat.setLockedByUserId(null);
            }
        });

        showSeatRepository.saveAll(seats);
    }

    @Override
    public void bookSeats(UUID showId, List<String> seatNumbers, UUID bookingId) {
        List<ShowSeat> seats = showSeatRepository.findByShowIdAndSeatNumberIn(showId, seatNumbers);

        if (seats.size() != seatNumbers.size()) {
            throw new NotFoundException(ErrorCodes.NOT_FOUND, "Some seats not found");
        }

        seats.forEach(seat -> {
            if (seat.getStatus() != SeatStatus.LOCKED) {
                throw new ConflictException(ErrorCodes.SEAT_NOT_LOCKED, "Seat " + seat.getSeatNumber() + " is not locked");
            }
            seat.setStatus(SeatStatus.BOOKED);
            seat.setBookingId(bookingId);
            seat.setLockedAt(null);
            seat.setLockedUntil(null);
            seat.setLockedByUserId(null);
        });

        showSeatRepository.saveAll(seats);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowSeatResponse> getSeatStatus(UUID showId, List<String> seatNumbers) {
        return showSeatRepository.findByShowIdAndSeatNumberIn(showId, seatNumbers)
                .stream()
                .map(showSeatMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowSeatResponse> getAllSeatsForShow(UUID showId) {
        validateShowExists(showId);
        return showSeatRepository.findByShowId(showId)
                .stream()
                .sorted(Comparator.comparing(ShowSeat::getSeatNumber))
                .map(showSeatMapper::toResponseDto)
                .toList();
    }

    @Override
    public int releaseExpiredLocks(LocalDateTime expiryTime) {
        List<ShowSeat> expired = showSeatRepository.findByStatusAndLockedUntilBefore(SeatStatus.LOCKED, expiryTime);

        expired.forEach(seat -> {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setLockedAt(null);
            seat.setLockedUntil(null);
            seat.setLockedByUserId(null);
        });

        if (!expired.isEmpty()) {
            showSeatRepository.saveAll(expired);
        }

        return expired.size();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean areSeatsAvailable(UUID showId, List<String> seatNumbers) {
        long availableCount = showSeatRepository.findByShowIdAndSeatNumberIn(showId, seatNumbers)
                .stream()
                .filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE)
                .count();

        return availableCount == seatNumbers.size();
    }

      /*
     * =====================================================================
     * ------------------ Helper to enrich with Show Seats -----------------
     * =====================================================================
     */

    private void validateShowExists(UUID showId) {
        showRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND, "Show not found"));
    }
}

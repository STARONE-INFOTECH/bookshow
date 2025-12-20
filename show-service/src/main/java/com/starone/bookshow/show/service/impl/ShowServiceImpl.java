package com.starone.bookshow.show.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starone.bookshow.show.client.IMovieClient;
import com.starone.bookshow.show.client.TheaterClient;
import com.starone.bookshow.show.dto.ShowRequestDto;
import com.starone.bookshow.show.entity.Show;
import com.starone.bookshow.show.entity.ShowSeat;
import com.starone.bookshow.show.mapper.IShowMapper;
import com.starone.bookshow.show.mapper.IShowSeatMapper;
import com.starone.bookshow.show.repository.IShowRepository;
import com.starone.bookshow.show.repository.IShowSeatRepository;
import com.starone.bookshow.show.service.IShowService;
import com.starone.common.dto.MovieResponseDto;
import com.starone.common.dto.ScreenResponseDto;
import com.starone.common.dto.ShowResponseDto;
import com.starone.common.dto.ShowSeatResponseDto;
import com.starone.common.dto.TheaterResponseDto;
import com.starone.common.enums.SeatStatus;
import com.starone.common.error.ErrorCodes;
import com.starone.common.exceptions.ConflictException;
import com.starone.common.exceptions.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ShowServiceImpl implements IShowService {

    private final IShowRepository showRepository;
    private final IShowSeatRepository showSeatRepository;
    private final IShowMapper showMapper;
    private final IShowSeatMapper showSeatMapper;
    private final IMovieClient movieClient; // Feign
    private final TheaterClient theaterClient; // Feign for screen/theater

    @Override
    public ShowResponseDto createShow(ShowRequestDto requestDto) {
        // Validate movie and screen exist
        movieClient.getMovieById(requestDto.getMovieId());
        ScreenResponseDto screen = theaterClient.getScreenById(requestDto.getScreenId());

        // Check no overlapping show on same screen
        if (showRepository.existsByScreenIdAndShowStartTimeBetween(
                requestDto.getScreenId(),
                requestDto.getShowStartTime().minusMinutes(30), // buffer
                requestDto.getShowStartTime().plusHours(4))) {
            throw new ConflictException(ErrorCodes.CONFLICT, "Screen has overlapping show");
        }

        Show show = showMapper.toEntity(requestDto);
        show.setShowEndTime(calculateEndTime(show.getShowStartTime(), screen));
        show.setTotalSeats(screen.getTotalSeats());
        show.setAvailableSeats(screen.getTotalSeats());

        show = showRepository.save(show);

        // Generate ShowSeats from screen layout
        generateShowSeats(show, screen.getSeatLayoutJson(), requestDto.getPricingJson());

        return enrichShowResponse(show);
    }

    @Override
    @Transactional(readOnly = true)
    public ShowResponseDto getShowById(UUID showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND));
        return enrichShowResponse(show);
    }

    @Override
    public ShowResponseDto updateShow(UUID showId, ShowRequestDto requestDto) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND));

        showMapper.updateEntity(requestDto, show);
        if (requestDto.getShowStartTime() != null) {
            show.setShowEndTime(calculateEndTime(requestDto.getShowStartTime(), null)); // recalc if needed
        }

        show = showRepository.save(show);
        return enrichShowResponse(show);
    }

    @Override
    public void deactivateShow(UUID showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND));
        show.setActive(false);
        showRepository.save(show);
    }

    @Override
    public void activateShow(UUID showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND));
        show.setActive(true);
        showRepository.save(show);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShowResponseDto> getShowsByMovieId(UUID movieId, Pageable pageable) {
        Page<Show> page = showRepository.findByMovieId(movieId, pageable);
        return page.map(this::enrichShowResponse);
    }

    // ... other list methods similar (use repository queries + map to enrich)

    @Override
    public List<ShowSeatResponseDto> lockSeats(UUID showId, List<String> seatNumbers, UUID userId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusMinutes(10);

        int lockedCount = showSeatRepository.lockSeats(showId, seatNumbers, now, expiry, userId);

        if (lockedCount != seatNumbers.size()) {
            throw new ConflictException(ErrorCodes.CONFLICT, "Some seats are not available");
        }

        return showSeatRepository.findByShowIdAndSeatNumberIn(showId, seatNumbers)
                .stream()
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
        showSeatRepository.saveAll(expired);
        return expired.size();
    }

    @Override
    public boolean areSeatsAvailable(UUID showId, List<String> seatNumbers) {
        long available = showSeatRepository.countByShowIdAndStatus(showId, SeatStatus.AVAILABLE);
        return showSeatRepository.findByShowIdAndSeatNumberIn(showId, seatNumbers)
                .stream()
                .allMatch(seat -> seat.getStatus() == SeatStatus.AVAILABLE);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShowResponseDto> getShowsByScreenAndDate(UUID screenId, LocalDateTime date, Pageable pageable) {
        // Validate screen exists
        theaterClient.getScreenById(screenId);

        // Define date range: start of day to end of day
        LocalDateTime startOfDay = date.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = date.withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        Page<Show> page = showRepository.findByScreenIdAndActiveTrueAndShowStartTimeBetween(
                screenId, startOfDay, endOfDay, pageable);

        return page.map(this::enrichShowResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShowResponseDto> getTodayShows(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59, 999999999);

        Page<Show> page = showRepository.findTodayShows(startOfDay, endOfDay, pageable);
        return page.map(this::enrichShowResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShowResponseDto> getUpcomingShows(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();

        Page<Show> page = showRepository.findByActiveTrueAndShowStartTimeAfter(now, pageable);
        return page.map(this::enrichShowResponse);
    }

    // Helper methods
    private void generateShowSeats(Show show, String layoutJson, String pricingJson) {
        // Parse layoutJson, create ShowSeat for each seat
        // Use Jackson or custom parser
        // Set price from pricingJson + category
        // status = AVAILABLE
    }

    private LocalDateTime calculateEndTime(LocalDateTime start, ScreenResponseDto screen) {
        // Fetch movie duration from movie-service or pass in request
        // return start.plusMinutes(duration);
        return start.plusHours(2).plusMinutes(30); // placeholder
    }

    private ShowResponseDto enrichShowResponse(Show show) {
        ShowResponseDto dto = showMapper.toResponseDto(show);

        MovieResponseDto movie = movieClient.getMovieById(show.getMovieId());
        dto.setMovieTitle(movie.getTitle());
        dto.setMoviePosterUrl(movie.getPosterUrl());

        ScreenResponseDto screen = theaterClient.getScreenById(show.getScreenId());
        dto.setScreenName(screen.getName());

        TheaterResponseDto theater = theaterClient.getTheaterByScreenId(show.getScreenId());
        dto.setTheaterId(theater.getId());
        dto.setTheaterName(theater.getName());
        dto.setTheaterCity(theater.getCity());

        return dto;
    }
}

package com.starone.bookshow.movie.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starone.bookshow.movie.client.PersonClient;
import com.starone.bookshow.movie.dto.MovieCreditRequestDto;
import com.starone.bookshow.movie.dto.MovieCreditResponseDto;
import com.starone.bookshow.movie.entity.Movie;
import com.starone.bookshow.movie.entity.MovieCredit;
import com.starone.bookshow.movie.mapper.IMovieCreditMapper;
import com.starone.bookshow.movie.repository.IMovieCreditRepository;
import com.starone.bookshow.movie.repository.IMovieRepository;
import com.starone.bookshow.movie.service.IMovieCreditService;
import com.starone.common.dto.PersonResponseDto;
import com.starone.common.enums.Profession;
import com.starone.common.error.ErrorCodes;
import com.starone.common.exceptions.BadRequestException;
import com.starone.common.exceptions.ConflictException;
import com.starone.common.exceptions.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovieCreditServiceImpl implements IMovieCreditService {

    private final IMovieCreditRepository movieCreditRepository;
    private final IMovieRepository movieRepository;
    private final IMovieCreditMapper movieCreditMapper; 
    private final PersonClient personClient;

    @Override
    public MovieCreditResponseDto addCredit(UUID movieId, MovieCreditRequestDto requestDto) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.MOVIE_NOT_FOUND));

        if (movieCreditRepository.existsByMovieIdAndPersonIdAndRoleInMovie(
                movieId, requestDto.getPersonId(), requestDto.getProfession())) {
            throw new ConflictException(ErrorCodes.CONFLICT, "This person already has this role in the movie");
        }

        personClient.getPersonById(requestDto.getPersonId()); // validate person exists

        MovieCredit credit = movieCreditMapper.toEntity(requestDto); // ← BaseMapper method
        credit.setMovie(movie); // manually set parent

        credit = movieCreditRepository.save(credit);

        return enrichAndMap(credit);
    }

    @Override
    public MovieCreditResponseDto updateCredit(UUID creditId, MovieCreditRequestDto requestDto) {
        MovieCredit credit = movieCreditRepository.findById(creditId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND, "Credit not found"));

        // Duplicate check if role or person changing
        if (requestDto.getProfession() != null && requestDto.getPersonId() != null &&
                (!requestDto.getProfession().equals(credit.getProfession()) ||
                        !requestDto.getPersonId().equals(credit.getPersonId()))) {

            if (movieCreditRepository.existsByMovieIdAndPersonIdAndRoleInMovie(
                    credit.getMovie().getId(), requestDto.getPersonId(), requestDto.getProfession())) {
                throw new ConflictException(ErrorCodes.CONFLICT, "Duplicate role for this person in movie");
            }
        }

        if (requestDto.getPersonId() != null && !requestDto.getPersonId().equals(credit.getPersonId())) {
            personClient.getPersonById(requestDto.getPersonId());
        }

        movieCreditMapper.updateEntity(requestDto, credit); // ← BaseMapper partial update (ignores nulls)

        credit = movieCreditRepository.save(credit);

        return enrichAndMap(credit);
    }

    @Override
    public void removeCredit(UUID creditId) {
        if (!movieCreditRepository.existsById(creditId)) {
            throw new NotFoundException(ErrorCodes.NOT_FOUND, "Credit not found");
        }
        movieCreditRepository.deleteById(creditId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieCreditResponseDto> getCreditsByMovieId(UUID movieId) {
        List<MovieCredit> credits = movieCreditRepository.findByMovieId(movieId);

        return credits.stream()
                .sorted(Comparator
                        .comparingInt(c -> c.getBillingOrder() == null ? Integer.MAX_VALUE : c.getBillingOrder()))
                .map(this::enrichAndMap)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieCreditResponseDto> getCreditsByMovieIdPaginated(UUID movieId, Pageable pageable) {
        Page<MovieCredit> page = movieCreditRepository.findByMovieId(movieId, pageable);

        return page.map(this::enrichAndMap); // BaseMapper not needed here since we enrich manually
    }

    @Override
    @Transactional(readOnly = true)
    public MovieCreditResponseDto getCreditById(UUID creditId) {
        MovieCredit credit = movieCreditRepository.findById(creditId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND));
        return enrichAndMap(credit);
    }

    @Override
    public List<MovieCreditResponseDto> reorderCredits(UUID movieId, List<MovieCreditRequestDto> orderedDtos) {
        List<MovieCredit> credits = movieCreditRepository.findByMovieId(movieId);

        for (int i = 0; i < orderedDtos.size(); i++) {
            MovieCreditRequestDto dto = orderedDtos.get(i);
            MovieCredit credit = credits.stream()
                    .filter(c -> c.getPersonId().equals(dto.getPersonId()) &&
                            c.getProfession().equals(dto.getProfession()))
                    .findFirst()
                    .orElseThrow(
                            () -> new BadRequestException(ErrorCodes.BAD_REQUEST, "Invalid credit in reorder list"));

            credit.setBillingOrder(i + 1);
        }

        movieCreditRepository.saveAll(credits);

        return getCreditsByMovieId(movieId);
    }

    @Override
    public boolean existsCredit(UUID movieId, UUID personId, Profession role) {
        return movieCreditRepository.existsByMovieIdAndPersonIdAndRoleInMovie(movieId, personId, role);
    }

    // Helper: enrich basic mapped DTO with person details
    private MovieCreditResponseDto enrichAndMap(MovieCredit credit) {
        MovieCreditResponseDto dto = movieCreditMapper.toResponseDto(credit); // ← BaseMapper basic mapping

        PersonResponseDto person = personClient.getPersonById(credit.getPersonId());
        dto.setPersonName(person.getName());
        dto.setNickName(person.getNickName());
        dto.setProfileImg(person.getProfileImg());

        return dto;
    }

}

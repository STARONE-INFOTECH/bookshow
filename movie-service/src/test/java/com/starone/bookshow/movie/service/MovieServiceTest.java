package com.starone.bookshow.movie.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.starone.bookshow.movie.client.PersonClient;
import com.starone.bookshow.movie.dto.MovieRequestDto;
import com.starone.bookshow.movie.entity.Movie;
import com.starone.bookshow.movie.entity.MovieCredit;
import com.starone.bookshow.movie.helper.TestDataFactory;
import com.starone.bookshow.movie.mapper.IMovieMapper;
import com.starone.bookshow.movie.repository.IMovieRepository;
import com.starone.bookshow.movie.service.impl.MovieServiceImpl;
import com.starone.common.dto.ApiResponse;
import com.starone.common.dto.MovieCreditResponseDto;
import com.starone.common.dto.MovieResponseDto;
import com.starone.common.error.ErrorCodes;
import com.starone.common.exceptions.BadRequestException;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private IMovieRepository movieRepository;

    @Mock
    private IMovieMapper movieMapper;

    @Mock
    private PersonClient personClient;

    @Mock
    private IMovieCreditService movieCreditService;

    @InjectMocks
    private MovieServiceImpl movieService;

    private MovieRequestDto validRequestDto;
    private MovieResponseDto expectedResponseDto;
    private Movie mappedMovie;
    private Movie savedMovie;

    @BeforeEach
    void setUp() {
        validRequestDto = TestDataFactory.createValidMovieRequestDto();
        mappedMovie = TestDataFactory.createMovieFromDto(validRequestDto);
        savedMovie = TestDataFactory.createMovieFromDto(validRequestDto);
        savedMovie.setId(UUID.randomUUID());
        savedMovie.setActive(true);
        expectedResponseDto = TestDataFactory.createResponseFromMovie(savedMovie);
    }

    @Test
    void create_validRequestWithCredits_savesAndReturnsResponse() {
        // Arrange (Given)
        Set<UUID> personIds = mappedMovie.getMovieCredits().stream()
                .map(MovieCredit::getPersonId)
                .collect(Collectors.toSet());

        // Let mapper do its job
        when(movieMapper.toEntity(validRequestDto)).thenReturn(mappedMovie);

        // Person service validates successfully
        when(personClient.validatePersonIds(personIds)).thenReturn(ApiResponse.success(personIds));

        // Repository saves and returns the entity with ID
        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);

        // IMPORTANT: Stub the enrichment dependency
        MovieResponseDto baseDto = new MovieResponseDto();
        baseDto.setId(savedMovie.getId());
        baseDto.setTitle(savedMovie.getTitle());
        baseDto.setDurationMinutes(148);
        when(movieMapper.toResponseDto(any(Movie.class))).thenReturn(baseDto);

        List<MovieCreditResponseDto> enrichedCredits = TestDataFactory.createResponseFromMovie(savedMovie)
                .getMovieCredits();
        when(movieCreditService.getCreditsByMovieId(savedMovie.getId())).thenReturn(enrichedCredits);

        // Act (When)
        MovieResponseDto result = movieService.create(validRequestDto);

        // Assert (Then) - State verification
        assertNotNull(result);
        assertEquals(savedMovie.getId(), result.getId());
        assertEquals("Inception", result.getTitle());
        assertEquals(148, result.getDurationMinutes());
        assertNotNull(result.getMovieCredits());
        assertEquals(2, result.getMovieCredits().size()); // assuming 2 credits in test data

        // Behavior verification
        verify(movieRepository).save(argThat(movie -> movie != null &&
                movie.getTitle().equals("Inception")));
        verify(personClient).validatePersonIds(personIds);
        verify(movieCreditService).getCreditsByMovieId(savedMovie.getId());
    }

    @Test
    void create_validRequestWithoutCredits_savesAndReturnsResponse() {
        // Arrange (Given)
        validRequestDto.setMovieCredits(List.of());
        mappedMovie.setMovieCredits(List.of());

        when(movieMapper.toEntity(validRequestDto)).thenReturn(mappedMovie);
        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);

        when(movieMapper.toResponseDto(savedMovie)).thenReturn(expectedResponseDto);
        // No credits → enrichment should return empty list
        when(movieCreditService.getCreditsByMovieId(savedMovie.getId()))
                .thenReturn(Collections.emptyList());

        // Act (When)
        MovieResponseDto result = movieService.create(validRequestDto);

        // Assert (Then)
        assertNotNull(result);
        assertEquals(expectedResponseDto, result);
        verify(movieRepository).save(mappedMovie);
    }

    @Test
    void create_withCredits_syncsBidirectionalRelationship() {
        // Arrange (Given)
        MovieCredit credit1 = mappedMovie.getMovieCredits().get(0);
        MovieCredit credit2 = mappedMovie.getMovieCredits().get(1);
        Set<UUID> personIds = mappedMovie.getMovieCredits().stream()
                .map(MovieCredit::getPersonId)
                .collect(Collectors.toSet());
        when(movieMapper.toEntity(validRequestDto)).thenReturn(mappedMovie);
        when(personClient.validatePersonIds(personIds)).thenReturn(ApiResponse.success(personIds));
        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);

        when(movieMapper.toResponseDto(any(Movie.class))).thenReturn(new MovieResponseDto());
        when(movieCreditService.getCreditsByMovieId(any(UUID.class))).thenReturn(Collections.emptyList());
        // Act (When)
        movieService.create(validRequestDto);

        // Assert (Then)
        assertSame(mappedMovie, credit1.getMovie());
        assertSame(mappedMovie, credit2.getMovie());
    }

    @Test
    void create_withCredits_callsPersonValidation() {
        // Arrange (Given)
        Set<UUID> personIds = mappedMovie.getMovieCredits().stream()
                .map(MovieCredit::getPersonId)
                .collect(Collectors.toSet());

        when(movieMapper.toEntity(validRequestDto))
                .thenReturn(mappedMovie);
        when(personClient.validatePersonIds(personIds))
                .thenReturn(ApiResponse.success(personIds));
        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);

        when(movieMapper.toResponseDto(savedMovie)).thenReturn(expectedResponseDto);
        when(movieCreditService.getCreditsByMovieId(any(UUID.class))).thenReturn(Collections.emptyList());
        // Act (When)
        movieService.create(validRequestDto);

        // Assert (Then)
        verify(personClient).validatePersonIds(personIds);
    }

    @Test
    void create_withNoCredits_doesNotCallPersonValidation() {
        // Arrange (Given)
        validRequestDto.setMovieCredits(List.of());
        mappedMovie.setMovieCredits(List.of());
        when(movieMapper.toEntity(validRequestDto)).thenReturn(mappedMovie);
        when(movieRepository.save(mappedMovie)).thenReturn(savedMovie);

        when(movieMapper.toResponseDto(savedMovie)).thenReturn(expectedResponseDto);
        when(movieCreditService.getCreditsByMovieId(any(UUID.class))).thenReturn(Collections.emptyList());
        // Act (When)
        movieService.create(validRequestDto);

        // Assert (Then)
        verify(personClient, never()).validatePersonIds(anySet());
        verify(movieRepository).save(mappedMovie);
    }

    @Test
    void create_invalidPersonIds_throwsBadRequest() {
        // Arrange (Given)
        validRequestDto = TestDataFactory.createMovieRequestWithInvalidPersonId();
        mappedMovie = TestDataFactory.createMovieFromDto(validRequestDto);
        Set<UUID> personIds = mappedMovie.getMovieCredits().stream()
                .map(MovieCredit::getPersonId)
                .collect(Collectors.toSet());

        when(movieMapper.toEntity(validRequestDto)).thenReturn(mappedMovie);
        when(personClient.validatePersonIds(personIds))
                .thenReturn(ApiResponse.success(Collections.emptySet()));

        // Act (When)
        BadRequestException ex = assertThrows(BadRequestException.class, () -> movieService.create(validRequestDto));

        // Assert (Then)
        assertEquals(ErrorCodes.MOVIE_INVALID_PERSON_IDS, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("The following person IDs do not exist: "));
    }

    @Test
    void create_personServiceReturnsErrorStatus_throwsBadRequest() {
        // Arrange (Given)
        Set<UUID> personIds = mappedMovie.getMovieCredits().stream()
                .map(MovieCredit::getPersonId)
                .collect(Collectors.toSet());
        when(movieMapper.toEntity(validRequestDto)).thenReturn(mappedMovie);

        ApiResponse<Set<UUID>> errorResponse = ApiResponse.error(ErrorCodes.MOVIE_INVALID_PERSON_IDS,
                "Person service temporary error");
        when(personClient.validatePersonIds(personIds)).thenReturn(errorResponse);

        // Act (When)
        BadRequestException ex = assertThrows(BadRequestException.class, () -> movieService.create(validRequestDto));

        // Assert (Then)
        assertTrue(ex.getMessage().contains("Person validation failed:"));
    }
}

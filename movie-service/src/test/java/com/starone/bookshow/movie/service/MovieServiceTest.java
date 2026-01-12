package com.starone.bookshow.movie.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.starone.bookshow.movie.client.IPersonClient;
import com.starone.bookshow.movie.dto.MovieCreditRequestDto;
import com.starone.bookshow.movie.dto.MovieRequestDto;
import com.starone.bookshow.movie.entity.Movie;
import com.starone.bookshow.movie.entity.MovieCredit;
import com.starone.bookshow.movie.helper.TestDataFactory;
import com.starone.bookshow.movie.mapper.IMovieCreditMapper;
import com.starone.bookshow.movie.mapper.IMovieMapper;
import com.starone.bookshow.movie.repository.IMovieRepository;
import com.starone.bookshow.movie.service.impl.MovieServiceImpl;
import com.starone.common.enums.Profession;
import com.starone.common.error.ErrorCodes;
import com.starone.common.exceptions.BadRequestException;
import com.starone.common.response.record.MovieCreditPersonResponse;
import com.starone.common.response.record.MovieResponse;
import com.starone.common.response.record.PersonProfessionSync;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private IMovieRepository movieRepository;

    @Mock
    private IMovieMapper movieMapper;

    @Mock
    private IMovieCreditMapper creditMapper;

    @Mock
    private IPersonClient personClient;

    @Mock
    private IMovieCreditService movieCreditService;

    @InjectMocks
    private MovieServiceImpl movieService;

    private MovieRequestDto validRequestDto;
    private MovieResponse expectedResponseDto;
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
    // ==================== CREATE TESTS ====================

    @Nested
    @DisplayName("create() method tests")
    class CreateTests {
        @Test
        void create_nullDto_throwBadRequest() {
            // Arrange (Given)
            BadRequestException ex = assertThrows(BadRequestException.class, () -> movieService.create(null));

            assertEquals(ErrorCodes.BAD_REQUEST, ex.getErrorCode());
            assertEquals("Movie requestDto is null", ex.getMessage());

            verifyNoInteractions(movieMapper, movieRepository);

        }

        @Test
        void create_success_validMovieRequestDto() {
            // Arrange
            when(movieMapper.toEntity(any())).thenReturn(mappedMovie);
            when(movieRepository.save(any())).thenReturn(savedMovie);
            when(movieMapper.toResponseDto(any())).thenReturn(expectedResponseDto);
            when(personClient.getAllPersonByIds(anySet()))
                    .thenReturn(TestDataFactory.createPersonsWithMissingProfessions());
            // Act
            MovieResponse result = movieService.create(validRequestDto);

            // Assert
            assertEquals(expectedResponseDto, result);
            verify(movieMapper).toEntity(validRequestDto);
            verify(movieRepository).save(mappedMovie);
            verify(movieMapper).toResponseDto(savedMovie);
        }

        @Test
        void create_success_nullCredits_normalizedToEmpty() {
            // Arrange
            validRequestDto.setMovieCredits(null);
            mappedMovie = TestDataFactory.createMovieFromDto(validRequestDto);
            expectedResponseDto = TestDataFactory.createResponseFromMovie(savedMovie);

            when(movieMapper.toEntity(any())).thenReturn(mappedMovie);
            when(movieRepository.save(any())).thenReturn(savedMovie);
            when(movieMapper.toResponseDto(any())).thenReturn(expectedResponseDto);
            // Act

            MovieResponse result = movieService.create(validRequestDto);
            // Assert
            assertEquals(expectedResponseDto, result);
            verify(creditMapper, never()).toEntity(any());
            verify(movieRepository).save(argThat(movie -> movie.getMovieCredits().isEmpty()));
        }

        @Test
        void create_success_creditsAdded_personIdsExtracted() {
            // Arrange
            mappedMovie = TestDataFactory.createMovieWithEmptyCredits(validRequestDto);
            when(movieMapper.toEntity(any())).thenReturn(mappedMovie);
            when(creditMapper.toEntity(any(MovieCreditRequestDto.class)))
                    .thenAnswer(invocation -> {
                        MovieCreditRequestDto req = invocation.getArgument(0);
                        return TestDataFactory.createMovieCreditFromDto(req); // ← new instance each call
                    });
            when(movieRepository.save(any())).thenReturn(savedMovie);
            when(movieMapper.toResponseDto(any())).thenReturn(expectedResponseDto);
            when(personClient.getAllPersonByIds(anySet()))
                    .thenReturn(TestDataFactory.createPersonsWithAllProfessions());
            // Act
            MovieResponse result = movieService.create(validRequestDto);

            // Assert
            assertEquals(expectedResponseDto, result);
            verify(creditMapper, times(2)).toEntity(any());
            ArgumentCaptor<Movie> captor = ArgumentCaptor.forClass(Movie.class);
            verify(movieRepository).save(captor.capture());
            assertEquals(2, captor.getValue().getMovieCredits().size());
            assertSame(captor.getValue(), captor.getValue().getMovieCredits().get(0).getMovie());
        }

        @Test
        void create_success_newProfessions_synced() {
            // Arrange
            when(movieMapper.toEntity(any())).thenReturn(mappedMovie);
            when(creditMapper.toEntity(any()))
                    .thenReturn(TestDataFactory.createMovieCreditFromDto(validRequestDto.getMovieCredits().get(0)));
            when(movieRepository.save(any())).thenReturn(savedMovie);
            when(movieMapper.toResponseDto(any())).thenReturn(expectedResponseDto);

            when(personClient.getAllPersonByIds(anySet()))
                    .thenReturn(TestDataFactory.createPersonsWithMissingProfessions());

            // Act
            MovieResponse result = movieService.create(validRequestDto);

            // Assert
            assertEquals(expectedResponseDto, result);
            verify(personClient).addProfessionsBulk(argThat(list -> !list.isEmpty()));
        }

        @Test
        void create_success_noNewProfessions() {
            //Arrange
            validRequestDto.getMovieCredits().get(0).setProfessions(Set.of(Profession.ACTOR));
            mappedMovie.getMovieCredits().forEach(credit -> System.out.println("Profession Mapped :"+credit.getProfessions()));
            when(movieMapper.toEntity(any())).thenReturn(mappedMovie);
            when(creditMapper.toEntity(any()))
                .thenReturn(TestDataFactory.createMovieCreditFromDto(validRequestDto.getMovieCredits().get(0)));

            validRequestDto.getMovieCredits().forEach(credit -> System.out.println("Profession request dto:"+credit.getProfessions()));
            when(movieRepository.save(any())).thenReturn(savedMovie);
            savedMovie.getMovieCredits().forEach(credit -> System.out.println("Profession saved :"+credit.getProfessions()));
            when(movieMapper.toResponseDto(any())).thenReturn(expectedResponseDto);
            expectedResponseDto.movieCredits().forEach(credit -> System.out.println("Profession response :"+credit.professions()));
            when(personClient.getAllPersonByIds(anySet()))
            .thenAnswer(invocation ->{
                Set<UUID> ids = invocation.getArgument(0);
                List<MovieCreditPersonResponse> persons = new ArrayList<>();
                for(UUID id : ids){
                    persons.add(new MovieCreditPersonResponse(
                        id, 
                        "Person "+id, 
                        "img.jpeg", 
                        Set.of(Profession.ACTOR, Profession.DIRECTOR)
                    ));
                }
                return persons;
            });
            
            //Act
            MovieResponse result = movieService.create(validRequestDto);
            result.movieCredits().forEach(credit -> System.out.println("Profession result dto:"+credit.professions()));

            //Assert
            assertEquals(expectedResponseDto, result);
            verify(personClient, never()).addProfessionsBulk(anyList());

        }
    }

    // ==================== READ / GET TESTS ====================
    @Nested
    @DisplayName("getById() and retrieval tests")
    class RetrievalTests {
        /*
         * @Test
         * void shouldReturnMovie_whenIdExists() { ... }
         * 
         * @Test
         * void shouldThrowNotFound_whenIdNotExists() { ... }
         */
    }

    // ==================== UPDATE TESTS ====================
    @Nested
    @DisplayName("update() method tests")
    class UpdateTests {
        // update(), activate(), deactivate() tests
    }

    // ==================== QUERY / PAGINATION TESTS ====================
    @Nested
    @DisplayName("Pagination and filtering tests")
    class QueryTests {
        /*
         * @Test
         * void shouldReturnNowShowingMovies() { ... }
         * 
         * @Test
         * void shouldReturnUpcomingMovies() { ... }
         * 
         * @Test
         * void shouldFilterByGenre() { ... }
         */
        // ... getAll, search, filter tests
    }

    // ==================== DELETE TESTS ====================
    @Nested
    @DisplayName("delete() method tests")
    class DeleteTests {
        // ...
    }
}

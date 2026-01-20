package com.starone.bookshow.movie.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import com.starone.bookshow.movie.helper.MovieTestDataFactory;
import com.starone.bookshow.movie.mapper.IMovieCreditMapper;
import com.starone.bookshow.movie.mapper.IMovieMapper;
import com.starone.bookshow.movie.repository.IMovieRepository;
import com.starone.bookshow.movie.service.impl.MovieServiceImpl;
import com.starone.common.enums.Profession;
import com.starone.common.error.ErrorCodes;
import com.starone.common.exceptions.BadRequestException;
import com.starone.common.response.record.MovieCreditPersonResponse;
import com.starone.common.response.record.MovieCreditResponse;
import com.starone.common.response.record.MovieResponse;
import com.starone.common.response.record.PersonProfessionAddition;

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

    // ==================== CREATE TESTS ====================

    @Nested
    @DisplayName("create() method tests")
    class CreateTests {
        @Test
        void should_throwBadRequest_when_movie_dto_is_null() {
            // Arrange (Given)
            MovieRequestDto movieDto = null;
            // + Act
            BadRequestException ex = assertThrows(BadRequestException.class, () -> movieService.create(movieDto));

            // Assert
            assertEquals(ErrorCodes.BAD_REQUEST, ex.getErrorCode());
            assertEquals("Movie requestDto is null", ex.getMessage());

            verifyNoInteractions(movieMapper, movieRepository);

        }

        @Test
        void should_throwBadRequest_when_duplicate_person_as_credit() {
            // Arrange
            MovieRequestDto movieDto = MovieTestDataFactory.movieWithDuplicatePersonCredits();
            Exception ex = assertThrows(BadRequestException.class, () -> movieService.create(movieDto));
            assertEquals("Same person cannot be added multiple times as movie credit", ex.getMessage());

        }

        @Test
        void should_throwBadRequest_when_null_person_as_credit() {
            MovieRequestDto requestDto = MovieTestDataFactory.movieWithNullPersonCredits();
            Exception ex = assertThrows(BadRequestException.class, () -> movieService.create(requestDto));
            assertEquals("PersonId cannot be null in movie credits", ex.getMessage());

        }

        @Test
        void should_create_movie_when_credits_are_null() {
            // Arrange
            MovieRequestDto movieDto = MovieTestDataFactory.movieWithNullCredits();
            Movie movie = new Movie();
            movie.setMovieCredits(new ArrayList<>());

            Movie savedMovie = new Movie();
            savedMovie.setId(MovieTestDataFactory.MOVIE_ID);
            savedMovie.setMovieCredits(Collections.emptyList());

            MovieResponse movieResponse = MovieTestDataFactory.baseMovieResponse();

            when(movieMapper.toEntity(eq(movieDto))).thenReturn(movie);
            when(movieRepository.save(any())).thenReturn(savedMovie);
            when(movieMapper.toResponseDto(any())).thenReturn(movieResponse);

            // Act
            MovieResponse response = movieService.create(movieDto);

            // Assert
            assertNotNull(response);
            assertTrue(response.movieCredits().isEmpty());
            verify(personClient, never()).getAllPersonByIds(any());
            verify(personClient, never()).addProfessionsToPersons(any());

            verify(creditMapper, never()).toEntity(any());
            verify(movieRepository).save(argThat(m -> m.getMovieCredits().isEmpty()));
        }

        @Test
        void should_create_movie_when_credits_are_empty() {
            // Arrange
            MovieRequestDto movieDto = MovieTestDataFactory.movieWithEmptyCredits();
            Movie movie = new Movie();
            movie.setMovieCredits(Collections.emptyList());

            Movie savedMovie = new Movie();
            savedMovie.setId(MovieTestDataFactory.MOVIE_ID);
            savedMovie.setMovieCredits(Collections.emptyList());

            MovieResponse movieResponse = MovieTestDataFactory.baseMovieResponse();

            when(movieMapper.toEntity(eq(movieDto))).thenReturn(movie);
            when(movieRepository.save(any())).thenReturn(savedMovie);
            when(movieMapper.toResponseDto(any())).thenReturn(movieResponse);

            // Act
            MovieResponse response = movieService.create(movieDto);

            // Assert
            assertNotNull(response);
            assertTrue(response.movieCredits().isEmpty());

            verify(personClient, never()).getAllPersonByIds(anySet());
            verify(personClient, never()).addProfessionsToPersons(anyList());
            verify(creditMapper, never()).toEntity(any());
            verify(movieRepository).save(argThat(m -> m.getMovieCredits().isEmpty()));
            verify(movieRepository).save(same(movie));
        }

        @Test
        void should_create_movie_when_single_credit() {
            // Arrange
            MovieRequestDto movieDto = MovieTestDataFactory.movieWithOneCredit();

            Movie movie = new Movie();
            movie.setMovieCredits(new ArrayList<>());

            Movie savedMovie = MovieTestDataFactory.savedMovieWithOneCredit();

            MovieResponse movieResponse = MovieTestDataFactory.baseMovieResponse();

            when(movieMapper.toEntity(eq(movieDto))).thenReturn(movie);
            when(creditMapper.toEntity(any(MovieCreditRequestDto.class)))
                    .thenAnswer(invocation -> {
                        MovieCreditRequestDto req = invocation.getArgument(0);

                        MovieCredit credit = new MovieCredit();
                        credit.setPersonId(req.getPersonId());
                        credit.setBillingOrder(req.getBillingOrder());

                        return credit; // ← new instance each call
                    });
            when(movieRepository.save(any())).thenReturn(savedMovie);

            when(movieMapper.toResponseDto(any())).thenReturn(movieResponse);

            when(personClient.getAllPersonByIds(anySet()))
                    .thenReturn(List.of(MovieTestDataFactory.personWithAllRequestedProfessions()));
            // Act
            MovieResponse response = movieService.create(movieDto);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.movieCredits().size());

            MovieCreditResponse creditResponse = response.movieCredits().get(0);
            assertEquals(MovieTestDataFactory.PERSON_ID_1, creditResponse.personId());
            assertEquals(1, creditResponse.billingOrder());

            verify(creditMapper, times(1)).toEntity(any());

            ArgumentCaptor<Movie> captor = ArgumentCaptor.forClass(Movie.class);
            verify(movieRepository).save(captor.capture());

            Movie persisted = captor.getValue();
            assertEquals(1, persisted.getMovieCredits().size());
            assertSame(persisted, persisted.getMovieCredits().get(0).getMovie());
        }

        @Test
        void should_create_movie_when_multiple_persons_as_credits() {
            // Arrange
            MovieRequestDto requestDto = MovieTestDataFactory.movieWithTwoDiffPersonCredits();

            Movie movie = new Movie();
            movie.setMovieCredits(new ArrayList<>());

            Movie savedMovie = MovieTestDataFactory.savedMovieWithTwoCreditsDifferentPerson();

            MovieResponse movieResponse = MovieTestDataFactory.baseMovieResponse();
            when(movieMapper.toEntity(any())).thenReturn(movie);
            when(creditMapper.toEntity(any())).thenAnswer(invocation -> {
                MovieCreditRequestDto req = invocation.getArgument(0);
                MovieCredit credit = new MovieCredit();
                credit.setPersonId(req.getPersonId());
                return credit;
            });
            when(movieRepository.save(any())).thenReturn(savedMovie);
            when(movieMapper.toResponseDto(any())).thenReturn(movieResponse);
            when(personClient.getAllPersonByIds(anySet()))
                    .thenReturn(List.of(
                            MovieTestDataFactory.personMissingRequestedProfessions_1(),
                            MovieTestDataFactory.personMissingRequestedProfessions_2()));

            // Act
            MovieResponse response = movieService.create(requestDto);

            // Assert
            assertNotNull(response);
            assertEquals(2, response.movieCredits().size());

            Set<UUID> personIds = response.movieCredits().stream()
                    .map(MovieCreditResponse::personId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            assertTrue(personIds.contains(MovieTestDataFactory.PERSON_ID_1));
            assertTrue(personIds.contains(MovieTestDataFactory.PERSON_ID_2));

            ArgumentCaptor<List<PersonProfessionAddition>> captor = ArgumentCaptor.forClass(List.class);
            verify(personClient).addProfessionsToPersons(captor.capture());
            List<PersonProfessionAddition> additions = captor.getValue();
            assertEquals(2, additions.size());
            
            PersonProfessionAddition addition1 = additions.get(0);
            assertEquals(MovieTestDataFactory.PERSON_ID_1, addition1.personId());
            assertEquals(Set.of(Profession.DIRECTOR), addition1.professions());

            PersonProfessionAddition addition2 = additions.get(1);
            assertEquals(MovieTestDataFactory.PERSON_ID_2, addition2.personId());
            assertEquals(Set.of(Profession.ACTOR, Profession.PRODUCER), addition2.professions());
        }

        @Test
        void should_create_movie_and_sync_new_professions_for_multiple_persons() {
            // Arrange
            MovieRequestDto requestDto = MovieTestDataFactory.movieWithTwoDiffPersonCredits();

            Movie movie = new Movie();
            movie.setMovieCredits(new ArrayList<>());

            Movie savedMovie = MovieTestDataFactory.savedMovieWithTwoCreditsDifferentPerson();

            MovieResponse movieResponse = MovieTestDataFactory.baseMovieResponse();

            when(movieMapper.toEntity(any())).thenReturn(movie);

            when(creditMapper.toEntity(any())).thenAnswer(invocation -> {
                MovieCreditRequestDto req = invocation.getArgument(0);

                MovieCredit credit = new MovieCredit();
                credit.setPersonId(req.getPersonId());
                credit.setProfessions(req.getProfessions());
                return credit;
            });

            when(movieRepository.save(any())).thenReturn(savedMovie);
            when(movieMapper.toResponseDto(any())).thenReturn(movieResponse);
            when(personClient.getAllPersonByIds(anySet())).thenReturn(List.of(
                    MovieTestDataFactory.personMissingRequestedProfessions_1(),
                    MovieTestDataFactory.personMissingRequestedProfessions_2()));

            // Act
            MovieResponse response = movieService.create(requestDto);

            // Assert
            assertNotNull(response);

            ArgumentCaptor<List<PersonProfessionAddition>> captor = ArgumentCaptor.forClass(List.class);
            verify(personClient).addProfessionsToPersons(captor.capture());
            List<PersonProfessionAddition> additions = captor.getValue();
            assertEquals(2, additions.size());

            Set<Profession> person_1 = additions.get(0).professions();
            Set<Profession> person_2 = additions.get(1).professions();
            assertEquals(Set.of(Profession.DIRECTOR), person_1);
            assertEquals(Set.of(Profession.ACTOR, Profession.PRODUCER), person_2);
        }

        @Test
        void should_create_movie_and_no_new_professions_for_multiple_persons() {
            // Arrange
            MovieRequestDto requestDto = MovieTestDataFactory.movieWithTwoDiffPersonCredits();

            Movie movie = new Movie();
            movie.setMovieCredits(new ArrayList<>());

            Movie savedMovie = MovieTestDataFactory.savedMovieWithTwoCreditsDifferentPerson();

            MovieResponse movieResponse = MovieTestDataFactory.baseMovieResponse();

            when(movieMapper.toEntity(any())).thenReturn(movie);

            when(creditMapper.toEntity(any())).thenAnswer(invocation -> {
                MovieCreditRequestDto req = invocation.getArgument(0);

                MovieCredit credit = new MovieCredit();
                credit.setPersonId(req.getPersonId());
                credit.setProfessions(req.getProfessions());
                return credit;
            });

            when(movieRepository.save(any())).thenReturn(savedMovie);
            when(movieMapper.toResponseDto(any())).thenReturn(movieResponse);
            when(personClient.getAllPersonByIds(anySet())).thenReturn(List.of(
                    MovieTestDataFactory.personWithRequestedProfessions_1(),
                    MovieTestDataFactory.personWithRequestedProfessions_2()));

            // Act
            MovieResponse response = movieService.create(requestDto);

            // Assert
            assertNotNull(response);
            assertEquals(2, response.movieCredits().size());
            verify(movieRepository,times(1)).save(any());
            Set<UUID> personIds = response.movieCredits().stream()
                    .map(m -> m.personId())
                    .collect(Collectors.toSet());

            assertEquals(Set.of(
                    MovieTestDataFactory.PERSON_ID_1,
                    MovieTestDataFactory.PERSON_ID_2), personIds);
            Map<UUID, Set<Profession>> professionsById = response.movieCredits().stream()
                    .collect(Collectors.toMap(
                            m -> m.personId(), m -> m.professions()));

            assertEquals(MovieTestDataFactory.ACTOR_DIRECTOR, professionsById.get(MovieTestDataFactory.PERSON_ID_1));
            assertEquals(MovieTestDataFactory.ACTOR_PRODUCER, professionsById.get(MovieTestDataFactory.PERSON_ID_2));
            
            verify(personClient, never()).addProfessionsToPersons(anyList());

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

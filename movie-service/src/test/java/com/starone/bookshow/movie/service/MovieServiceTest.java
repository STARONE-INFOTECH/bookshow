package com.starone.bookshow.movie.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.starone.bookshow.movie.client.IPersonClient;
import com.starone.bookshow.movie.dto.MovieCreditRequestDto;
import com.starone.bookshow.movie.dto.MovieRequestDto;
import com.starone.bookshow.movie.entity.Movie;
import com.starone.bookshow.movie.entity.MovieCredit;
import com.starone.bookshow.movie.helper.MovieCreateTestDataFactory;
import com.starone.bookshow.movie.helper.MovieGetTestDataFactory;
import com.starone.bookshow.movie.helper.MovieUpdateTestDataFactory;
import com.starone.bookshow.movie.mapper.IMovieCreditMapper;
import com.starone.bookshow.movie.mapper.IMovieMapper;
import com.starone.bookshow.movie.repository.IMovieRepository;
import com.starone.bookshow.movie.service.impl.MovieServiceImpl;
import com.starone.common.enums.Language;
import com.starone.common.enums.Profession;
import com.starone.common.error.ErrorCodes;
import com.starone.common.exceptions.BadRequestException;
import com.starone.common.exceptions.NotFoundException;
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
        void should_throw_bad_request_when_movie_request_is_null() {
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
        void should_throw_bad_request_when_duplicate_person_in_credits() {
            // Arrange
            MovieRequestDto movieDto = MovieCreateTestDataFactory.movieWithDuplicatePersonCredits();

            // Act + Assert
            Exception ex = assertThrows(BadRequestException.class, () -> movieService.create(movieDto));
            assertEquals("Same person cannot be added multiple times as movie credit", ex.getMessage());
            verifyNoInteractions(movieMapper, movieRepository, creditMapper, personClient);
        }

        @Test
        void should_throw_bad_request_when_credit_person_id_is_null() {
            // Arrange
            MovieRequestDto requestDto = MovieCreateTestDataFactory.movieWithNullPersonCredits();
            // Act +Assert
            Exception ex = assertThrows(BadRequestException.class, () -> movieService.create(requestDto));
            assertEquals("PersonId cannot be null in movie credits", ex.getMessage());
            verifyNoInteractions(movieMapper, movieRepository, creditMapper, personClient);
        }

        @Test
        void should_create_movie_when_credits_are_null() {
            // Arrange
            MovieRequestDto movieDto = MovieCreateTestDataFactory.movieWithNullCredits();
            Movie movie = new Movie();
            movie.setMovieCredits(new ArrayList<>());

            Movie savedMovie = new Movie();
            savedMovie.setId(MovieCreateTestDataFactory.MOVIE_ID);

            MovieResponse movieResponse = MovieCreateTestDataFactory.baseMovieResponse(true);

            when(movieMapper.toEntity(eq(movieDto))).thenReturn(movie);
            when(movieRepository.save(any())).thenReturn(savedMovie);
            when(movieMapper.toResponseDto(any())).thenReturn(movieResponse);

            // Act
            MovieResponse response = movieService.create(movieDto);

            // Assert
            assertNotNull(response);
            assertTrue(response.movieCredits().isEmpty());
            verify(movieRepository).save(argThat(m -> m.getMovieCredits().isEmpty()));
        }

        @Test
        void should_create_movie_with_empty_credits() {
            // Arrange
            MovieRequestDto movieDto = MovieCreateTestDataFactory.movieWithEmptyCredits();
            Movie movie = new Movie();
            movie.setMovieCredits(Collections.emptyList());

            Movie savedMovie = new Movie();
            savedMovie.setId(MovieCreateTestDataFactory.MOVIE_ID);
            savedMovie.setMovieCredits(Collections.emptyList());

            MovieResponse movieResponse = MovieCreateTestDataFactory.baseMovieResponse(true);

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
        }

        @Test
        void should_create_movie_with_single_credit() {
            // Arrange
            MovieRequestDto movieDto = MovieCreateTestDataFactory.movieWithOneCredit();

            Movie movie = new Movie();
            movie.setMovieCredits(new ArrayList<>());

            Movie savedMovie = MovieCreateTestDataFactory.savedMovieWithOneCredit();

            MovieResponse movieResponse = MovieCreateTestDataFactory.baseMovieResponse(true);

            when(movieMapper.toEntity(eq(movieDto))).thenReturn(movie);
            when(creditMapper.toEntity(any(MovieCreditRequestDto.class)))
                    .thenAnswer(invocation -> {
                        MovieCreditRequestDto req = invocation.getArgument(0);

                        MovieCredit credit = new MovieCredit();
                        credit.setPersonId(req.getPersonId());

                        return credit; // ← new instance each call
                    });
            when(movieRepository.save(any())).thenReturn(savedMovie);

            when(movieMapper.toResponseDto(any())).thenReturn(movieResponse);

            when(personClient.getAllPersonByIds(anySet()))
                    .thenReturn(List.of(MovieCreateTestDataFactory.personWithAllRequestedProfessions()));
            // Act
            MovieResponse response = movieService.create(movieDto);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.movieCredits().size());

            MovieCreditResponse creditResponse = response.movieCredits().get(0);
            assertEquals(MovieCreateTestDataFactory.PERSON_ID_1, creditResponse.personId());
            assertEquals(1, creditResponse.billingOrder());

            ArgumentCaptor<Movie> captor = ArgumentCaptor.forClass(Movie.class);
            verify(movieRepository).save(captor.capture());

            Movie persisted = captor.getValue();
            assertEquals(1, persisted.getMovieCredits().size());
            assertSame(persisted, persisted.getMovieCredits().get(0).getMovie());
        }

        @Test
        void should_sync_new_professions_when_creating_movie_with_multiple_credits() {
            // Arrange
            MovieRequestDto requestDto = MovieCreateTestDataFactory.movieWithTwoDiffPersonCredits();

            Movie movie = new Movie();
            movie.setMovieCredits(new ArrayList<>());

            Movie savedMovie = MovieCreateTestDataFactory.savedMovieWithTwoCreditsDifferentPerson();

            MovieResponse movieResponse = MovieCreateTestDataFactory.baseMovieResponse(true);

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
                    MovieCreateTestDataFactory.personMissingRequestedProfessions_1(),
                    MovieCreateTestDataFactory.personMissingRequestedProfessions_2()));

            // Act
            MovieResponse response = movieService.create(requestDto);

            // Assert
            assertNotNull(response);

            ArgumentCaptor<List<PersonProfessionAddition>> captor = ArgumentCaptor.forClass(List.class);
            verify(personClient).addProfessionsToPersons(captor.capture());
            List<PersonProfessionAddition> additions = captor.getValue();
            assertEquals(2, additions.size());

            Map<UUID, Set<Profession>> byPerson = additions.stream().collect(Collectors.toMap(
                    PersonProfessionAddition::personId,
                    PersonProfessionAddition::professions));

            assertEquals(Set.of(Profession.DIRECTOR), byPerson.get(MovieCreateTestDataFactory.PERSON_ID_1));
            assertEquals(Set.of(Profession.ACTOR, Profession.PRODUCER),
                    byPerson.get(MovieCreateTestDataFactory.PERSON_ID_2));
        }

        @Test
        void should_not_sync_professions_when_creating_movie_with_existing_professions() {
            // Arrange
            MovieRequestDto requestDto = MovieCreateTestDataFactory.movieWithTwoDiffPersonCredits();

            Movie movie = new Movie();
            movie.setMovieCredits(new ArrayList<>());

            Movie savedMovie = MovieCreateTestDataFactory.savedMovieWithTwoCreditsDifferentPerson();

            MovieResponse movieResponse = MovieCreateTestDataFactory.baseMovieResponse(true);

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
                    MovieCreateTestDataFactory.personWithRequestedProfessions_1(),
                    MovieCreateTestDataFactory.personWithRequestedProfessions_2()));

            // Act
            MovieResponse response = movieService.create(requestDto);

            // Assert
            assertNotNull(response);
            assertEquals(2, response.movieCredits().size());
            Set<UUID> personIds = response.movieCredits().stream()
                    .map(m -> m.personId())
                    .collect(Collectors.toSet());

            assertEquals(Set.of(
                    MovieCreateTestDataFactory.PERSON_ID_1,
                    MovieCreateTestDataFactory.PERSON_ID_2), personIds);

            verify(personClient, never()).addProfessionsToPersons(anyList());

        }

    }

    // ==================== READ / GET TESTS ====================
    @Nested
    @DisplayName("getById() and retrieval tests")
    class RetrievalTests {

        @Test
        void should_return_movie_when_id_exists() {
            // Arrange

            UUID movieId = MovieGetTestDataFactory.MOVIE_ID;

            Movie movie = MovieGetTestDataFactory.savedMovieWithTwoCreditsDifferentPerson();

            when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));

            when(personClient.getAllPersonByIds(anySet())).thenReturn(
                    List.of(
                            MovieGetTestDataFactory.personWithRequestedProfessions_1(),
                            MovieGetTestDataFactory.personWithRequestedProfessions_2()));

            when(movieMapper.toResponseDto(any())).thenReturn(MovieGetTestDataFactory.baseMovieResponse(true));
            // Act
            MovieResponse response = movieService.getById(movieId);

            // Assert
            assertNotNull(response);
            assertNotNull(response.movieCredits());
            assertEquals(2, response.movieCredits().size());

            Map<UUID, MovieCreditResponse> creditByPerson = response.movieCredits().stream()
                    .collect(Collectors.toMap(
                            MovieCreditResponse::personId,
                            Function.identity()));

            assertEquals("Leonardo DiCaprio", creditByPerson.get(MovieGetTestDataFactory.PERSON_ID_1).personName());

            verify(movieRepository).findById(movieId);
            verify(personClient).getAllPersonByIds(Set.of(
                    MovieGetTestDataFactory.PERSON_ID_1,
                    MovieGetTestDataFactory.PERSON_ID_2));

        }
        /*
         * @Test
         * void shouldThrowNotFound_whenIdNotExists() { ... }
         */
    }

    // ==================== UPDATE TESTS ====================
    @Nested
    @DisplayName("update() method tests")
    class UpdateTests {

        @Test
        void should_sync_new_professions_when_updating_movie_with_multiple_credits() {
            // Arrange
            UUID movieId = MovieUpdateTestDataFactory.MOVIE_ID;

            MovieRequestDto movieRequestDto = MovieUpdateTestDataFactory.updateRequest_withNewProfessions();

            Movie existingMovie = MovieUpdateTestDataFactory.existingMovie_beforeUpdate();

            Movie updatedMovie = MovieUpdateTestDataFactory.updatedMovie_afterUpdate();

            MovieResponse movieResponse = MovieUpdateTestDataFactory.updatedMovieResponse();

            when(movieRepository.findById(movieId)).thenReturn(Optional.of(existingMovie));

            doAnswer(invocation -> {
                MovieRequestDto dto = invocation.getArgument(0);
                Movie movie = invocation.getArgument(1);

                List<MovieCredit> credits = movie.getMovieCredits();
                if (credits == null) {
                    credits = new ArrayList<>();
                    movie.setMovieCredits(credits);
                } else {
                    credits.clear();
                }

                for (MovieCreditRequestDto creditDto : dto.getMovieCredits()) {
                    MovieCredit credit = new MovieCredit();
                    credit.setPersonId(creditDto.getPersonId());
                    credit.setProfessions(creditDto.getProfessions());
                    credit.setBillingOrder(creditDto.getBillingOrder());
                    credit.setMovie(movie);
                    movie.getMovieCredits().add(credit);
                }
                return null;
            }).when(movieMapper).updateEntity(any(), any());

            when(movieRepository.save(existingMovie))
                    .thenReturn(updatedMovie);

            when(personClient.getAllPersonByIds(anySet()))
                    .thenReturn(MovieUpdateTestDataFactory.persons_beforeUpdate());

            when(movieMapper.toResponseDto(updatedMovie)).thenReturn(movieResponse);

            // Act
            MovieResponse response = movieService.update(movieId, movieRequestDto);

            // Assert — basic response
            assertNotNull(response);
            assertEquals("Updated synopsis", response.synopsis());
            assertEquals(
                    List.of(Language.ENGLISH, Language.HINDI),
                    response.languages());

            // Verify update flow
            verify(movieRepository).findById(movieId);
            verify(movieMapper).updateEntity(movieRequestDto, existingMovie);
            verify(movieRepository).save(existingMovie);

            // Verify correct person lookup
            verify(personClient).getAllPersonByIds(Set.of(
                    MovieUpdateTestDataFactory.PERSON_ID_1,
                    MovieUpdateTestDataFactory.PERSON_ID_2));

            // Capture profession sync
            ArgumentCaptor<List<PersonProfessionAddition>> captor = ArgumentCaptor.forClass(List.class);

            verify(personClient).addProfessionsToPersons(captor.capture());

            List<PersonProfessionAddition> additions = captor.getValue();
            assertEquals(2, additions.size());

            // Order-independent verification
            Map<UUID, Set<Profession>> byPerson = additions.stream()
                    .collect(Collectors.toMap(
                            PersonProfessionAddition::personId,
                            PersonProfessionAddition::professions));

            assertEquals(
                    Set.of(Profession.DIRECTOR),
                    byPerson.get(MovieUpdateTestDataFactory.PERSON_ID_1));

            assertEquals(
                    Set.of(Profession.PRODUCER),
                    byPerson.get(MovieUpdateTestDataFactory.PERSON_ID_2));
            assertTrue(
                    additions.stream().anyMatch(a -> !a.professions().isEmpty()));
        }
    }

    // ==================== DEACTIVATE TESTS ====================
    @Nested
    @DisplayName("deactivate() method tests")
    class DeactivateTests {

        @Test
        void should_deactivate_movie_when_movie_is_active() {
            // Arrange
            UUID movieId = MovieGetTestDataFactory.MOVIE_ID;
            Movie existingMovie = new Movie();
            existingMovie.setId(movieId);
            existingMovie.setActive(true);

            Movie savedMovie = new Movie();
            savedMovie.setId(movieId);
            savedMovie.setActive(false);

            MovieResponse movieResponse = MovieGetTestDataFactory.baseMovieResponse(false);
            when(movieRepository.findById(movieId)).thenReturn(Optional.of(existingMovie));
            when(movieRepository.save(any())).thenReturn(savedMovie);
            when(movieMapper.toResponseDto(savedMovie)).thenReturn(movieResponse);

            // Act
            MovieResponse response = movieService.deactivate(movieId);

            // Assert
            assertNotNull(response);
            assertFalse(response.active());
            verify(movieRepository).findById(movieId);
            verify(movieRepository).save(existingMovie);
        }

        @Test
        void should_not_change_state_when_movie_is_already_inactive() {
            // Arrange
            UUID movieId = MovieGetTestDataFactory.MOVIE_ID;
            Movie existingMovie = new Movie();
            existingMovie.setId(movieId);
            existingMovie.setActive(false);

            MovieResponse movieResponse = MovieGetTestDataFactory.baseMovieResponse(false);

            when(movieRepository.findById(movieId)).thenReturn(Optional.of(existingMovie));
            when(movieMapper.toResponseDto(any())).thenReturn(movieResponse);

            // Act
            MovieResponse response = movieService.deactivate(movieId);

            // Assert
            assertNotNull(response);
            assertFalse(response.active());

            verify(movieRepository).findById(movieId);
            verify(movieRepository, never()).save(any());
        }

        @Test
        void should_throw_not_found_when_movie_does_not_exist() {
            // Arrange
            UUID movieId = MovieGetTestDataFactory.MOVIE_ID;
            when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(NotFoundException.class, () -> movieService.deactivate(movieId));
            verify(movieRepository).findById(movieId);
            verify(movieRepository, never()).save(any());
            verifyNoInteractions(movieMapper);
        }
    }

    // ==================== ACTIVATE TESTS ====================
    @Nested
    @DisplayName("activate() method tests")
    class ActivateTests {

        @Test
        void should_activate_movie_when_movie_is_inactive() {
            // Arrange
            UUID movieId = MovieGetTestDataFactory.MOVIE_ID;
            Movie existingMovie = new Movie();
            existingMovie.setId(movieId);
            existingMovie.setActive(false);

            Movie savedMovie = new Movie();
            savedMovie.setId(movieId);
            savedMovie.setActive(true);

            MovieResponse movieResponse = MovieGetTestDataFactory.baseMovieResponse(true);

            when(movieRepository.findById(movieId)).thenReturn(Optional.of(existingMovie));
            when(movieRepository.save(any())).thenReturn(savedMovie);
            when(movieMapper.toResponseDto(savedMovie)).thenReturn(movieResponse);
            // Act
            MovieResponse response = movieService.activate(movieId);

            // Assert
            assertNotNull(response);
            assertTrue(response.active());
            verify(movieRepository).findById(movieId);
            verify(movieRepository).save(existingMovie);
        }

        @Test
        void should_not_change_state_when_movie_is_already_active() {
            // Arrange
            UUID movieId = MovieGetTestDataFactory.MOVIE_ID;
            Movie existingMovie = new Movie();
            existingMovie.setId(movieId);
            existingMovie.setActive(true);

            MovieResponse movieResponse = MovieGetTestDataFactory.baseMovieResponse(true);

            when(movieRepository.findById(movieId)).thenReturn(Optional.of(existingMovie));
            when(movieMapper.toResponseDto(any())).thenReturn(movieResponse);
            // Act
            MovieResponse response = movieService.activate(movieId);

            // Assert
            assertNotNull(response);
            assertTrue(response.active());
            verify(movieRepository).findById(movieId);
            verify(movieRepository, never()).save(any());
        }

        @Test
        void should_throw_not_found_when_movie_does_not_exist() {
            // Arrange
            UUID movieId = MovieGetTestDataFactory.MOVIE_ID;

            when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> movieService.activate(movieId));

            // Act + Assert
            verify(movieRepository).findById(movieId);
            verify(movieRepository, never()).save(any());
            verifyNoInteractions(movieMapper);
        }
    }

    // ==================== QUERY / PAGINATION TESTS ====================
    @Nested
    @DisplayName("Pagination and filtering tests")
    class QueryTests {

        @Test
        void should_return_now_showing_movies() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            Movie movie1 = MovieGetTestDataFactory.activeReleasedMovie();
            Movie movie2 = MovieGetTestDataFactory.activeReleasedMovie();

            Page<Movie> moviePage = new PageImpl<>(
                    List.of(movie1, movie2), pageable, 2);

            MovieResponse response1 = MovieCreateTestDataFactory.baseMovieResponse(true);
            MovieResponse response2 = MovieCreateTestDataFactory.baseMovieResponse(true);

            when(movieRepository.findByActiveTrueAndReleaseDateLessThanEqual(any(LocalDate.class), eq(pageable)))
                    .thenReturn(moviePage);
            when(movieMapper.toResponseDto(movie1)).thenReturn(response1);
            when(movieMapper.toResponseDto(movie2)).thenReturn(response2);

            // Act
            Page<MovieResponse> result = movieService.getNowShowing(pageable);
            
            // Assert
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertEquals(2, result.getContent().size());

            assertTrue(result.getContent().stream().allMatch(MovieResponse::active));

            verify(movieRepository).findByActiveTrueAndReleaseDateLessThanEqual(any(LocalDate.class), eq(pageable));
            verify(movieMapper, times(2)).toResponseDto(any(Movie.class));
        }

        /*
         * @Test
         * void shouldReturnUpcomingMovies() { ... }
         * 
         * @Test
         * void shouldFilterByGenre() { ... }
         */
        // ... getAll, search, filter tests
    }

}

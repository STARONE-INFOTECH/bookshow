package com.starone.bookshow.movie.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starone.bookshow.movie.client.PersonClient;
import com.starone.bookshow.movie.dto.MovieCreditRequestDto;
import com.starone.bookshow.movie.entity.Movie;
import com.starone.bookshow.movie.entity.MovieCredit;
import com.starone.bookshow.movie.mapper.IMovieCreditMapper;
import com.starone.bookshow.movie.repository.IMovieCreditRepository;
import com.starone.bookshow.movie.repository.IMovieRepository;
import com.starone.bookshow.movie.service.IMovieCreditService;
import com.starone.common.dto.ApiResponse;
import com.starone.common.dto.MovieCreditResponseDto;
import com.starone.common.dto.PersonResponseDto;
import com.starone.common.enums.Profession;
import com.starone.common.enums.Status;
import com.starone.common.error.ErrorCodes;
import com.starone.common.exceptions.BadRequestException;
import com.starone.common.exceptions.ConflictException;
import com.starone.common.exceptions.NotFoundException;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovieCreditServiceImpl implements IMovieCreditService {

        private static final Logger log = LoggerFactory.getLogger(MovieCreditServiceImpl.class);

        private final IMovieCreditRepository movieCreditRepository;
        private final IMovieRepository movieRepository;
        private final IMovieCreditMapper movieCreditMapper;
        private final PersonClient personClient;

        @Override
        public MovieCreditResponseDto addCredit(UUID movieId, MovieCreditRequestDto requestDto) {
                Objects.requireNonNull(movieId, "Movie Id is required");

                log.info("Adding credit for movie ID: {} - Person ID: {}, Role: {}",
                                movieId, requestDto.getPersonId(), requestDto.getProfessions());

                // Fetch movie
                Movie movie = movieRepository.findById(movieId)
                                .orElseThrow(() -> {
                                        log.warn("Movie not found when adding credit - Movie ID: {}", movieId);
                                        return new NotFoundException(ErrorCodes.MOVIE_NOT_FOUND);
                                });
                // Check for duplicate credit (same person + same role in same movie)
                if (movieCreditRepository.existsByMovieIdAndPersonIdAndMovieCharacters(
                                movieId, requestDto.getPersonId(), requestDto.getProfessions())) {
                        log.warn("Duplicate credit detected - Person ID: {} already has role '{}' in Movie ID: {}",
                                        requestDto.getPersonId(), requestDto.getProfessions(), movieId);
                        throw new ConflictException(ErrorCodes.MOVIE_CREDIT_ALREADY_EXISTS,
                                        "This person already has this role in the movie");
                }

                log.debug("Validating person existence via person-service - Person ID: {}", requestDto.getPersonId());
                personClient.getPersonById(requestDto.getPersonId()); // validate person exists

                // Map and persist
                MovieCredit credit = movieCreditMapper.toEntity(requestDto); // ← BaseMapper method
                credit.setMovie(movie); // manually set parent

                // INFO: Successful creation
                credit = movieCreditRepository.save(credit);

                log.info("Credit added successfully - Credit ID: {}, Movie ID: {}, Person ID: {}, Role: {}",
                                credit.getId(), movieId, requestDto.getPersonId(), requestDto.getProfessions());

                return enrichAndMapResponse(credit);
        }

        @Override
        public MovieCreditResponseDto updateCredit(UUID creditId, MovieCreditRequestDto requestDto) {
                Objects.requireNonNull(creditId, "Credit Id is required.");
                log.info("Updating movie credit with ID: {}", creditId);

                MovieCredit credit = movieCreditRepository.findById(creditId)
                                .orElseThrow(() -> {
                                        log.warn("Credit not found for update - Credit ID: {}", creditId);
                                        return new NotFoundException(
                                                        ErrorCodes.MOVIE_CREDIT_NOT_FOUND, "Credit not found");
                                });

                UUID movieId = credit.getMovie().getId();
                UUID oldPersonId = credit.getPersonId();
                Set<Profession> oldProfessions = credit.getProfessions();

                // Duplicate check if role or person changing
                if (requestDto.getProfessions() != null && requestDto.getPersonId() != null &&
                                (!requestDto.getProfessions().equals(oldProfessions) ||
                                                !requestDto.getPersonId().equals(oldPersonId))) {

                        if (movieCreditRepository.existsByMovieIdAndPersonIdAndMovieCharacters(
                                        movieId, requestDto.getPersonId(), requestDto.getProfessions())) {
                                // WARN: Business rule violation on update
                                log.warn(
                                                "Duplicate credit detected during update - Person ID: {} with role '{}' already exists in Movie ID: {}",
                                                requestDto.getPersonId(), requestDto.getProfessions(), movieId);
                                throw new ConflictException(ErrorCodes.MOVIE_CREDIT_ALREADY_EXISTS,
                                                "Duplicate role for this person in movie");
                        }
                        // INFO: Significant change in credit assignment
                        log.info(
                                        "Changing credit assignment - Movie ID: {}, Old: (Person: {}, Role: {}), New: (Person: {}, Role: {})",
                                        movieId, oldPersonId, oldProfessions, requestDto.getPersonId(),
                                        requestDto.getProfessions());
                }

                // Validate new person exists if personId is being changed
                if (requestDto.getPersonId() != null && !requestDto.getPersonId().equals(oldPersonId)) {
                        log.debug("Validating new person existence via person-service - Person ID: {}",
                                        requestDto.getPersonId());
                        personClient.getPersonById(requestDto.getPersonId()); // throws if not found
                }

                // Apply partial updates
                movieCreditMapper.updateEntity(requestDto, credit);

                credit = movieCreditRepository.save(credit);

                // INFO: Successful update — key audit event
                log.info("Movie credit updated successfully - Credit ID: {}, Movie ID: {}, Person ID: {}, Role: {}",
                                credit.getId(), movieId, credit.getPersonId(), credit.getProfessions());

                return enrichAndMapResponse(credit);
        }

        @Override
        public void removeCredit(UUID creditId) {
                // INFO: Start of a destructive operation — critical for audit
                log.info("Removing movie credit with ID: {}", creditId);

                // Check existence before delete
                if (!movieCreditRepository.existsById(creditId)) {
                        // WARN: Client tried to delete non-existent credit
                        log.warn("Cannot remove credit - not found with ID: {}", creditId);
                        throw new NotFoundException(ErrorCodes.MOVIE_CREDIT_NOT_FOUND, "Credit not found");
                }

                // Perform deletion
                movieCreditRepository.deleteById(creditId);

                // INFO: Successful removal — essential audit event (who/when/what was deleted)
                log.info("Movie credit removed successfully - Credit ID: {}", creditId);
        }

        @Override
        @Transactional(readOnly = true)
        public List<MovieCreditResponseDto> getCreditsByMovieId(UUID movieId) {
                // INFO: Start of a read operation that retrieves all credits for a movie
                log.info("Fetching all credits for movie ID: {}", movieId);

                List<MovieCredit> credits = movieCreditRepository.findByMovieId(movieId);
                // INFO: Result summary — very useful for monitoring API usage and data volume
                log.info("Retrieved {} credits for movie ID: {}", credits.size(), movieId);

                // Optional DEBUG: Show billing order sorting in action (helpful when debugging
                // display order)
                if (log.isDebugEnabled() && !credits.isEmpty()) {
                        List<String> roles = credits.stream()
                                        .limit(10) // limit to avoid huge logs on big casts
                                        .map(c -> String.format("%s (order: %s)",
                                                        c.getProfessions(),
                                                        c.getBillingOrder() == null ? "none" : c.getBillingOrder()))
                                        .toList();
                        log.debug("Credits before sorting (sample): {}", roles);
                }

                // Sort by billingOrder (nulls last), then enrich and map
                List<MovieCreditResponseDto> response = credits.stream()
                                .sorted(Comparator
                                                .comparingInt(c -> c.getBillingOrder() == null ? Integer.MAX_VALUE
                                                                : c.getBillingOrder()))
                                .map(this::enrichAndMapResponse)
                                .toList();

                // INFO: Final result count after processing
                log.info("Returning {} enriched credits for movie ID: {}", response.size(), movieId);

                return response;
        }

        @Override
        @Transactional(readOnly = true)
        public Page<MovieCreditResponseDto> getCreditsByMovieIdPaginated(UUID movieId, Pageable pageable) {
                // INFO: Start of paginated read operation
                log.info("Fetching paginated credits for movie ID: {} (page: {}, size: {}, sort: {})",
                                movieId,
                                pageable.getPageNumber(),
                                pageable.getPageSize(),
                                pageable.getSort());

                Page<MovieCredit> page = movieCreditRepository.findByMovieId(movieId, pageable);

                // INFO: Result summary from database — very useful for performance monitoring
                log.info("Retrieved {} credits for movie ID: {} (page: {} of {}, total: {})",
                                page.getNumberOfElements(),
                                movieId,
                                page.getNumber() + 1, // human-readable page number
                                page.getTotalPages(),
                                page.getTotalElements());

                // Optional DEBUG: Sample of raw credits (useful when debugging
                // pagination/sorting/billing order)
                if (log.isDebugEnabled() && !page.isEmpty()) {
                        List<String> sample = page.getContent().stream()
                                        .limit(10)
                                        .map(c -> String.format("%s - %s (order: %s)",
                                                        c.getPersonId(),
                                                        c.getProfessions(),
                                                        c.getBillingOrder() == null ? "none" : c.getBillingOrder()))
                                        .toList();
                        log.debug("Raw credits on this page (sample): {}", sample);
                }

                // Map and enrich
                Page<MovieCreditResponseDto> responsePage = page.map(this::enrichAndMapResponse);

                // INFO: Final result after enrichment
                log.info("Returning {} enriched credits for movie ID: {} (page: {} of {}, total: {})",
                                responsePage.getNumberOfElements(),
                                movieId,
                                responsePage.getNumber() + 1,
                                responsePage.getTotalPages(),
                                responsePage.getTotalElements());

                return responsePage;
        }

        @Override
        @Transactional(readOnly = true)
        public MovieCreditResponseDto getCreditById(UUID creditId) {
                // INFO: Start of a single-record read operation
                log.info("Fetching movie credit by ID: {}", creditId);

                MovieCredit credit = movieCreditRepository.findById(creditId)
                                .orElseThrow(() -> {
                                        // WARN: Client requested a non-existent credit
                                        log.warn("Movie credit not found - Credit ID: {}", creditId);
                                        return new NotFoundException(ErrorCodes.MOVIE_CREDIT_NOT_FOUND);
                                });

                // INFO: Successful retrieval — useful for audit and usage monitoring
                log.info("Movie credit retrieved successfully - Credit ID: {}, Movie ID: {}, Person ID: {}, Role: {}",
                                credit.getId(),
                                credit.getMovie().getId(),
                                credit.getPersonId(),
                                credit.getProfessions());

                // Optional DEBUG: Extra details (helpful when debugging enrichment or specific
                // credit issues)
                if (log.isDebugEnabled()) {
                        log.debug("Retrieved credit details - Billing Order: {}, Character: {}",
                                        credit.getBillingOrder() == null ? "none" : credit.getBillingOrder(),
                                        credit.getMovieCharacters() == null ? "N/A" : credit.getMovieCharacters());
                }

                return enrichAndMapResponse(credit);
        }

        @Override
        public List<MovieCreditResponseDto> reorderCredits(UUID movieId, List<MovieCreditRequestDto> orderedDtos) {
                // Validate input early
                if (orderedDtos == null || orderedDtos.isEmpty()) {
                        log.warn("Reorder credits requested with null or empty list for movie ID: {}", movieId);
                        throw new BadRequestException(ErrorCodes.VALIDATION_ERROR, "Reorder list cannot be empty");
                }

                // INFO: Start of a critical ordering operation — highly auditable
                log.info("Reordering credits for movie ID: {} - received {} items to reorder",
                                movieId, orderedDtos.size());

                // Fetch current credits
                List<MovieCredit> credits = movieCreditRepository.findByMovieId(movieId);

                if (credits.isEmpty()) {
                        log.warn("No existing credits found for movie ID: {} during reorder attempt", movieId);
                        throw new BadRequestException(ErrorCodes.MOVIE_CREDIT_NOT_FOUND, "No credits exist to reorder");
                }

                // INFO: Log current vs requested count for validation
                log.info("Current credits count: {}, requested reorder count: {}", credits.size(), orderedDtos.size());

                // Validate that requested list matches existing credits (same size and valid
                // entries)
                if (orderedDtos.size() != credits.size()) {
                        log.warn("Reorder list size mismatch for movie ID: {} - expected {}, got {}",
                                        movieId, credits.size(), orderedDtos.size());
                        throw new BadRequestException(ErrorCodes.VALIDATION_ERROR,
                                        "Reorder list must contain all existing credits with the same size");
                }

                // Apply new billing order
                for (int i = 0; i < orderedDtos.size(); i++) {
                        MovieCreditRequestDto dto = orderedDtos.get(i);
                        MovieCredit credit = credits.stream()
                                        .filter(c -> c.getPersonId().equals(dto.getPersonId()) &&
                                                        c.getProfessions().equals(dto.getProfessions()))
                                        .findFirst()
                                        .orElseThrow(() -> {
                                                log.warn("Invalid credit in reorder list for movie ID: {} - Person ID: {}, Role: {}",
                                                                movieId, dto.getPersonId(), dto.getProfessions());
                                                return new BadRequestException(ErrorCodes.VALIDATION_ERROR,
                                                                "Invalid credit in reorder list: no matching person and role");
                                        });

                        int oldOrder = credit.getBillingOrder() == null ? -1 : credit.getBillingOrder();
                        int newOrder = i + 1;

                        if (oldOrder != newOrder) {
                                log.info("Updating billing order for credit - Movie ID: {}, Person ID: {}, Role: {} | {} → {}",
                                                movieId, credit.getPersonId(), credit.getProfessions(), oldOrder,
                                                newOrder);
                        }

                        credit.setBillingOrder(newOrder);
                }

                // Persist changes
                movieCreditRepository.saveAll(credits);

                // INFO: Successful reorder — key audit event
                log.info("Credits reordered successfully for movie ID: {} - {} items updated", movieId, credits.size());

                // Return fresh ordered list
                return getCreditsByMovieId(movieId);
        }

        @Override
        public boolean existsCredit(UUID movieId, UUID personId, Set<Profession> roles) {
                // Basic input validation
                if (movieId == null || personId == null || roles == null) {
                        log.warn("existsCredit called with null parameter - Movie ID: {}, Person ID: {}, Role: {}",
                                        movieId, personId, roles);
                        return false;
                }

                // DEBUG: Log the uniqueness check (very useful when debugging conflicts)
                log.debug("Checking if credit exists - Movie ID: {}, Person ID: {}, Role: {}",
                                movieId, personId, roles);

                boolean exists = movieCreditRepository.existsByMovieIdAndPersonIdAndMovieCharacters(movieId, personId,
                                roles);

                // DEBUG: Result of the check
                log.debug("Credit existence check result - Movie ID: {}, Person ID: {}, Role: {} → {}",
                                movieId, personId, roles, exists);

                return exists;
        }

        // Helper: enrich basic mapped DTO with person details
        private MovieCreditResponseDto enrichAndMapResponse(MovieCredit credit) {
                // DEBUG: Start of enrichment (external call)
                log.debug("Enriching movie credit - Credit ID: {}, Person ID: {}",
                                credit.getId(), credit.getPersonId());

                MovieCreditResponseDto creditDto = movieCreditMapper.toResponseDto(credit);

                try {
                        ApiResponse<PersonResponseDto> personResponse = personClient
                                        .getPersonById(credit.getPersonId());

                        if (personResponse.getStatus() == Status.SUCCESS && personResponse.getData() != null) {
                                PersonResponseDto person = personResponse.getData();

                                creditDto.setPersonName(person.getName());
                                creditDto.setNickName(person.getNickName());
                                creditDto.setProfileImg(person.getProfileImg());

                                // DEBUG: Success
                                log.debug("Successfully enriched credit - Credit ID: {}, Person Name: {}",
                                                credit.getId(), person.getName());
                        } else {
                                // Business-level error (e.g., person not found, validation failed, etc.)
                                String reason = personResponse.getMessage() != null
                                                ? personResponse.getMessage()
                                                : "Unknown Reason";

                                log.debug("Person not available for ID: {} | ErrorCode: {}, Message: {}",
                                                credit.getPersonId(),
                                                personResponse.getErrorCode(),
                                                reason);
                                creditDto.setPersonName("Unknown Person");
                                creditDto.setProfileImg(null);
                        }

                } catch (FeignException e) {
                        // Only reaches here on network-level issues:
                        // - Connection refused
                        // - Timeout
                        // - DNS failure
                        // - Person service down
                        log.warn("Failed to reach Person service for ID: {} | Status: {} | Message: {}",
                                        credit.getPersonId(), e.status(), e.getMessage());

                        creditDto.setPersonName("Unavailable");
                        creditDto.setProfileImg(null);

                }
                // Always set fields from MovieCredit (even if person failed)
                creditDto.setProfessions(credit.getProfessions());
                creditDto.setCharacterNames(credit.getMovieCharacters());
                creditDto.setBillingOrder(credit.getBillingOrder());

                return creditDto;
        }
}

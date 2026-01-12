package com.starone.bookshow.movie.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starone.bookshow.movie.client.IPersonClient;
import com.starone.bookshow.movie.dto.MovieCreditRequestDto;
import com.starone.bookshow.movie.entity.MovieCredit;
import com.starone.bookshow.movie.mapper.IMovieCreditMapper;
import com.starone.bookshow.movie.repository.IMovieCreditRepository;
import com.starone.bookshow.movie.repository.IMovieRepository;
import com.starone.bookshow.movie.service.IMovieCreditService;
import com.starone.common.enums.Profession;
import com.starone.common.error.ErrorCodes;
import com.starone.common.exceptions.BadRequestException;
import com.starone.common.exceptions.NotFoundException;
import com.starone.common.response.record.MovieCreditPersonResponse;
import com.starone.common.response.record.MovieCreditResponse;

import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovieCreditServiceImpl implements IMovieCreditService {

        private static final Logger log = LoggerFactory.getLogger(MovieCreditServiceImpl.class);

        private final IMovieCreditRepository movieCreditRepository;
        private final IMovieRepository movieRepository;
        private final IMovieCreditMapper movieCreditMapper;
        private final IPersonClient personClient;

        @Override
        @Transactional(readOnly = true)
        public List<MovieCreditResponse> getCreditsByMovieId(UUID movieId) {
                if (movieId == null) {
                        throw new BadRequestException(ErrorCodes.VALIDATION_400, "movie ID null");
                }
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
                List<MovieCreditResponse> response = buildEnrichedCreditResponses(credits).stream()
                                .sorted(Comparator
                                                .comparingInt(c -> c.billingOrder() == null ? Integer.MAX_VALUE
                                                                : c.billingOrder()))
                                .toList();

                // INFO: Final result count after processing
                log.info("Returning {} enriched credits for movie ID: {}", response.size(), movieId);

                return response;
        }

        @Override
        @Transactional(readOnly = true)
        public Page<MovieCreditResponse> getCreditsByMovieIdPaginated(UUID movieId, Pageable pageable) {
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
                List<MovieCreditResponse> enriched = buildEnrichedCreditResponses(page.getContent());

                Page<MovieCreditResponse> responsePage = new PageImpl<>(enriched, pageable, page.getTotalElements());

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
        public MovieCreditResponse getCreditById(UUID creditId) {
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
        @Transactional
        public List<MovieCreditResponse> reorderCredits(UUID movieId, List<MovieCreditRequestDto> orderedDtos) {
                // Validate input early
                if (orderedDtos == null || orderedDtos.isEmpty()) {
                        log.warn("Reorder credits requested with null or empty list for movie ID: {}", movieId);
                        throw new BadRequestException(ErrorCodes.VALIDATION_400, "Reorder list cannot be empty");
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
                        throw new BadRequestException(ErrorCodes.VALIDATION_400,
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
                                                return new BadRequestException(ErrorCodes.VALIDATION_400,
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
        public boolean existsCredit(UUID movieId, UUID personId, Set<Profession> professions) {
                // Basic input validation
                if (movieId == null || personId == null || professions == null) {
                        log.warn("existsCredit called with null parameter - Movie ID: {}, Person ID: {}, Role: {}",
                                        movieId, personId, professions);
                        return false;
                }

                // DEBUG: Log the uniqueness check (very useful when debugging conflicts)
                log.debug("Checking if credit exists - Movie ID: {}, Person ID: {}, Role: {}",
                                movieId, personId, professions);

                boolean exists = movieCreditRepository.existsByMovieIdAndPersonIdAndMovieCharacters(movieId, personId,
                                professions);

                // DEBUG: Result of the check
                log.debug("Credit existence check result - Movie ID: {}, Person ID: {}, Role: {} → {}",
                                movieId, personId, professions, exists);

                return exists;
        }

        // Helper: enrich basic mapped DTO with person details
        private MovieCreditResponse enrichAndMapResponse(MovieCredit credit) {
                // DEBUG: Start of enrichment (external call)
                log.debug("Enriching movie credit - Credit ID: {}, Person ID: {}",
                                credit.getId(), credit.getPersonId());
                MovieCreditPersonResponse movieCredit = personClient.getPersonById(credit.getPersonId());
                if (movieCredit == null) {
                        log.warn("Person is null");
                        throw new BadRequestException(ErrorCodes.VALIDATION_400, "Please, provide valid person");
                }
                return new MovieCreditResponse(
                                credit.getId(),
                                credit.getPersonId(),
                                movieCredit.name(),
                                movieCredit.profileImg(),
                                credit.getProfessions(),
                                credit.getMovieCharacters(),
                                credit.getBillingOrder());

        }

        private List<MovieCreditResponse> buildEnrichedCreditResponses(List<MovieCredit> credits) {
                if (credits == null || credits.isEmpty()) {
                        log.warn("Credits are Empty or null");
                        return Collections.emptyList();
                }
                Set<UUID> personIds = credits.stream()
                                .map(MovieCredit::getPersonId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet());

                log.debug("Extracted person ids :{}", personIds.size());

                // Get persdon details from person service

                List<MovieCreditPersonResponse> persons = personClient.getAllPersonByIds(personIds);

                if (persons.size() != personIds.size()) {
                        log.warn("Person list size mismatch - expected {}, got {}",
                                        credits.size(), persons.size());
                        throw new BadRequestException(ErrorCodes.VALIDATION_400,
                                        "Person list must contain all existing credits with the same size");
                }
                Map<UUID, MovieCreditPersonResponse> personMap = persons.stream()
                                .collect(Collectors.toMap(
                                                MovieCreditPersonResponse::id,
                                                p -> p));

                return credits.stream().map(credit -> {
                        MovieCreditPersonResponse person = personMap.get(credit.getPersonId());
                        String name = person != null ? person.name() : null;
                        String profileImg = person != null ? person.profileImg() : null;

                        return new MovieCreditResponse(
                                        credit.getId(),
                                        credit.getPersonId(),
                                        name,
                                        profileImg,
                                        credit.getProfessions(),
                                        credit.getMovieCharacters(),
                                        credit.getBillingOrder());
                }).toList();

        }

}

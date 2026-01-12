package com.starone.bookshow.person.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.starone.bookshow.person.dto.PersonRequestDto;
import com.starone.common.response.record.MovieCreditPersonResponse;
import com.starone.common.response.record.PersonProfessionSync;
import com.starone.common.response.record.PersonResponse;

public interface IPersonService {

    /**
     * Create a new person (admin/partner use)
     */
    PersonResponse create(PersonRequestDto requestDto);

    /**
     * Get person(s) by ID(s) (used by movie-service for enrichment, frontend for
     * details)
     */
    List<MovieCreditPersonResponse> getAllByIds(Set<UUID> ids);

    /**
     * Get person by ID (used by movie-service for enrichment, frontend for
     * details)
     */
    MovieCreditPersonResponse getPersonById(UUID id);

    /**
     * add profession(s) by ID(s) (used by movie-service to add new professions
     */
    void addProfessionsBulk(List<PersonProfessionSync> bulkUpdates);

    /**
     * Update person (partial - PATCH)
     */
    PersonResponse update(UUID id, PersonRequestDto requestDto);

    /**
     * Get person (full details to view Person)
     */
    PersonResponse getById(UUID id);

    /**
     * Deactivate person (soft delete - hide from public)
     */
    PersonResponse deactivate(UUID id);

    /**
     * Activate person (re-enable if previously deactivated)
     */
    PersonResponse activate(UUID id);

    /**
     * Search persons by name (case-insensitive partial match)
     * Used when adding cast/crew in movie admin panel
     */
    Page<PersonResponse> searchByName(String name, Pageable pageable);

    /**
     * Get all active persons (paginated - for admin list)
     */
    Page<PersonResponse> getAllActive(Pageable pageable);

    /**
     * Get all persons (paginated - for admin list)
     */
    Page<PersonResponse> getAll(Pageable pageable);

    /**
     * Check if person exists by name (for uniqueness during create/update)
     */
    boolean existsByNameIgnoreCase(String name);
}

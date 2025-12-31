package com.starone.bookshow.person.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.starone.bookshow.person.dto.PersonRequestDto;
import com.starone.common.dto.PersonResponseDto;

public interface IPersonService {

    /**
     * Create a new person (admin/partner use)
     */
    PersonResponseDto create(PersonRequestDto requestDto);

    /**
     * Get person by ID (used by movie-service for enrichment, frontend for details)
     */
    PersonResponseDto getById(UUID id);

    /**
     * Find person's by ID's (used by movie-service for validation : bulk operation)
     */
    Set<UUID> findExistingIds(Set<UUID> ids);

    /**
     * Update person (partial - PATCH)
     */
    PersonResponseDto update(UUID id, PersonRequestDto requestDto);

    /**
     * Deactivate person (soft delete - hide from public)
     */
    PersonResponseDto deactivate(UUID id);

    /**
     * Activate person (re-enable if previously deactivated)
     */
    PersonResponseDto activate(UUID id);

    /**
     * Search persons by name (case-insensitive partial match)
     * Used when adding cast/crew in movie admin panel
     */
    Page<PersonResponseDto> searchByName(String name, Pageable pageable);

    /**
     * Get all active persons (paginated - for admin list)
     */
    Page<PersonResponseDto> getAllActive(Pageable pageable);

    /**
     * Get all persons (paginated - for admin list)
     */
    Page<PersonResponseDto> getAll(Pageable pageable);

    /**
     * Check if person exists by name (for uniqueness during create/update)
     */
    boolean existsByNameIgnoreCase(String name);
}

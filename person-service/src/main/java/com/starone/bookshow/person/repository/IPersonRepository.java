package com.starone.bookshow.person.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.starone.bookshow.person.entity.Person;
import com.starone.bookshow.person.projection.PersonMovieCreditProjection;

@Repository
public interface IPersonRepository extends JpaRepository<Person, UUID> {

    // Bulk: fetches only 4 fields — super efficient!
    List<PersonMovieCreditProjection> findAllByIdIn(Set<UUID> ids);

    // Single: renamed to avoid conflict with built-in findById
    Optional<PersonMovieCreditProjection> findPersonById(UUID id);

    // Basic pagination for all persons
    Page<Person> findAll(Pageable pageable);

    // Pagination for active persons only (most common public list)
    Page<Person> findByActiveTrue(Pageable pageable);

    // Pagination for inactive persons (admin view)
    Page<Person> findByActiveFalse(Pageable pageable);

    // Pagination with name search (case-insensitive partial match)
    Page<Person> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Uniqueness checks (no pagination needed)
    boolean existsByNameIgnoreCase(String name);

    Optional<Person> findByNameIgnoreCase(String name);

    // Optional: pagination by profession (if needed later)
    // Page<Person> findByProfessionsContaining(Profession profession, Pageable
    // pageable);
}

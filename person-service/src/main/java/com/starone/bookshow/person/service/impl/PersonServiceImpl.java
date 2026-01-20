package com.starone.bookshow.person.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starone.bookshow.person.dto.PersonRequestDto;
import com.starone.bookshow.person.entity.Person;
import com.starone.bookshow.person.mapper.IPersonMapper;
import com.starone.bookshow.person.projection.PersonMovieCreditProjection;
import com.starone.bookshow.person.repository.IPersonRepository;
import com.starone.bookshow.person.service.IPersonService;
import com.starone.common.enums.Profession;
import com.starone.common.error.ErrorCodes;
import com.starone.common.exceptions.BadRequestException;
import com.starone.common.exceptions.ConflictException;
import com.starone.common.exceptions.NotFoundException;
import com.starone.common.response.record.MovieCreditPersonResponse;
import com.starone.common.response.record.PersonProfessionAddition;
import com.starone.common.response.record.PersonResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonServiceImpl implements IPersonService {

    private static final Logger log = LoggerFactory.getLogger(PersonServiceImpl.class);

    private final IPersonRepository personRepository;
    private final IPersonMapper personMapper;

    @Override
    public PersonResponse create(PersonRequestDto requestDto) {
        String personName = requestDto.getName();

        // check person is already avaiable
        if (personRepository.existsByNameIgnoreCase(personName)) {
            log.warn("Attempt to create duplicate person with name: {}", personName);
            throw new ConflictException(
                    ErrorCodes.PERSON_ALREADY_EXISTS,
                    "Person with name '" + personName + "' already exists");
        }
        log.info("Creating new person with name :{}", personName);

        // for DEBUG
        if (log.isDebugEnabled()) {
            log.debug("Communication Address: {}, Permanent Address: {}",
                    requestDto.getCAddress(), requestDto.getPAddress());
        }

        // Business logic
        Person person = personMapper.toEntity(requestDto);
        person = personRepository.save(person);

        log.info("Person created successfully with ID: {} and name: {}", person.getId(), personName);

        return personMapper.toResponseDto(person);
    }

    @Override
    public List<MovieCreditPersonResponse> getAllByIds(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BadRequestException(ErrorCodes.BAD_REQUEST, "Invalid person id reference");
        }
        List<PersonMovieCreditProjection> persons = personRepository.findAllByIdIn(ids);
        if (persons.size() != ids.size()) {
            Set<UUID> foundIds = persons.stream()
                    .map(PersonMovieCreditProjection::getId)
                    .collect(Collectors.toSet());
            Set<UUID> missingIds = new HashSet<>(ids);
            missingIds.removeAll(foundIds);

            throw new NotFoundException(ErrorCodes.PERSON_NOT_FOUND, "Persons not found: " + missingIds);
        }
        return persons.stream()
                .map(person -> {
                    return new MovieCreditPersonResponse(
                            person.getId(),
                            person.getName(),
                            person.getProfileImg(),
                            person.getProfessions());
                })
                .toList();
    }

    @Override
    public MovieCreditPersonResponse getPersonById(UUID id) {
        if (id == null) {
            throw new BadRequestException(ErrorCodes.BAD_REQUEST, "Person Id is required");
        }
        return personRepository.findPersonById(id).map(person -> {
            return new MovieCreditPersonResponse(person.getId(),
                    person.getName(),
                    person.getProfileImg(),
                    person.getProfessions());
        }).orElseThrow(() -> new NotFoundException(ErrorCodes.PERSON_NOT_FOUND, "Person not found"));
    }

    @Override
    public void addProfessionsToPersons(List<PersonProfessionAddition> bulkUpdates) {
        if (bulkUpdates == null || bulkUpdates.isEmpty()) {
            throw new BadRequestException(ErrorCodes.BAD_REQUEST,
                    "Person profession(s) must not be null or empty");
        }

        // ONE DB call — set all persons Id
        Set<UUID> personIds = bulkUpdates.stream()
                .map(PersonProfessionAddition::personId)
                .collect(Collectors.toSet());

        // ONE DB call — fetches all persons
        Map<UUID, Person> personMap = personRepository.findAllById(personIds)
                .stream()
                .collect(Collectors.toMap(Person::getId, person -> person));

        // Check missing persons early
        Set<UUID> missingIds = personIds.stream()
                .filter(id -> !personMap.containsKey(id))
                .collect(Collectors.toSet());
        if (!missingIds.isEmpty()) {
            throw new NotFoundException(ErrorCodes.PERSON_NOT_FOUND,
                    "Person(s) not found " + missingIds);
        }

        // Apply updates
        for (PersonProfessionAddition update : bulkUpdates) {
            Person person = personMap.get(update.personId());
            Set<Profession> toAdd = update.professions();
            if (toAdd != null && !toAdd.isEmpty()) {
                person.getProfessions().addAll(toAdd);
            }

        }
        // All changes saved in one transaction
        log.info("Bulk added professions for {} persons", bulkUpdates.size());
    }

    @Override
    @Transactional(readOnly = true)
    public PersonResponse getById(UUID id) {

        // INFO: Important business event — retrieving a person record
        log.info("Fetching person by ID: {}", id);
        Person person = personRepository.findById(id)
                .orElseThrow(
                        () -> {
                            log.warn("Person not found for ID: {}", id);

                            return new NotFoundException(
                                    ErrorCodes.PERSON_NOT_FOUND,
                                    "Person not found with id: " + id);

                        });
        return personMapper.toResponseDto(person);

    }

    @Override
    public PersonResponse update(UUID id, PersonRequestDto requestDto) {
        Objects.requireNonNull(id, "Person ID is required");
        log.info("Updating person with ID: {}", id);

        // Fetch existing person
        Person person = personRepository.findById(id)
                .orElseThrow(
                        () -> {
                            log.warn("Person not found for update - ID: {}", id);
                            return new NotFoundException(
                                    ErrorCodes.PERSON_NOT_FOUND,
                                    "Person not found with id: " + id);
                        });
        String oldName = person.getName();
        String newName = requestDto.getName();

        // Check name uniqueness if name is being changed
        if (Objects.nonNull(newName) && !newName.equalsIgnoreCase(oldName)) {
            if (personRepository.existsByNameIgnoreCase(newName)) {

                log.warn("Update failed - name '{}' already exists (person ID: {})", newName, id);

                throw new ConflictException(
                        ErrorCodes.PERSON_ALREADY_EXISTS,
                        "Person with name '" + newName + "' already exists");
            }
            log.info("Changing person name from '{}' to '{}' (ID: {})", oldName, newName, id);
        }
        // Apply updates
        personMapper.updateEntity(requestDto, person);
        person = personRepository.save(person);

        log.info("Person updated successfully with ID: {} and name: {}", person.getId(), person.getName());

        // for DEBUG: Log what fields were updated (if needed for troubleshooting)
        if (log.isDebugEnabled()) {
            log.debug("Updated person details - Email: {}, Addresses changed: {}",
                    person.getEmail(),
                    /* you can add a flag or compare if needed */ "yes/no");
        }
        return personMapper.toResponseDto(person);
    }

    @Override
    public PersonResponse deactivate(UUID id) {

        Objects.requireNonNull(id, "Person ID is required");
        log.info("Deactivating person with ID: {}", id);

        Person person = personRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot deactivate - person not found with ID: {}", id);
                    return new NotFoundException(
                            ErrorCodes.PERSON_NOT_FOUND,
                            "Person not found");
                });

        // If the person is already inactive
        if (!person.isActive()) {
            log.info("Person with ID: {} is already deactivated - no changes made", id);
            return personMapper.toResponseDto(person);
        }

        log.info("Setting person status to inactive - ID: {}, Name: {}", id, person.getName());
        person.setActive(false);

        person = personRepository.save(person);

        log.info("Person deactivated successfully - ID: {} (Name: {})", person.getId(), person.getName());

        return personMapper.toResponseDto(person);
    }

    @Override
    public PersonResponse activate(UUID id) {
        Objects.requireNonNull(id, "Person ID is required");

        log.info("Activating person with ID: {}", id);

        Person person = personRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot activate - person not found with ID: {}", id);
                    return new NotFoundException(
                            ErrorCodes.PERSON_NOT_FOUND,
                            "Person not found");
                });
        // Handle case where person is already active (idempotent operation)
        if (person.isActive()) {
            log.info("Person with ID: {} is already active - no changes made", id);
            return personMapper.toResponseDto(person);
        }

        log.info("Setting person status to active - ID: {}, Name: {}", id, person.getName());
        person.setActive(true);
        person = personRepository.save(person);

        log.info("Person activated successfully - ID: {} (Name: {})", person.getId(), person.getName());

        return personMapper.toResponseDto(person);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonResponse> searchByName(String name, Pageable pageable) {
        // Validate input (optional but recommended)
        if (Objects.isNull(name) || name.trim().isEmpty()) {
            log.warn("Search requested with null or empty name - returning empty page");
            return Page.empty(pageable);
        }
        String searchTerm = name.trim();

        log.info("Searching persons by name containing: '{}' (page: {}, size: {}, sort: {})",
                searchTerm,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());

        Page<Person> page = personRepository.findByNameContainingIgnoreCase(name, pageable);

        log.info("Person search completed - query: '{}', results: {} (total: {}, pages: {})",
                searchTerm,
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getTotalPages());

        // DEBUG: Log actual names returned (useful when debugging relevance)
        if (log.isDebugEnabled() && !page.isEmpty()) {
            List<String> names = page.getContent().stream()
                    .map(Person::getName)
                    .toList();
            log.debug("Found persons: {}", names);
        }

        return page.map(personMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonResponse> getAllActive(Pageable pageable) {
        // INFO: Start of a read operation that lists active records
        log.info("Fetching all active persons (page: {}, size: {}, sort: {})",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());

        Page<Person> page = personRepository.findByActiveTrue(pageable);

        log.info("Retrieved active persons - page: {} of {}, results on page: {}, total active: {}",
                page.getNumber() + 1, // human-readable page number
                page.getTotalPages(),
                page.getNumberOfElements(),
                page.getTotalElements());

        // List names or IDs of returned persons (helpful when debugging
        // pagination/sorting)
        if (log.isDebugEnabled() && !page.isEmpty()) {
            List<String> names = page.getContent().stream()
                    .map(Person::getName)
                    .toList();
            log.debug("Active persons on this page: {}", names);
        }

        return page.map(personMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonResponse> getAll(Pageable pageable) {
        // INFO: Start of a full list operation (potentially large result set)
        log.info("Fetching all persons (including inactive) - page: {}, size: {}, sort: {}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());

        Page<Person> page = personRepository.findAll(pageable);

        // INFO: Result summary — crucial for monitoring total data volume and admin
        // usage
        log.info("Retrieved all persons - page: {} of {}, results on page: {}, total persons: {}",
                page.getNumber() + 1, // human-readable page
                page.getTotalPages(),
                page.getNumberOfElements(),
                page.getTotalElements());

        // Optional DEBUG: Sample of names returned (useful for verifying data)
        if (log.isDebugEnabled() && !page.isEmpty()) {
            List<String> sampleNames = page.getContent().stream()
                    .limit(10) // limit to avoid huge logs
                    .map(Person::getName)
                    .toList();
            log.debug("Persons on this page (sample): {}", sampleNames);
        }

        return page.map(personMapper::toResponseDto);
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        if (Objects.isNull(name) || name.trim().isEmpty()) {
            log.warn("Name existence check called with null or empty name - returning false");
            return false;
        }

        String trimmedName = name.trim();

        // DEBUG: Log the check (useful when troubleshooting uniqueness issues)
        log.debug("Checking if person exists with name (ignore case): '{}'", trimmedName);

        boolean exists = personRepository.existsByNameIgnoreCase(trimmedName);

        // DEBUG: Result of the check
        log.debug("Person name '{}' existence check result: {}", trimmedName, exists);

        return exists;
    }

}

package com.starone.bookshow.person.service.impl;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.starone.bookshow.person.dto.PersonDto;
import com.starone.bookshow.person.entity.Person;
import com.starone.bookshow.person.exception.custom.InvalidInputException;
import com.starone.bookshow.person.exception.custom.ResourceNotFoundException;
import com.starone.bookshow.person.repository.IPersonRepository;
import com.starone.bookshow.person.service.IPersonService;
import com.starone.bookshow.person.utils.PersonMapper;
import com.starone.bookshow.person.utils.PersonUtils;

public class PersonServiceImpl implements IPersonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonServiceImpl.class);
    private final PersonMapper mapper;
    private final IPersonRepository personRepository;

    public PersonServiceImpl(PersonMapper mapper, IPersonRepository personRepository) {
        this.mapper = mapper;
        this.personRepository = personRepository;
    }

    @Override
    public PersonDto addPerson(PersonDto personDto) {
        if (PersonUtils.isNull(personDto)) {
            LOGGER.warn("Failed to save Person! Person must not be null");
            throw new InvalidInputException("Person");
        }
        Person savedPerson = personRepository.save(mapper.mapToPerson(personDto));

        LOGGER.info("Person saved successfully");
        return mapper.mapToPersonDto(savedPerson);

    }

    @Override
    public PersonDto updatePerson(String personId, PersonDto personDto) {
        if (PersonUtils.isNullOrEmpty(personId) || PersonUtils.isNull(personDto)) {
            LOGGER.warn("Failed to update Person! PersonId or Person must not be null");
            throw new InvalidInputException("Person or personId");
        }
        Person existingPerson = personRepository.findById(UUID.fromString(personId)).orElseThrow(() -> {
            LOGGER.warn("Failed to get Person! Person not found.");
            return new ResourceNotFoundException("Person", personId);
        });
        LOGGER.debug("PersonDto mapping with existing Person");
        mapper.mapToExistingPerson(personDto, existingPerson);

        Person updatedPerson = personRepository.save(mapper.mapToPerson(personDto));

        LOGGER.info("Person updated successfully.");
        return mapper.mapToPersonDto(updatedPerson);
    }

    @Override
    public List<PersonDto> getAllPerson() {
        return personRepository.findAll()
                .stream()
                .map(mapper::mapToPersonDto)
                .toList();
    }

    @Override
    public PersonDto getPersonById(String personId) {
        if (PersonUtils.isNullOrEmpty(personId)) {
            LOGGER.warn("Failed to get Person! PersonId must not be null");
            throw new InvalidInputException("PersonId");
        }
        Person existingPerson = personRepository.findById(UUID.fromString(personId)).orElseThrow(() -> {
            LOGGER.warn("Failed to get Person! Person not found.");
            return new ResourceNotFoundException("Person", personId);
        });
        LOGGER.info("Person fetched successfully!");
        return mapper.mapToPersonDto(existingPerson);
    }

    @Override
    public void deletePersonById(String personId) {
        if (PersonUtils.isNullOrEmpty(personId)) {
            LOGGER.warn("Failed to delete Person! PersonId must not be null");
            throw new InvalidInputException("PersonId");
        }
        personRepository.deleteById(UUID.fromString(personId));
        LOGGER.info("Person deleted successfully!");
    }

}

package com.starone.bookshow.person.service.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starone.bookshow.person.dto.PersonRequestDto;
import com.starone.bookshow.person.entity.Person;
import com.starone.bookshow.person.mapper.PersonMapper;
import com.starone.bookshow.person.repository.PersonRepository;
import com.starone.bookshow.person.service.IPersonService;
import com.starone.common.dto.PersonResponseDto;
import com.starone.common.error.ErrorCodes;
import com.starone.common.exceptions.ConflictException;
import com.starone.common.exceptions.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonServiceImpl implements IPersonService {

    private static final Logger log = LoggerFactory.getLogger(PersonServiceImpl.class);

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    @Override
    public PersonResponseDto create(PersonRequestDto requestDto) {
        if (personRepository.existsByNameIgnoreCase(requestDto.getName())) {
            log.warn("Person with name ': {}' already exists.", requestDto.getName());
            throw new ConflictException(ErrorCodes.PERSON_ALREADY_EXISTS,
                    "Person with name '" + requestDto.getName() + "' already exists");
        }
        log.info("CAddress : {} PAddress : {} ", requestDto.getCAddress(), requestDto.getPAddress());
        Person person = personMapper.toEntity(requestDto);
        person = personRepository.save(person);
        return personMapper.toResponseDto(person);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonResponseDto getById(UUID id) {
        Person person = personRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException(ErrorCodes.PERSON_NOT_FOUND, "Person not found with id: " + id));
        return personMapper.toResponseDto(person);
    }

    @Override
    public PersonResponseDto update(UUID id, PersonRequestDto requestDto) {
        Person person = personRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException(ErrorCodes.PERSON_NOT_FOUND, "Person not found with id: " + id));

        // Check name uniqueness if name is being changed
        if (requestDto.getName() != null && !requestDto.getName().equalsIgnoreCase(person.getName())) {
            if (personRepository.existsByNameIgnoreCase(requestDto.getName())) {
                throw new ConflictException(ErrorCodes.PERSON_ALREADY_EXISTS,
                        "Person with name '" + requestDto.getName() + "' already exists");
            }
        }

        personMapper.updateEntity(requestDto, person);
        person = personRepository.save(person);
        return personMapper.toResponseDto(person);
    }

    @Override
    public PersonResponseDto deactivate(UUID id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.PERSON_NOT_FOUND, "Person not found"));
        person.setActive(false);
        person = personRepository.save(person);
        return personMapper.toResponseDto(person);
    }

    @Override
    public PersonResponseDto activate(UUID id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.PERSON_NOT_FOUND, "Person not found"));
        person.setActive(true);
        person = personRepository.save(person);
        return personMapper.toResponseDto(person);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonResponseDto> searchByName(String name, Pageable pageable) {
        Page<Person> page = personRepository.findByNameContainingIgnoreCase(name, pageable);
        return page.map(personMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonResponseDto> getAllActive(Pageable pageable) {
        Page<Person> page = personRepository.findByActiveTrue(pageable);
        return page.map(personMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonResponseDto> getAll(Pageable pageable) {
        Page<Person> page = personRepository.findAll(pageable);
        return page.map(personMapper::toResponseDto);
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return personRepository.existsByNameIgnoreCase(name);
    }

}

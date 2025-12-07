package com.starone.bookshow.person.service;

import java.util.List;

import com.starone.bookshow.person.dto.PersonDto;

public interface IPersonService {
    
    PersonDto addPerson(PersonDto personDto);

    PersonDto updatePerson(String personId, PersonDto personDto);

    List<PersonDto> getAllPerson();

    PersonDto getPersonById(String personId);

    void deletePersonById(String personId);
}

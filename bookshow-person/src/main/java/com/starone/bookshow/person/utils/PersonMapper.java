package com.starone.bookshow.person.utils;

import org.modelmapper.ModelMapper;

import com.starone.bookshow.person.dto.PersonDto;
import com.starone.bookshow.person.entity.Person;

public class PersonMapper {

    private final ModelMapper mapper;

    public PersonMapper(ModelMapper mapper) {
        this.mapper = mapper;
    }

    public Person mapToPerson(PersonDto personDto) {
        return mapper.map(personDto, Person.class);
    }

    public PersonDto mapToPersonDto(Person person) {
        return mapper.map(person, PersonDto.class);
    }

    public void mapToExistingPerson(PersonDto personDto, Person existingPerson) {
        mapper.map(personDto, existingPerson);
    }
}

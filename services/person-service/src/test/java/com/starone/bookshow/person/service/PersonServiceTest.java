package com.starone.bookshow.person.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.starone.bookshow.person.entity.Person;
import com.starone.bookshow.person.helper.TestDataFactory;
import com.starone.bookshow.person.mapper.IPersonMapper;
import com.starone.bookshow.person.repository.IPersonRepository;
import com.starone.bookshow.person.service.impl.PersonServiceImpl;
import com.starone.springcommon.response.record.PersonProfessionAddition;

@ExtendWith(MockitoExtension.class)
public class PersonServiceTest {

    @Mock
    IPersonRepository personRepository;

    @Mock
    IPersonMapper personMapper;

    @InjectMocks
    PersonServiceImpl personService;

    Person person;

    List<PersonProfessionAddition> personProfessions = new ArrayList<>();
    Map<UUID, Person> personMap = new HashMap<>();

    @Nested
    @DisplayName("addProfessionsToPersons() method tests")
    class AddProfessionsToPersonsTests {

        @Test
        void addProfessionsToPersons_success_validBulk() {
            // Arrange
            List<PersonProfessionAddition> personProfessions = TestDataFactory.createBulkUpdates(3);
            Map<UUID, Person> personMap = TestDataFactory.createPersonMapFromUpdates(personProfessions);
            List<Person> persons = personMap.entrySet().stream().map(entry -> entry.getValue()).toList();

            // Stub repository to return existing persons
            when(personRepository.findAllById(anySet())).thenReturn(persons);

            // Act
            personService.addProfessionsToPersons(personProfessions);

            // Assert
            ArgumentCaptor<List<PersonProfessionAddition>> captor = ArgumentCaptor.forClass(List.class);

            for (PersonProfessionAddition personProfession : personProfessions) {
                Person person = personMap.get(personProfession.personId());
                assertTrue(person.getProfessions().containsAll(personProfession.professions()));
            }
            verify(personRepository).findAllById(anySet());
        }

        void addProfessions_whenNewProfession_thenSuccess() {
            // Arrange
            // Act
            // Assert
        }

        void addProfessions_whenDuplicates_thenSkipAndAddNew() {
            // Arrange
            // Act
            // Assert
        }

        void addProfessions_whenNullId_thenBadRequestException() {
            // Arrange
            // Act
            // Assert
        }

    }

}

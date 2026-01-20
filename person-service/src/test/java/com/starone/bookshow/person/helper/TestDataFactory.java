package com.starone.bookshow.person.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.starone.bookshow.person.entity.Person;
import com.starone.common.enums.Profession;
import com.starone.common.response.record.PersonProfessionAddition;

public class TestDataFactory {
    public static List<PersonProfessionAddition> createBulkUpdates(int count) {
        List<PersonProfessionAddition> updates = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            UUID id = UUID.randomUUID();
            updates.add(new PersonProfessionAddition(id, Set.of(Profession.ACTOR, Profession.DIRECTOR)));
        }
        return updates;
    }

    public static Map<UUID, Person> createPersonMapFromUpdates(List<PersonProfessionAddition> updates) {
        Map<UUID, Person> map = new HashMap<>();
        for (PersonProfessionAddition u : updates) {
            Person p = new Person();
            p.setId(u.personId());
            p.setProfessions(new HashSet<>(u.professions())); // start with requested
            map.put(u.personId(), p);
        }
        return map;
    }

}

package com.starone.bookshow.person.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.starone.bookshow.person.entity.Person;

public interface IPersonRepository extends JpaRepository<Person, UUID> {

}

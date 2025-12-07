package com.starone.bookshow.person.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.starone.bookshow.person.enums.EGender;
import com.starone.bookshow.person.enums.EOccupation;

import jakarta.persistence.Entity;

@Entity
public class Person {
    private UUID personId;
    private String name;
    private EGender gender;
    private LocalDate birthdate;
    private String birthPlace;
    private String profilePic;

    private List<EOccupation> occupations;
    private String about;
    private String earlyLife;
    private String career;
    private List<UUID> familyMembers;

    private boolean isActive;
}

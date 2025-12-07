package com.starone.bookshow.person.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.starone.bookshow.person.enums.EGender;
import com.starone.bookshow.person.enums.EOccupation;

public class PersonDto {
    private UUID personId;
    private String name;
    private EGender gender;
    private LocalDate birthdate;
    private String birthplace;
    private String profilePic;

    private List<EOccupation> occupations;
    private String about;
    private String earlyLife;
    private String career;
    private List<UUID> familyMembers;

    private boolean isActive;
}

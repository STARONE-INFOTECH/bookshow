package com.starone.bookshow.movie.request;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.starone.bookshow.movie.enums.EGender;
import com.starone.bookshow.movie.enums.EOccupation;

public class PersonRequest {
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

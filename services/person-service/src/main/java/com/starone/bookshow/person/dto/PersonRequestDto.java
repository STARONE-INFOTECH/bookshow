package com.starone.bookshow.person.dto;

import java.time.LocalDate;
import java.util.Set;

import com.starone.common.enums.Profession;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PersonRequestDto {
    private String name;
    private String nickName;
    private LocalDate birthDate;
    private String email;
    private String pAddress;
    private String cAddress;
    private String profileImg;
    private Set<Profession> professions;
}

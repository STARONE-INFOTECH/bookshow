package com.starone.bookshow.movie.dto;

import java.util.Set;
import java.util.UUID;

import com.starone.common.enums.Profession;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieCreditRequestDto {
    private UUID personId;
    private Set<Profession> professions;
    private Set<String> characterNames;
    private Integer billingOrder;
}

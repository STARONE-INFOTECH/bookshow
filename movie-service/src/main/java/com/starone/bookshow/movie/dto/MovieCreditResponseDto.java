package com.starone.bookshow.movie.dto;

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
public class MovieCreditResponseDto {
    private UUID id;
    private UUID personId; // Reference to Person service
    private String personName;
    private String nickName;
    private String profileImg;
    private Profession profession;
    private String characterName;
    private Integer billingOrder;
}

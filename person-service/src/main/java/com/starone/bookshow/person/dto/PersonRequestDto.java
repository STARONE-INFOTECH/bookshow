package com.starone.bookshow.person.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonRequestDto {
   private String name;              
    private String nickName;
    private LocalDate birthDate;
    private String email;
    private String pAddress;
    private String cAddress;
    private String profileImg;        
}

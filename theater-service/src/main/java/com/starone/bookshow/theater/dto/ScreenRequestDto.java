package com.starone.bookshow.theater.dto;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScreenRequestDto {
    private String name;
    private Set<String> facilities;
    private String seatLayoutJson; // full JSON layout
    private int totalSeats; // optional - can be validated against JSON
}

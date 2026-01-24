package com.starone.bookshow.theater.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScreenRequestDto {
    private String name;
    private Set<String> facilities;
    private String seatLayoutJson; // full JSON layout
    private int totalSeats; // optional - can be validated against JSON
}

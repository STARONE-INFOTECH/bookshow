package com.starone.bookshow.theater.dto;

import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScreenResponseDto {
    private UUID id;
    private String name;
    private int totalSeats;
    private Set<String> facilities;
    private String seatLayoutJson;  // full layout for frontend seat selection
    private boolean active;
}

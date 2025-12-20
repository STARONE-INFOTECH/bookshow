package com.starone.bookshow.show.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShowResponseDto {
    private UUID id;
    private UUID movieId;
    private String movieTitle;
    private String moviePosterUrl;

    private UUID screenId;
    private String screenName;
    private UUID theaterId;
    private String theaterName;
    private String theaterCity;

    private LocalDateTime showStartTime;
    private LocalDateTime showEndTime;

    private String showType;
    private Set<String> formats; // ← added: 4K, Dolby Atmos, etc.

    private String pricingJson;
    private int totalSeats;
    private int availableSeats;
    private boolean active;
}

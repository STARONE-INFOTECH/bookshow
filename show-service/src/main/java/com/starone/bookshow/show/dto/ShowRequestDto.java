package com.starone.bookshow.show.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShowRequestDto {
    private UUID movieId; // from movie-service
    private UUID screenId; // from theater-service

    private LocalDateTime showStartTime; // e.g., 2025-12-20T18:30:00

    private String showType; // 2D, 3D, IMAX, 4DX

    private String pricingJson; // {"SILVER": 180.0, "GOLD": 250.0, "PLATINUM": 350.0}

    private double basePrice; // optional fallback
}

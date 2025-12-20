package com.starone.bookshow.show.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "shows")
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    // Foreign key to Movie service (only movieId - no full entity)
    @Column(name = "movie_id", nullable = false)
    private UUID movieId;

    // Foreign key to Theater service (screenId)
    @Column(name = "screen_id", nullable = false)
    private UUID screenId;

    @Column(nullable = false)
    private LocalDateTime showStartTime; // e.g., 2025-12-20T18:30:00

    @Column(nullable = false)
    private LocalDateTime showEndTime; // computed or stored

    @Column(nullable = false)
    private String showType; // 2D, 3D, IMAX, 4DX, etc.

    // Additional format/technical fields
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "show_formats", joinColumns = @JoinColumn(name = "show_id"))
    @Column(name = "format")
    private Set<String> formats = new HashSet<>();  // e.g., 4K, Dolby Atmos, HDR10, ScreenX
    
    // Pricing per category (can be JSON or separate table - start with JSON for
    // flexibility)
    @Column(name = "pricing", columnDefinition = "jsonb")
    private String pricingJson; // e.g., {"SILVER": 180.0, "GOLD": 250.0, "PLATINUM": 350.0}

    private int totalSeats; // from screen layout

    private int availableSeats; // updated on booking

    @Column(nullable = false)
    private boolean active = true;

    // Optional: base price or other flags
    private double basePrice;
}

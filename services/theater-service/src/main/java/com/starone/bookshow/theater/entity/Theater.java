package com.starone.bookshow.theater.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "theaters")
public class Theater {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name; // e.g., PVR Phoenix Mall

    private String description; // optional long description

    @Column(nullable = false)
    private String city;

    private String address;
    private String landmark;

    private double latitude;
    private double longitude;

    private String contactPhone;
    private String contactEmail;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "theater_amenities", joinColumns = @JoinColumn(name = "theater_id"))
    @Column(name = "amenity")
    private Set<String> amenities = new HashSet<>(); // e.g., Parking, Food Court, 4K, Dolby Atmos

    @Column(nullable = false)
    private boolean active = true;

    // Optional: link to tenant/partner (from tenant-service)
    private UUID tenantId;

    @OneToMany(mappedBy = "theater", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Screen> screens = new HashSet<>();
}

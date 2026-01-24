package com.starone.bookshow.theater.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "screens")
public class Screen {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;                     // e.g., Audi 1, IMAX Screen

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false)
    private Theater theater;

    private int totalSeats;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "screen_facilities", joinColumns = @JoinColumn(name = "screen_id"))
    @Column(name = "facility")
    private Set<String> facilities = new HashSet<>();  // e.g., 3D, Recliner, 4K

    /**
     * Seat layout stored as JSON
     * Recommended: PostgreSQL with jsonb for indexing/querying
     * MySQL: json column
     */
    @Column(name = "seat_layout", columnDefinition = "jsonb")  // PostgreSQL
    // @Column(name = "seat_layout", columnDefinition = "json") // MySQL
    @JdbcTypeCode(SqlTypes.JSON)
    private String seatLayoutJson;
    
    private boolean active = true;
}

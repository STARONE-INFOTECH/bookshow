package com.starone.bookshow.movie.entity;

import java.util.UUID;

import com.starone.common.enums.Profession;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movie_credits")
public class MovieCredit {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID personId; // Reference to Person service

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Profession profession; // ACTOR, DIRECTOR, PRODUCER, etc.

    private String movieCharacter; // only for actors

    private Integer billingOrder; // very useful for ordering cast

    // Foreign key to Movie (same service)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

}

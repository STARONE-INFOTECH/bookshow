package com.starone.bookshow.movie.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.starone.common.enums.Profession;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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

    @ElementCollection(targetClass = Profession.class,fetch = FetchType.LAZY)
    @CollectionTable(name = "movie_credit_professions",joinColumns = @JoinColumn(name="movie_credit_id"))
    @Column(name = "professions", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Profession> professions = new HashSet<>(); // ACTOR, DIRECTOR, PRODUCER, etc.

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "movie_credit_characters", joinColumns = @JoinColumn(name= "movie_credit_id"))
    @Column(name = "character_name", nullable = true)  // allow null/empty for uncredited
    private Set<String> movieCharacters = new HashSet<>(); // only for actors/actress

    private Integer billingOrder; // very useful for ordering cast

    // Foreign key to Movie (same service)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

}

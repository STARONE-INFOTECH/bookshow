package com.starone.bookshow.movie.entity;

import java.util.UUID;

import com.starone.bookshow.movie.request.PersonRequest;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class CastAndCrew {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID castId;
    private PersonRequest person;
    private String role;

}

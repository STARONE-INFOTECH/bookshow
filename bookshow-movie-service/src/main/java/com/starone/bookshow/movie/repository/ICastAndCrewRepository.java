package com.starone.bookshow.movie.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.starone.bookshow.movie.entity.CastAndCrew;

public interface ICastAndCrewRepository extends JpaRepository<CastAndCrew, UUID> {

}

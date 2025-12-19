package com.starone.bookshow.theater.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.starone.bookshow.theater.entity.Theater;

public interface ITheaterRepository extends JpaRepository<Theater, UUID> {
    Page<Theater> findByActiveTrue(Pageable pageable);

    Page<Theater> findByCityIgnoreCase(String city, Pageable pageable);

    Page<Theater> findByCityIgnoreCaseAndActiveTrue(String city, Pageable pageable);

    boolean existsByNameAndCityIgnoreCase(String name, String city);
}

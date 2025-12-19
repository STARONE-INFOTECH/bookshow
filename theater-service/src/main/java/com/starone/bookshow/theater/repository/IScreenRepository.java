package com.starone.bookshow.theater.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.starone.bookshow.theater.entity.Screen;

public interface IScreenRepository extends JpaRepository<Screen, UUID> {
    Page<Screen> findByTheaterId(UUID theaterId, Pageable pageable);

    Page<Screen> findByTheaterIdAndActiveTrue(UUID theaterId, Pageable pageable);
}

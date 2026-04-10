package com.starone.bookshow.theater.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.starone.bookshow.theater.entity.Screen;
import com.starone.bookshow.theater.projection.TheaterScreenShowProjection;

public interface IScreenRepository extends JpaRepository<Screen, UUID> {
    Page<Screen> findByTheaterId(UUID theaterId, Pageable pageable);

    Page<Screen> findByTheaterIdAndActiveTrue(UUID theaterId, Pageable pageable);

    /*
     * ====================================================================
     * --- Internal Service-To-Service usable methods with Feign client ---
     * ====================================================================
     */
    @Query("""
            SELECT 
                t.id AS theaterId,
                t.name AS theaterName,
                t.city AS city,
                s.id AS screenId,
                s.name AS screenName 
            FROM Screen s 
            JOIN s.theater t
            WHERE t.id = :theaterId 
            AND s.id = :screenId 
            AND s.active = true 
            AND t.active = true
            """)
    Optional<TheaterScreenShowProjection> findTheaterAndScreenByScreenId(UUID theaterId, UUID screenId);
}

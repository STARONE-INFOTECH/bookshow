package com.starone.bookshow.theater.repository;

import org.springframework.data.jpa.domain.Specification;

import com.starone.bookshow.theater.entity.Theater;

public class TheaterSpecifications {

    public static Specification<Theater> hasCity(String city) {
        return (root, query, cb) -> city == null
                ? null
                : cb.equal(
                        cb.lower(root.get("city")), city.toLowerCase());
    }

    public static Specification<Theater> isActive(Boolean active) {
        return (root, query, cb) -> 
        active == null
                ? null
                : cb.equal(root.get("active"), active);
    }
}

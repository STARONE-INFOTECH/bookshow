package com.starone.bookshow.theater.repository.h2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.starone.bookshow.theater.entity.Theater;
import com.starone.bookshow.theater.repository.ITheaterRepository;
import com.starone.bookshow.theater.util.ScreenRepositoryTestDataFactory;

@DataJpaTest
@ActiveProfiles("h2")
class TheaterRepositoryH2Test {

    @Autowired
    private ITheaterRepository theaterRepository;

    @Test
    void should_return_only_active_theaters_with_pagination() {
        // Arrange
        theaterRepository.save(ScreenRepositoryTestDataFactory.createTheater("PVR", true));
        theaterRepository.save(ScreenRepositoryTestDataFactory.createTheater("INOX", false));
        theaterRepository.save(ScreenRepositoryTestDataFactory.createTheater("Cinepolis", true));

        Pageable pageable = PageRequest.of(0, 2);

        // Act

        Page<Theater> page = theaterRepository.findByActiveTrue(pageable);

        // Assert
        assertEquals(2, page.getTotalElements());
        assertEquals(1, page.getTotalPages());
        assertEquals(2, page.getContent().size());
        assertTrue(page.getContent().stream().allMatch(Theater::isActive));
    }

}

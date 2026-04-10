package com.starone.bookshow.theater.repository.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.starone.bookshow.theater.entity.Screen;
import com.starone.bookshow.theater.entity.Theater;
import com.starone.bookshow.theater.projection.TheaterScreenShowProjection;
import com.starone.bookshow.theater.repository.IScreenRepository;
import com.starone.bookshow.theater.util.ScreenRepositoryTestDataFactory;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ScreenRepositoryPostgresTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static void overrideDatasource(DynamicPropertyRegistry registry){
        registry.add("pring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.database-platform", ()-> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private IScreenRepository screenRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void should_fetch_theater_and_screen_when_both_are_active() {
        // Arrange
        Theater theater = ScreenRepositoryTestDataFactory.getActiveTheater();
        entityManager.persist(theater);

        Screen screen = ScreenRepositoryTestDataFactory.getActiveScreenWithTheater();
        entityManager.persist(screen);

        entityManager.flush();

        // Act
        Optional<TheaterScreenShowProjection> result = screenRepository.findTheaterAndScreenByScreenId(
                theater.getId(),
                screen.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(theater.getId(), result.get().getTheaterId());
        assertEquals(screen.getId(), result.get().getScreenId());

        TheaterScreenShowProjection projection = result.get();
        assertEquals("PVR Cinemas", projection.getTheaterName());
        assertEquals("Bangalore", projection.getCity());
        assertEquals("Screen 1", projection.getScreenName());
    }

}

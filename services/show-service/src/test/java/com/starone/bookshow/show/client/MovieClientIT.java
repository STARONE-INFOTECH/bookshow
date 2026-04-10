package com.starone.bookshow.show.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import com.starone.springcommon.response.record.MovieShowResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
class MovieClientIT {

    @Autowired
    private MovieClient movieClient;

    private static final UUID MOVIE_ID = UUID.fromString("2ad27668-92a0-4916-a69c-72f0275378d3");

    @Test
    void sould_return_movie_data_from_feign_client(){
        stubFor(get(urlEqualTo("/api/v1/movies/show/2ad27668-92a0-4916-a69c-72f0275378d3")).willReturn(
            aResponse().withHeader("Content-Type", "application/json")
            .withBody("""
                {
                    "id":"2ad27668-92a0-4916-a69c-72f0275378d3",
                    "title":"Inception",
                    "posterUr":"inception.url"
                }
            """)));

         MovieShowResponse movie = movieClient.getMovieById(MOVIE_ID);
         assertEquals(UUID.fromString("2ad27668-92a0-4916-a69c-72f0275378d3"), movie.id());
         assertEquals("Inception", movie.title());
    }

}

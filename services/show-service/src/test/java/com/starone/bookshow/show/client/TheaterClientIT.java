package com.starone.bookshow.show.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import com.starone.springcommon.response.record.TheaterScreenShowResponse;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
class TheaterClientIT {

    private TheaterClient theaterClient;

    private UUID THEATER_ID = UUID.fromString("905948dc-db63-4c4f-9a2e-79a37f9bfc04");
    private UUID SCREEN_ID = UUID.fromString("4433e10e-a979-485a-86f1-766847bc3560");

    @Test
    void sould_return_theater_data_from_feign_client() {
        stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                        "theaterId":"905948dc-db63-4c4f-9a2e-79a37f9bfc04",
                                        "theaterName":"PVR",
                                        "theaterCity":"Mumbai",
                                        "screenId":"4433e10e-a979-485a-86f1-766847bc3560",
                                        "screenName":"Screen_1"
                                """)));

        TheaterScreenShowResponse theater = theaterClient.getTheaterByScreenId(THEATER_ID, SCREEN_ID);
        assertEquals(
                UUID.fromString("905948dc-db63-4c4f-9a2e-79a37f9bfc04"),
                theater.theaterId());
        assertEquals(
                UUID.fromString("4433e10e-a979-485a-86f1-766847bc3560"),
                theater.screenId());
        assertEquals("PVR", theater.screenName());
        assertEquals("Mumbai", theater.theaterCity());
        assertEquals("Screen_1", theater.screenName());
    }
}

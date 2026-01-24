package com.starone.bookshow.theater.projection;

import java.util.UUID;

public interface TheaterScreenShowProjection {

    // Theater fields
    UUID getTheaterId();

    String getTheaterName();

    String getCity();

    // Screen fields
    UUID getScreenId();

    String getScreenName();

}

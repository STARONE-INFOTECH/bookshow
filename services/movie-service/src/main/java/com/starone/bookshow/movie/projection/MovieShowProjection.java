package com.starone.bookshow.movie.projection;

import java.util.UUID;

public interface MovieShowProjection {
    
    UUID getId();

    String getTitle();

    String getPosterUrl();
}

package com.starone.bookshow.person.projection;

import java.util.Set;
import java.util.UUID;

import com.starone.common.enums.Profession;

public interface PersonMovieCreditProjection {
    
    UUID getId();

    String getName();

    String getProfileImg();

    Set<Profession> getProfessions();
}

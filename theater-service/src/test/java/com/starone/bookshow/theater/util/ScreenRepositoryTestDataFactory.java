package com.starone.bookshow.theater.util;

import com.starone.bookshow.theater.entity.Screen;
import com.starone.bookshow.theater.entity.Theater;

public class ScreenRepositoryTestDataFactory {

    private static Theater theater = new Theater();
    private static Screen screen = new Screen();

    public static Theater getActiveTheater() {
        theater.setName("PVR Cinemas");
        theater.setCity("Bangalore");
        theater.setActive(true);
        return theater;
    }

    public static Screen getActiveScreenWithTheater() {
        screen.setName("Screen 1");
        screen.setActive(true);
        screen.setTheater(theater);
        return screen;
    }

    public static Theater createTheater(String name, boolean active) {
        Theater t = new Theater();
        t.setName(name);
        t.setCity("Bangalore");
        t.setActive(active);
        return t;
    }

}

package com.ft.platform.dropwizard.system;

import java.text.ParseException;
import java.util.Date;

public abstract class Clock {

    private static Clock instance = new SystemClock();

    public static void fixClockAt(String date) throws ParseException {
        instance = new FixedClock(date);
    }

    public static Date now() {
        return instance.currentTime();
    }

    public abstract Date currentTime();
}

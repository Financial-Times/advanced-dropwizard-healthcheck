package com.ft.platform.dropwizard.system;

import java.util.Date;

public class SystemClock extends Clock {

    @Override
    public Date currentTime() {
        return new Date();
    }
}

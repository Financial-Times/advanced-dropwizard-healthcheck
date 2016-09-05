package com.ft.platform.dropwizard.system;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import java.text.ParseException;
import java.util.Date;

public class FixedClock extends Clock {

    public Date currentTime = new Date();

    public FixedClock(String date) throws ParseException {
        setCurrentFromIsoString(date);
    }

    @Override
    public Date currentTime() {
        return currentTime;
    }

    public void setCurrentFromIsoString(String date) throws ParseException {
        currentTime = new ISO8601DateFormat().parse(date);
    }
}

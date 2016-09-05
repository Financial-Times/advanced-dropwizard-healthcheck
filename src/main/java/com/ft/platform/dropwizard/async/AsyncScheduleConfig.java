package com.ft.platform.dropwizard.async;

import java.util.concurrent.TimeUnit;

public class AsyncScheduleConfig {

    private long startDelay;
    private long startupWaitTime;
    private long period;
    private TimeUnit timeUnit;

    /**
     * @param startDelay the delay to add to the initial health check run
     * @param startupWaitTime, time the application will wait for the initial asynchronous healthcheck run to
     *                         complete before starting up
     * @param period the delay between the end of one execution and the start of another
     * @param timeUnit the time units for the startDelay and period parameters
     *
     *  N.B The period is the time between the end of the execution of one health check
     *  and the start of the subsequent health check. Therefore the period between checks
     *  may not be static!
     * */
    public AsyncScheduleConfig(final long startDelay, final long startupWaitTime, final long period, final TimeUnit timeUnit) {
        this.startDelay = startDelay;
        this.startupWaitTime = startupWaitTime;
        this.period = period;
        this.timeUnit = timeUnit;
    }

    public AsyncScheduleConfig() {
        this(0, 10, 10, TimeUnit.SECONDS);
    }

    public long getStartDelay() {

        return startDelay;
    }

    public long getPeriod() {
        return period;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setStartDelay(long startDelay) {
        this.startDelay = startDelay;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public long getStartupWaitTime() {
        return startupWaitTime;
    }

    public void setStartupWaitTime(long startupWaitTime) {
        this.startupWaitTime = startupWaitTime;
    }
}

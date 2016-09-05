package com.ft.platform.dropwizard;

import com.ft.platform.dropwizard.async.AsyncScheduleConfig;

public interface AdvancedHealthCheckConfig extends ConfigWithAppInfo {
    AsyncScheduleConfig getAsyncSchedule();
}

package com.ft.platform.dropwizard.async;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.platform.dropwizard.AdvancedSynchronousHealthChecksRunner;
import com.ft.platform.dropwizard.HealthCheckPageData;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

public class AdvancedAsyncHealthCheckRunner implements Managed {
    private static Logger LOG = LoggerFactory.getLogger(AdvancedAsyncHealthCheckRunner.class);

    private final AdvancedSynchronousHealthChecksRunner synchronousHealthChecksRunner;
    private AsyncScheduleConfig asyncScheduleConfig;
    private ScheduledExecutorService executorService;
    private volatile HealthCheckPageData pageData;
    private CompletableFuture<HealthCheckPageData> initialRunFuture = new CompletableFuture<>();

    public AdvancedAsyncHealthCheckRunner(final AdvancedSynchronousHealthChecksRunner synchronousHealthChecksRunner,
                                          final AsyncScheduleConfig asyncScheduleConfig,
                                          final ScheduledExecutorService executorService) {
        this.synchronousHealthChecksRunner = synchronousHealthChecksRunner;
        this.asyncScheduleConfig = asyncScheduleConfig;
        this.executorService = executorService;
        this.pageData = new HealthCheckPageData(
                synchronousHealthChecksRunner.getAppName(),
                "First run not complete",
                new ArrayList<>(),
                new ObjectMapper(),
                synchronousHealthChecksRunner.getAppName()
        );
    }

    //Using the dw lifecycle ensures all of the healthchecks have been registered before
    //the first run of the async healthcheck is kicked off
    @Override
    public void start() {
        executorService.scheduleWithFixedDelay(
                () -> {
                    pageData = synchronousHealthChecksRunner.run();
                    initialRunFuture.complete(pageData);
                },
                asyncScheduleConfig.getStartDelay(),
                asyncScheduleConfig.getPeriod(),
                asyncScheduleConfig.getTimeUnit()
        );

        try {
            //this will cause dropwizard to wait for the first run of the healthchecks to complete
            //or a timeout before starting up, this avoids Thread.sleeps() in tests
            initialRunFuture.get(
                    asyncScheduleConfig.getStartupWaitTime(),
                    asyncScheduleConfig.getTimeUnit()
            );
        } catch (Exception e) {
            LOG.info(String.format(
                    "Healthcheck run did not complete successfully before the startup wait time of %d %s",
                    asyncScheduleConfig.getStartupWaitTime(),
                    asyncScheduleConfig.getTimeUnit()
            ), e);
        }

    }

    public HealthCheckPageData healthCheckPageData() {
        return pageData;
    }

    @Override
    public void stop() throws Exception {

    }
}

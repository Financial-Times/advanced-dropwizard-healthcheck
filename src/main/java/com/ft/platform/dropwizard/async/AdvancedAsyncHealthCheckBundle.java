package com.ft.platform.dropwizard.async;

import com.ft.platform.dropwizard.AdvancedHealthCheckConfig;
import com.ft.platform.dropwizard.AdvancedSynchronousHealthChecksRunner;
import com.ft.platform.dropwizard.metrics.HealthcheckFailureReporter;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ft.platform.dropwizard.AdvancedHealthcheckRegistration.registerServlets;

public class AdvancedAsyncHealthCheckBundle implements ConfiguredBundle<AdvancedHealthCheckConfig> {
    private static Logger LOG = LoggerFactory.getLogger(AdvancedAsyncHealthCheckBundle.class);

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
    }

    @Override
    public void run(final AdvancedHealthCheckConfig configuration, final Environment environment) throws Exception {
        final AdvancedAsyncHealthCheckRunner advancedAsyncHealthCheckRunner = new AdvancedAsyncHealthCheckRunner(
                new AdvancedSynchronousHealthChecksRunner(
                        environment,
                        environment.getObjectMapper(),
                        environment.getName(),
                        environment.getName(),
                        environment.getName(),
                        new HealthcheckFailureReporter(environment.metrics())
                ),
                configuration.getAsyncSchedule(),
                environment.lifecycle().scheduledExecutorService("Async Healthcheck").build()
        );

        final AdvancedAsyncHealthCheckServlet healthcheckServlet = new AdvancedAsyncHealthCheckServlet(
                advancedAsyncHealthCheckRunner
        );

        environment.lifecycle().manage(advancedAsyncHealthCheckRunner);

        registerServlets( environment, healthcheckServlet);
    }
}

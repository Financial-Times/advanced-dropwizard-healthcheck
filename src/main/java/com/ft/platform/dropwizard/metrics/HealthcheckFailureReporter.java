package com.ft.platform.dropwizard.metrics;

import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HealthcheckFailureReporter {

    private static Logger log = LoggerFactory.getLogger(HealthcheckFailureReporter.class);

    private final MetricRegistry metricRegistry;

    public HealthcheckFailureReporter(final MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public void updateFailureCounter(final String healthCheckName) {
        final String counterName = buildFailuresCounterName(healthCheckName);
        metricRegistry.counter(counterName).inc();
        log.info("Health check [{}] has failed, incrementing metric registry counter {} by 1 to {}",
                healthCheckName,
                counterName,
                metricRegistry.counter(counterName).getCount()
        );
    }

    protected String buildFailuresCounterName(final String healthCheckName) {
        return String.format("healthcheck-failures.%s", healthCheckName.toLowerCase().replaceAll("[./ ]", "_"));
    }
}

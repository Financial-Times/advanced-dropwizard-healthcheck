package com.ft.platform.dropwizard.metrics;

import com.codahale.metrics.MetricRegistry;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthcheckFailureReporterTest {

    public static final String HEALTHCHECK_NAME = "test";

    MetricRegistry metricRegistry;
    HealthcheckFailureReporter healthcheckFailureReporter = new HealthcheckFailureReporter(metricRegistry);

    @Before
    public void setUp() {
        metricRegistry = new MetricRegistry();
        healthcheckFailureReporter = new HealthcheckFailureReporter(metricRegistry);
    }

    @Test
    public void updateFailureCounterShouldIncrementCounterCorrectly() {
        final String counterName = healthcheckFailureReporter.buildFailuresCounterName(HEALTHCHECK_NAME);
        assertThat(metricRegistry.counter(counterName).getCount()).isEqualTo(0L);
        HealthcheckFailureReporter healthcheckFailureReporter = new HealthcheckFailureReporter(metricRegistry);
        healthcheckFailureReporter.updateFailureCounter(HEALTHCHECK_NAME);
        assertThat(metricRegistry.counter(counterName).getCount()).isEqualTo(1L);
    }

    @Test
    public void buildFailuresCounterNameShouldEscapePeriodsCorrectly() {
        final String input = "Kafka healthcheck for http://cmdb.ft.com/systems/user-cred-svc";
        final String expected = "healthcheck-failures.kafka_healthcheck_for_http:__cmdb_ft_com_systems_user-cred-svc";
        assertThat(healthcheckFailureReporter.buildFailuresCounterName(input)).isEqualTo(expected);
    }
}

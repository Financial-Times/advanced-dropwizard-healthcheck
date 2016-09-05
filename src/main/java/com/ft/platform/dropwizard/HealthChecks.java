package com.ft.platform.dropwizard;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.setup.Environment;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class HealthChecks {

    public static SortedMap<AdvancedHealthCheck, AdvancedResult> runAdvancedHealthChecksIn(Environment environment) {
        Map<String, HealthCheck> healthChecks = extractHealthChecksFrom(environment);

        final SortedMap<AdvancedHealthCheck, AdvancedResult> results = new TreeMap<AdvancedHealthCheck, AdvancedResult>();
        for (HealthCheck entry: healthChecks.values()) {
            if (!(entry instanceof AdvancedHealthCheck)) continue;
            AdvancedHealthCheck healthCheck = (AdvancedHealthCheck) entry;
            final AdvancedResult result = healthCheck.executeAdvanced();
            results.put(healthCheck, result);
        }

        return Collections.unmodifiableSortedMap(results);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, HealthCheck> extractHealthChecksFrom(Environment environment) {
        try {
            Field healthChecksField = HealthCheckRegistry.class.getDeclaredField("healthChecks");
            healthChecksField.setAccessible(true);
            return (Map<String, HealthCheck>) healthChecksField.get(environment.healthChecks());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalArgumentException(e);
        }
    }
}

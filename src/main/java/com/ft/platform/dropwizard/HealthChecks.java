package com.ft.platform.dropwizard;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.setup.Environment;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HealthChecks {

    public static SortedMap<AdvancedHealthCheck, AdvancedResult> runAdvancedHealthChecksIn(Environment environment) {
        return runParallelAdvancedHealthChecksWithTimeoutIn(environment, 0);
    }

    static SortedMap<AdvancedHealthCheck, AdvancedResult> runParallelAdvancedHealthChecksWithTimeoutIn(Environment environment, int timeoutInSeconds) {
        Map<String, HealthCheck> healthChecks = extractHealthChecksFrom(environment);

        final SortedMap<AdvancedHealthCheck, AdvancedResult> results = new TreeMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(healthChecks.size());
        Map<AdvancedHealthCheck, Future<AdvancedResult>> futures = new TreeMap<>();
        for (HealthCheck entry : healthChecks.values()) {
            if (!(entry instanceof AdvancedHealthCheck)) continue;
            AdvancedHealthCheck healthCheck = (AdvancedHealthCheck) entry;
            futures.put(healthCheck, executorService.submit(healthCheck::executeAdvanced));
        }

        for (Map.Entry<AdvancedHealthCheck, Future<AdvancedResult>> entry: futures.entrySet()) {
            try {
                if (timeoutInSeconds > 0) {
                    results.put(entry.getKey(), entry.getValue().get(timeoutInSeconds, TimeUnit.SECONDS));
                } else {
                    results.put(entry.getKey(), entry.getValue().get());
                }
            } catch (InterruptedException | ExecutionException e) {
                results.put(entry.getKey(), AdvancedResult.error(entry.getKey(), e));
            } catch (TimeoutException e) {
                results.put(entry.getKey(), AdvancedResult.error(entry.getKey(), "Timed out after " + timeoutInSeconds + " second(s)"));
            }
        }
        executorService.shutdownNow();
        return Collections.unmodifiableSortedMap(results);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, HealthCheck> extractHealthChecksFrom(Environment environment) {
        try {
            Field healthChecksField = HealthCheckRegistry.class.getDeclaredField("healthChecks");
            healthChecksField.setAccessible(true);
            return (Map<String, HealthCheck>) healthChecksField.get(environment.healthChecks());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalArgumentException(e);
        }
    }
}

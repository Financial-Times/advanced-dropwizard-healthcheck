package com.ft.platform.dropwizard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.platform.dropwizard.metrics.HealthcheckFailureReporter;
import io.dropwizard.setup.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

public class AdvancedSynchronousHealthChecksRunner {

    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final String appName;
    private final String appDescription;
    private final HealthcheckFailureReporter healthcheckFailureReporter;
    private final String systemCode;

    public AdvancedSynchronousHealthChecksRunner(final Environment environment,
                                                 final String appName,
                                                 final String appDescription,
                                                 final String systemCode,
                                                 final HealthcheckFailureReporter healthcheckFailureReporter) {
        this(environment, buildObjectMapper(), appName, appDescription, systemCode, healthcheckFailureReporter);
    }

    public AdvancedSynchronousHealthChecksRunner(final Environment environment,
                                                 final ObjectMapper objectMapper,
                                                 final String appName,
                                                 final String appDescription,
                                                 final String systemCode,
                                                 final HealthcheckFailureReporter healthcheckFailureReporter) {
        this.environment = environment;
        this.objectMapper = objectMapper;
        this.appName = appName;
        this.appDescription = appDescription;
        this.healthcheckFailureReporter = healthcheckFailureReporter;
        this.systemCode = systemCode;
    }

    private static ObjectMapper buildObjectMapper() {
        return new ObjectMapper();
    }

    public String getAppName() {
        return appName;
    }

    public HealthCheckPageData run() {
        SortedMap<AdvancedHealthCheck, AdvancedResult> results = HealthChecks.runAdvancedHealthChecksIn(environment);

        List<CheckResultData> checkResults = new ArrayList<>(results.size());
        results.entrySet().stream()
                .filter(x -> x.getValue().status() == AdvancedResult.Status.OK)
                .forEach(x -> checkResults.add(new CheckResultData(x.getKey(), x.getValue())));
        results.entrySet().stream()
                .filter(x -> x.getValue().status() != AdvancedResult.Status.OK)
                .forEach(x -> {
                    healthcheckFailureReporter.updateFailureCounter(x.getKey().getName());
                    checkResults.add(new ErrorCheckResultData(x.getKey(), x.getValue()));
                });

        return new HealthCheckPageData(appName, appDescription, checkResults, objectMapper, systemCode).log();
    }

}

package com.ft.platform.dropwizard;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.setup.Environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class AdvancedHealthChecksRunner {

    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final String appName;
    private final String appDescription;

    public AdvancedHealthChecksRunner(Environment environment, String appName, String appDescription) {
        this(environment, buildObjectMapper(), appName, appDescription);
    }

    public AdvancedHealthChecksRunner(Environment environment, ObjectMapper objectMapper, String appName, String appDescription) {
        this.environment = environment;
        this.objectMapper = objectMapper;
        this.appName = appName;
        this.appDescription = appDescription;
    }

    private static ObjectMapper buildObjectMapper() {
        return new ObjectMapper();
    }

    public HealthCheckPageData run() {
        SortedMap<AdvancedHealthCheck, AdvancedResult> results = HealthChecks.runAdvancedHealthChecksIn(environment);
        List<CheckResultData> checkResults = new ArrayList<CheckResultData>(results.size());
        for (Map.Entry<AdvancedHealthCheck, AdvancedResult> result: results.entrySet()) {
            if(result.getValue().status() == AdvancedResult.Status.OK){
                checkResults.add(new CheckResultData(result.getKey(), result.getValue()));
            }else{
                checkResults.add(new ErrorCheckResultData(result.getKey(), result.getValue()));
            }
        }
        
        return new HealthCheckPageData(appName, appDescription, checkResults, objectMapper).log();
    }
}

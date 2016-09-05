package com.ft.platform.dropwizard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HealthCheckPageData {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckPageData.class); 
    
    private final int schemaVersion = 1;
    private final String name;
    private final String description;
    private final List<CheckResultData> checks;

    private final ObjectMapper objectMapper;

    public HealthCheckPageData(String name, String description, List<CheckResultData> checks, ObjectMapper objectMapper) {
        this.name = name;
        this.description = description;
        this.checks = checks;
        this.objectMapper = objectMapper;

        Collections.sort(this.checks);
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<CheckResultData> getChecks() {
        return checks;
    }
    
    public HealthCheckPageData log() {
        for (CheckResultData check : checks) {
            LOGGER.info(String.format("event=\"advancedHealthCheck\", action=\"detail\", name=\"%s\", checkName=\"%s\", ok=\"%s\", checkOutput=\"%s\"", 
                    name, check.getName(), check.isOk(), check.getCheckOutput()));
        }
        LOGGER.info(String.format("event=\"advancedHealthCheck\", action=\"summary\", name=\"%s\", status=\"%s\"", name, overallStatus()));
        return this;
    }

    public AdvancedResult.Status overallStatus() {
        boolean hasErrors = false;
        boolean hasWarnings = false;

        for (CheckResultData checkResult : checks) {
            if (checkResult instanceof ErrorCheckResultData) {
                if (((ErrorCheckResultData) checkResult).isWarning()) {
                    hasWarnings = true;
                } else if (((ErrorCheckResultData) checkResult).isError()) {
                    hasErrors = true;
                }
            }
        }

        if (hasErrors) {
            return AdvancedResult.Status.ERROR;
        }

        if (hasWarnings) {
            return AdvancedResult.Status.WARN;
        }

        return AdvancedResult.Status.OK;
    }

    @Override
    public String toString() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

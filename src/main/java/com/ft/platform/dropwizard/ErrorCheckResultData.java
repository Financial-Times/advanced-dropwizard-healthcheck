package com.ft.platform.dropwizard;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ErrorCheckResultData extends CheckResultData {

    public ErrorCheckResultData(AdvancedHealthCheck healthCheck, AdvancedResult result) {
        super(healthCheck, result);
    }

    public boolean isOk() {
        return result.status() == AdvancedResult.Status.OK;
    }

    @JsonIgnore
    public boolean isWarning() {
        return result.status() == AdvancedResult.Status.WARN;
    }

    @JsonIgnore
    public boolean isError() {
        return result.status() == AdvancedResult.Status.ERROR;
    }

    public String getCheckOutput() {
        return result.checkOutput();
    }

}

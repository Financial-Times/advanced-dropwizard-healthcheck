package com.ft.platform.dropwizard;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ErrorCheckResultData extends CheckResultData {

    public ErrorCheckResultData(final AdvancedHealthCheck healthCheck, final AdvancedResult result) {
        super(healthCheck, result);
    }

    public boolean isOk() {
        return super.result.status() == AdvancedResult.Status.OK;
    }

    @JsonIgnore
    public boolean isWarning() {
        return super.result.status() == AdvancedResult.Status.WARN;
    }

    @JsonIgnore
    public boolean isError() {
        return super.result.status() == AdvancedResult.Status.ERROR;
    }

    public String getCheckOutput() {
        return super.result.checkOutput();
    }

}

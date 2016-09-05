package com.ft.platform.dropwizard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.google.common.base.Objects;

public class CheckResultData implements Comparable<CheckResultData> {

    final AdvancedHealthCheck healthCheck;
    final AdvancedResult result;

    public CheckResultData(AdvancedHealthCheck healthCheck, AdvancedResult result) {
        this.healthCheck = healthCheck;
        this.result = result;
    }

    public String getName() {
        return healthCheck.getName();
    }

    public boolean isOk() {
        return true;
    }

    public String getLastUpdated() {
        return new ISO8601DateFormat().format(result.checkedDate());
    }

    public String getCheckOutput() {
        return result.checkOutput();
    }

    public int getSeverity() {
        return healthCheck.severity();
    }

    public String getBusinessImpact() {
        return healthCheck.businessImpact();
    }

    public String getTechnicalSummary() {
        return healthCheck.technicalSummary();
    }

    public String getPanicGuide() {
        return healthCheck.panicGuideUrl();
    }

    @JsonIgnore
    public AdvancedResult getResult() {
		return result;
	}

	public String toString(){
        return Objects.toStringHelper(this)
                .add("name", getName())
                .add("isOk", isOk())
                .add("lastUpdated", getLastUpdated())
                .add("checkOutput", getCheckOutput())
                .add("Severity", getSeverity())
                .add("Business Impact", getBusinessImpact())
                .add("Technical Summary", getTechnicalSummary())
                .add("Panic Guide", getPanicGuide())
                .toString();
    }

	@Override
	public int compareTo(CheckResultData o) {
		return this.getResult().status().getPriority().compareTo(o.getResult().status().getPriority());
	}
    
    
}

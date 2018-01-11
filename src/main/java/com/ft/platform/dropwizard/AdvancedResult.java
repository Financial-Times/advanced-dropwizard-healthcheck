package com.ft.platform.dropwizard;

import com.codahale.metrics.health.HealthCheck;
import com.ft.platform.dropwizard.system.Clock;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

public class AdvancedResult {

    private final AdvancedHealthCheck healthCheck;

    public static enum Status {
    	OK (3), WARN (2), ERROR (1);
    	
    	private Integer priority;
    	
    	Status(Integer priority) {
    		this.priority = priority;
    	}

		public Integer getPriority() {
			return priority;
		}
    }

    private final Status status;
    private final String checkOutput;
    private final Date checkedDate;

    private AdvancedResult(AdvancedHealthCheck healthCheck, Status status, String checkOutput) {
        this.healthCheck = healthCheck;
        this.status = status;
        this.checkOutput = checkOutput;
        this.checkedDate = Clock.now();
    }
    public static AdvancedResult healthy() {
        return healthy("OK");
    }

    public static AdvancedResult healthy(String message) {
        return new AdvancedResult(null, Status.OK, message);
    }

    public static AdvancedResult warn(AdvancedHealthCheck healthCheck,String message) {
        return new AdvancedResult(healthCheck,Status.WARN, message);
    }

    public static AdvancedResult error(AdvancedHealthCheck healthCheck,Throwable e) {
        return new AdvancedResult(healthCheck,Status.ERROR, renderException(e));
    }

    public static AdvancedResult error(AdvancedHealthCheck healthCheck, String message) {

        return new AdvancedResult(healthCheck, Status.ERROR, message);
    }

    public static AdvancedResult error(AdvancedHealthCheck healthCheck,String message, Throwable e) {
        String fullMessage = message + "\n" + renderException(e);
        return new AdvancedResult(healthCheck, Status.ERROR, fullMessage);
    }

    private static String renderException(Throwable e) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(out));
        return new String(out.toByteArray());
    }

    public HealthCheck.Result asResult() {
        if(status()!=Status.OK){
            return HealthCheck.Result.unhealthy(getErrorMessage(checkOutput));
        }else{
            return HealthCheck.Result.healthy(checkOutput);
        }
    }

    public Status status() {
        return status;
    }

    public String checkOutput() {
        return checkOutput;
    }

    public AdvancedHealthCheck getAdvancedHealthCheck() {
      return this.healthCheck;
    }

    public Date checkedDate() {
        return checkedDate;
    }

    private String getErrorMessage(String checkOutput) {
        return Joiner.on("\n").withKeyValueSeparator(": ").join(ImmutableMap.of(
                "Severity", healthCheck.severity(),
                "Business impact", healthCheck.businessImpact(),
                "Technical summary", healthCheck.technicalSummary(),
                "Panic guide", healthCheck.panicGuideUrl(),
                "Output", checkOutput));
    }

	@Override
	public String toString() {
		return Objects.toStringHelper(this.getClass())
				.add("status", status)
				.add("checkOutput", checkOutput)
				.add("checkedDate", checkedDate)
				.add("healthCheck", healthCheck)
				.toString();
	}
}

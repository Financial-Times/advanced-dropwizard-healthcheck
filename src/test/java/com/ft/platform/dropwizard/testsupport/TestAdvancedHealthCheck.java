package com.ft.platform.dropwizard.testsupport;

import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;

import static com.ft.platform.dropwizard.AdvancedResult.Status.OK;

public class TestAdvancedHealthCheck extends AdvancedHealthCheck {

    public static AdvancedResult.Status status = OK;

    public TestAdvancedHealthCheck() {
        super("Advanced Check Test");
    }

    @Override
    protected String id() {
        return "id";
    }

    @Override
    protected AdvancedResult checkAdvanced() throws Exception {
        switch (status) {
            case OK: return AdvancedResult.healthy("Everything is fine");
            case WARN: return AdvancedResult.warn(this, "Hmmm...not looking so good");
        }

        return AdvancedResult.error(this, "The audit host's RAID array is on fire");
    }

    @Override
    protected int severity() {
        return 1;
    }

    @Override
    protected String businessImpact() {
        return "The CEO will go to prison";
    }

    @Override
    protected String technicalSummary() {
        return "The audit system has failed";
    }

    @Override
    protected String panicGuideUrl() {
        return "http://pan.ic/helpme";
    }
}

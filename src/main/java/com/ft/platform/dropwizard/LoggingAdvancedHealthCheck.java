package com.ft.platform.dropwizard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ft.platform.dropwizard.AdvancedResult.Status;

/**
 * Adds logging to the targeted check, normally logging is provided by the framework but in some
 * deployments part of the framework are not invoked.
 *
 * @author Simon.Gibbs
 */
public class LoggingAdvancedHealthCheck extends AdvancedHealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAdvancedHealthCheck.class);

    private final AdvancedHealthCheck target;

    public LoggingAdvancedHealthCheck(final AdvancedHealthCheck target) {
        super(target.getName());
        this.target = target;
    }

    @Override
    protected AdvancedResult checkAdvanced() throws Exception {
        final AdvancedResult rawResult = target.executeAdvanced();

        if (Status.OK.equals(rawResult.status())) {
            LOGGER.debug(String.format("checkId=\"%s\", checkName=\"%s\", checkOutput=\"%s\"",
                    this.id(), this.getName(), rawResult.checkOutput()));
        } else {
            LOGGER.warn(String.format("checkId=\"%s\", checkName=\"%s\", checkOutput=\"%s\"",
                    this.id(), this.getName(), rawResult.checkOutput()));
        }
        return rawResult;
    }

    @Override
    protected String id() {
        return target.id();
    }

    @Override
    protected int severity() {
        return target.severity();
    }

    @Override
    protected String businessImpact() {
        return target.businessImpact();
    }

    @Override
    protected String technicalSummary() {
        return target.technicalSummary();
    }

    @Override
    protected String panicGuideUrl() {
        return target.panicGuideUrl();
    }
}

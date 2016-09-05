package com.ft.platform.dropwizard;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * SyntheticFailingAdvancedHealthCheck
 *
 * @author Simon.Gibbs
 */
public class SyntheticFailingAdvancedHealthCheck extends AdvancedHealthCheck {

    private final String id;
    private final int severity;
    private final String message;

    public SyntheticFailingAdvancedHealthCheck(final String id, final String name, final int severity, final String message) {
        super(name);
        this.id = id;
        this.severity = severity;
        this.message = message;
    }

    @Override
    protected String id() {
        return id;
    }

    @Override
    protected AdvancedResult checkAdvanced() throws Exception {
        throw new RuntimeException(message);
    }

    @Override
    protected int severity() {
        return severity;
    }

    @Override
    protected String businessImpact() {
        return message;
    }

    @Override
    protected String technicalSummary() {
        return message;
    }

    @Override
    protected String panicGuideUrl() {
        try {
            return "http://example.com/?msg="+ URLEncoder.encode(message,"UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("An improbable issue occurred. That's surprising",e);
        }
    }
}

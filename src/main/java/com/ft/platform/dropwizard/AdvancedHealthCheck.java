package com.ft.platform.dropwizard;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Objects;

public abstract class AdvancedHealthCheck extends HealthCheck implements Comparable<AdvancedHealthCheck> {

    private final String name;

    protected AdvancedHealthCheck(String name) {
        this.name = name;
    }

    @Override
    protected final Result check() throws Exception {
        return checkAdvanced().asResult();
    }

    public final AdvancedResult executeAdvanced() {
        try {
            return checkAdvanced();
        } catch (Error e) {
            throw e;
        } catch (Throwable e) {
            return AdvancedResult.error(this, e);
        }
    }

    protected abstract AdvancedResult checkAdvanced() throws Exception;

    protected abstract int severity();

    protected abstract String businessImpact();

    protected abstract String technicalSummary();

    protected abstract String panicGuideUrl();

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(AdvancedHealthCheck other) {
        return getName().compareTo(other.getName());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this.getClass())
                .add("name", name)
                .add("severity", severity())
                .add("businessImpact", businessImpact())
                .add("technicalSummary", technicalSummary())
                .add("panicGuideUrl", panicGuideUrl())
                .toString();
    }

}

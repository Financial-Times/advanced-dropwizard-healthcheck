package com.ft.platform.dropwizard;

import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DefaultGoodToGoCheckerTest {

    private GoodToGoChecker checker;

    @Before
    public void init() {
        checker = new DefaultGoodToGoChecker(1);
    }

    @Test
    public void shouldReturnFalseWhenACheckIsError() {

        final Environment environment = new Environment("test-env", null, null, null,
                Thread.currentThread().getContextClassLoader());
        environment.healthChecks().register("test", new ErroringHealthCheck());

        assertThat(checker.runCheck(environment), is(new GoodToGoResult(false, "")));

    }

    @Test
    public void shouldReturnTrueWhenACheckIsWarn() {

        final Environment environment = new Environment("test-env", null, null, null,
                Thread.currentThread().getContextClassLoader());
        environment.healthChecks().register("test", new WarningHealthCheck());

        assertThat(checker.runCheck(environment), is(new GoodToGoResult(true, "")));

    }

    @Test
    public void shouldReturnTrueWhenACheckIsHealthy() {
        final Environment environment = new Environment("test-env", null, null, null,
                Thread.currentThread().getContextClassLoader());
        environment.healthChecks().register("test", new PassingHealthCheck());

        assertThat(checker.runCheck(environment), is(new GoodToGoResult(true, "")));

    }

    @Test
    public void shouldReturnTrueWithMixedResultsContainingNoError() {
        final Environment environment = new Environment("test-env", null, null, null,
                Thread.currentThread().getContextClassLoader());
        environment.healthChecks().register("test", new PassingHealthCheck());
        environment.healthChecks().register("test", new WarningHealthCheck());
        environment.healthChecks().register("test", new PassingHealthCheck());
        environment.healthChecks().register("test", new WarningHealthCheck());

        assertThat(checker.runCheck(environment), is(new GoodToGoResult(true, "")));
    }

    @Test
    public void shouldReturnFalseWithMixedResultsContainingAnError() {
        final Environment environment = new Environment("test-env", null, null, null,
                Thread.currentThread().getContextClassLoader());
        environment.healthChecks().register("test", new PassingHealthCheck());
        environment.healthChecks().register("test", new WarningHealthCheck());
        environment.healthChecks().register("test", new ErroringHealthCheck());
        environment.healthChecks().register("test", new WarningHealthCheck());

        assertThat(checker.runCheck(environment), is(new GoodToGoResult(true, "")));
    }

    @Test
    public void shouldReturnFalseWithErrorMessageWhenTimeout() {
        final Environment environment = new Environment("test-env", null, null, null,
                Thread.currentThread().getContextClassLoader());
        environment.healthChecks().register("test", new LongRunningHealthCheck());

        assertThat(checker.runCheck(environment), is(new GoodToGoResult(false, "Timed out after 1 second(s)")));
    }

    private abstract static class TestHealthCheck extends AdvancedHealthCheck {

        protected TestHealthCheck(final String name) {
            super(name);
        }

        @Override
        protected int severity() {
            return 1;
        }

        @Override
        protected String businessImpact() {
            return "impact";
        }

        @Override
        protected String technicalSummary() {
            return "tech summary";
        }

        @Override
        protected String panicGuideUrl() {
            return "don't panic";
        }

    }

    private static class ErroringHealthCheck extends TestHealthCheck {
        protected ErroringHealthCheck() {
            super(ErroringHealthCheck.class.getName());
        }

        @Override
        protected String id() {
            return "id";
        }

        @Override
        protected AdvancedResult checkAdvanced() throws Exception {
            return AdvancedResult.error(this, "error");
        }
    }

    private static class WarningHealthCheck extends TestHealthCheck {
        protected WarningHealthCheck() {
            super(WarningHealthCheck.class.getName());
        }

        @Override
        protected String id() {
            return "id";
        }

        @Override
        protected AdvancedResult checkAdvanced() throws Exception {
            return AdvancedResult.warn(this, "warn");
        }
    }

    private static class PassingHealthCheck extends TestHealthCheck {
        protected PassingHealthCheck() {
            super(PassingHealthCheck.class.getName());
        }

        @Override
        protected String id() {
            return "id";
        }

        @Override
        protected AdvancedResult checkAdvanced() throws Exception {
            return AdvancedResult.healthy();
        }
    }

    private static class LongRunningHealthCheck extends TestHealthCheck {
        protected LongRunningHealthCheck() {
            super(LongRunningHealthCheck.class.getName());
        }

        @Override
        protected String id() {
            return "id";
        }

        @Override
        protected AdvancedResult checkAdvanced() throws Exception {
            TimeUnit.SECONDS.sleep(2);
            return AdvancedResult.healthy();
        }
    }
}

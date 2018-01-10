package com.ft.platform.dropwizard;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;

import io.dropwizard.setup.Environment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DefaultGoodToGoCheckerTest {

    private static final int WAIT_IN_SECONDS = 2;
    private GoodToGoChecker checker;

    @Before
    public void init() {
        checker = new DefaultGoodToGoChecker(Executors.newSingleThreadExecutor(), 1);
    }

    @Test
    public void shouldReturnFalseWhenACheckIsError() {

        final Environment environment = new Environment("test-env", null, null, null,
                Thread.currentThread().getContextClassLoader());
        environment.healthChecks().register("test", new ErroringHealthCheck());

        assertThat(checker.runCheck(environment), is(new GoodToGoResult(false, "error")));
    }

    @Test
    public void shouldReturnTrueWhenACheckIsWarn() {

        final Environment environment = new Environment("test-env", null, null, null,
                Thread.currentThread().getContextClassLoader());
        environment.healthChecks().register("test", new WarningHealthCheck());

        assertThat(checker.runCheck(environment), is(new GoodToGoResult(true, "OK")));

    }

    @Test
    public void shouldReturnTrueWhenACheckIsHealthy() {
        final Environment environment = new Environment("test-env", null, null, null,
                Thread.currentThread().getContextClassLoader());
        environment.healthChecks().register("test", new PassingHealthCheck());

        assertThat(checker.runCheck(environment), is(new GoodToGoResult(true, "OK")));

    }

    @Test
    public void shouldReturnTrueWithMixedResultsContainingNoError() {
        final Environment environment = new Environment("test-env", null, null, null,
                Thread.currentThread().getContextClassLoader());
        environment.healthChecks().register("test-1", new PassingHealthCheck());
        environment.healthChecks().register("test-2", new WarningHealthCheck());
        environment.healthChecks().register("test-3", new PassingHealthCheck());
        environment.healthChecks().register("test-4", new WarningHealthCheck());

        assertThat(checker.runCheck(environment), is(new GoodToGoResult(true, "OK")));
    }

    @Test
    public void shouldReturnFalseWithMixedResultsContainingAnError() {
        final Environment environment = new Environment("test-env", null, null, null,
                Thread.currentThread().getContextClassLoader());
        environment.healthChecks().register("test-1", new PassingHealthCheck());
        environment.healthChecks().register("test-2", new WarningHealthCheck());
        environment.healthChecks().register("test-3", new ErroringHealthCheck());
        environment.healthChecks().register("test-4", new WarningHealthCheck());

        assertThat(checker.runCheck(environment), is(new GoodToGoResult(false, "error")));
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
        private int wait;

        protected LongRunningHealthCheck() {
          super(LongRunningHealthCheck.class.getName());
          this.wait = WAIT_IN_SECONDS;
        }

        protected LongRunningHealthCheck(int wait) {
          super(LongRunningHealthCheck.class.getName());
          this.wait = wait;
        }

        @Override
        protected String id() {
            return "id";
        }

        @Override
        protected AdvancedResult checkAdvanced() throws Exception {
            TimeUnit.SECONDS.sleep(wait);
            return AdvancedResult.healthy();
        }
    }
}

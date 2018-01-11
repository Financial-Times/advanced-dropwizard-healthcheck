package com.ft.platform.dropwizard;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.dropwizard.setup.Environment;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

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

        assertThat(checker.runCheck(environment), is(new GoodToGoResult(false, "Healthcheck \"ErroringHealthCheck\" failed. See /__health for more information.")));
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

        assertThat(checker.runCheck(environment), is(new GoodToGoResult(false, "Healthcheck \"ErroringHealthCheck\" failed. See /__health for more information.")));
    }

    @Test
    public void shouldReturnFalseWithErrorMessageWhenTimeout() {
        final Environment environment = new Environment("test-env", null, null, null,
                Thread.currentThread().getContextClassLoader());
        environment.healthChecks().register("test", new LongRunningHealthCheck());

        assertThat(checker.runCheck(environment), is(new GoodToGoResult(false, "Timed out after 1 second(s)")));
    }

    @Test
    public void shouldOnlyRunAMaximumOfThreeThreadsWithDefaults() throws Exception {
      final Environment environment = new Environment("test-env", null, null, null,
              Thread.currentThread().getContextClassLoader());

      environment.healthChecks().register("test", new PassingHealthCheck());
      int expectedThreadCount = Thread.activeCount() + 3; // There are three executors

      checker = new DefaultGoodToGoChecker(); // use the defaults to mimic actual use

      for(int i = 0 ; i <= 6 ; i++){
        assertThat(checker.runCheck(environment), is(new GoodToGoResult(true, "OK")));
      }

      TimeUnit.SECONDS.sleep(1); // allow the last worker thread to move to WAITING state

      assertThat("thread count", Thread.activeCount(), lessThanOrEqualTo(expectedThreadCount));
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
            super("ErroringHealthCheck");
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
            super("WarningHealthCheck");
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
            super("PassingHealthCheck");
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

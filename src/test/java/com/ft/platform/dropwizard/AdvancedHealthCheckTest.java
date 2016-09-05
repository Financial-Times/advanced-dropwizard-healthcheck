package com.ft.platform.dropwizard;

import com.ft.platform.dropwizard.system.Clock;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.util.SortedMap;

import static com.ft.platform.dropwizard.AdvancedResult.Status.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AdvancedHealthCheckTest {

    Environment environment;
    AdvancedHealthChecksRunner runner;

    @Before
    public void init() {
        environment = new Environment("test-env", null, null, null, Thread.currentThread().getContextClassLoader());
        runner = new AdvancedHealthChecksRunner(environment, "Test App", "A test application");
    }

    @Test
    public void can_register_and_execute_healthy_check() {
        environment.healthChecks().register("TestHealthCheck", new TestHealthCheck(OK));

        AdvancedResult result = runChecksAndReturnFirstResult();
        assertThat(result.status(), is(OK));
        assertThat(result.checkOutput(), is("It's all fine"));
    }

    @Test
    public void can_register_and_execute_warning_check() {
        environment.healthChecks().register("TestHealthCheck", new TestHealthCheck(WARN));

        AdvancedResult result = runChecksAndReturnFirstResult();
        assertThat(result.status(), is(WARN));
        assertThat(result.checkOutput(), is("Going a bit pear shaped"));
    }

    @Test
    public void can_register_and_execute_error_check() {
        environment.healthChecks().register("TestHealthCheck", new TestHealthCheck(ERROR));

        AdvancedResult result = runChecksAndReturnFirstResult();
        assertThat(result.status(), is(ERROR));
        assertThat(result.checkOutput(), allOf(containsString("Belly up!"), containsString("The IOs are broken")));
    }

    @Test
    public void can_register_and_execute_exception_throwing_check() {
        environment.healthChecks().register("TestHealthCheck", new ExceptionThrowingHealthCheck());

        AdvancedResult result = runChecksAndReturnFirstResult();
        assertThat(result.status(), is(ERROR));
        assertThat(result.checkOutput(), containsString("Something really very bad happened"));
    }

    @Test
    public void renders_checks_as_json() throws Exception {
        environment.healthChecks().register("TestHealthCheck", new TestHealthCheck(OK));
        Clock.fixClockAt("2013-12-20T09:51:00Z");

        HealthCheckPageData healthCheckPageData = runner.run();
        JSONAssert.assertEquals(
                        "{\n" +
                        "  \"schemaVersion\": 1,\n" +
                        "  \"name\": \"Test App\",\n" +
                        "  \"description\": \"A test application\",\n" +
                        "  \"checks\": [\n" +
                        "    {\n" +
                        "      \"name\": \"Testing123\",\n" +
                        "      \"ok\": true,\n" +
                        "      \"lastUpdated\": \"2013-12-20T09:51:00Z\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}", healthCheckPageData.toString(), false);
    }

    @Test
    public void summarises_status_correcly_when_all_ok() {
        environment.healthChecks().register("TestHealthCheck1", new TestHealthCheck("one", OK));
        environment.healthChecks().register("TestHealthCheck2", new TestHealthCheck("two", OK));
        environment.healthChecks().register("TestHealthCheck3", new TestHealthCheck("three", OK));

        HealthCheckPageData healthCheckPageData = runner.run();
        assertThat(healthCheckPageData.overallStatus(), is(OK));
    }

    @Test
    public void summarises_status_correcly_when_some_warnings() {
        environment.healthChecks().register("TestHealthCheck1", new TestHealthCheck("one", OK));
        environment.healthChecks().register("TestHealthCheck2", new TestHealthCheck("two", WARN));
        environment.healthChecks().register("TestHealthCheck3", new TestHealthCheck("three", OK));

        HealthCheckPageData healthCheckPageData = runner.run();
        assertThat(healthCheckPageData.overallStatus(), is(WARN));
    }

    @Test
    public void summarises_status_correcly_when_some_errors() {
        environment.healthChecks().register("TestHealthCheck1", new TestHealthCheck("one", OK));
        environment.healthChecks().register("TestHealthCheck2", new TestHealthCheck("two", WARN));
        environment.healthChecks().register("TestHealthCheck3", new TestHealthCheck("three", ERROR));

        HealthCheckPageData healthCheckPageData = runner.run();
        assertThat(healthCheckPageData.overallStatus(), is(ERROR));
    }

    private AdvancedResult runChecksAndReturnFirstResult() {
        SortedMap<AdvancedHealthCheck, AdvancedResult> results = HealthChecks.runAdvancedHealthChecksIn(environment);
        return results.values().iterator().next();
    }

    public static class TestHealthCheck extends AdvancedHealthCheck {

        private AdvancedResult.Status statusToProduce;

        public TestHealthCheck(AdvancedResult.Status status) {
            this("Testing123", status);
        }

        public TestHealthCheck(String name, AdvancedResult.Status status) {
            super(name);
            this.statusToProduce = status;
        }

        @Override
        protected AdvancedResult checkAdvanced() throws Exception {
            switch (statusToProduce) {
                case OK:
                    return AdvancedResult.healthy("It's all fine");
                case WARN:
                    return AdvancedResult.warn(this, "Going a bit pear shaped");
                case ERROR:
                     return AdvancedResult.error(this, "Belly up!", new IOException("The IOs are broken"));
                default:
                    return AdvancedResult.healthy();
            }

        }

        @Override
        protected int severity() {
            return 2;
        }

        @Override
        protected String businessImpact() {
            return "All the money will be lost";
        }

        @Override
        protected String technicalSummary() {
            return "The bank account is leaking";
        }

        @Override
        protected String panicGuideUrl() {
            return "http://panic.com/12-steps.html";
        }
    }

    public static class ExceptionThrowingHealthCheck extends TestHealthCheck {

        protected ExceptionThrowingHealthCheck() {
            super(ERROR);
        }

        @Override
        protected AdvancedResult checkAdvanced() throws Exception {
            throw new Exception("Something really very bad happened");
        }
    }

}

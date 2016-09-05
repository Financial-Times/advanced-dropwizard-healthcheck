package com.ft.platform.dropwizard;

import com.ft.platform.dropwizard.html.HTMLWrapperFilter;
import com.ft.platform.dropwizard.system.Clock;
import com.ft.platform.dropwizard.testsupport.ConfigPathBuilder;
import com.ft.platform.dropwizard.testsupport.HealthCheckTestConfig;
import com.ft.platform.dropwizard.testsupport.HealthCheckTestRootResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.servlet.DispatcherType;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.util.EnumSet;

import static com.ft.platform.dropwizard.AdvancedResult.Status.OK;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * LoggingAdvancedHealthCheckIntegrationTest
 *
 * @author Simon.Gibbs
 */
public class LoggingAdvancedHealthCheckIntegrationTest {

    @Rule
    public DropwizardAppRule<HealthCheckTestConfig> noLogging =
            new DropwizardAppRule<>(HealthCheckTestService.class, ConfigPathBuilder.DEFAULT_PATH);

    @Rule
    public DropwizardAppRule<HealthCheckTestConfig> withLogging =
            new DropwizardAppRule<>(LoggingHealthCheckTestService.class, ConfigPathBuilder.DEFAULT_PATH);

    Client client;

    @Before
    public void init() throws ParseException {

        Clock.fixClockAt("2015-06-22T11:05:00.000Z");

        client = noLogging.getConfiguration().buildJerseyClient(noLogging.getEnvironment());
    }


    @Test
    public void jsonShouldNotBeModifiedByAdditionalLoggingBehaviour() {

        Response noLoggingResult = getHealthCheckPage(noLogging);
        Response withLoggingResult = getHealthCheckPage(withLogging);

        assertThat(withLoggingResult.getStatus(), is(noLoggingResult.getStatus()));
        assertThat(withLoggingResult.getStringHeaders().getFirst("Content-Type"), is(noLoggingResult.getStringHeaders().getFirst("Content-Type")));
        assertThat(withLoggingResult.getStringHeaders().getFirst("Cache-Control"), is(noLoggingResult.getStringHeaders().getFirst("Cache-Control")));

        String withLoggingEntity = normaliseServiceName(withLoggingResult.readEntity(String.class));

        assertThat(withLoggingEntity, is(noLoggingResult.readEntity(String.class)));
    }


    @Test
    public void htmlViewsShouldNotBeModifiedByAdditionalLoggingBehaviour() {

        Response noLoggingResult = getHealthCheckHtmlPage(noLogging);
        Response withLoggingResult = getHealthCheckHtmlPage(withLogging);

        assertThat(withLoggingResult.getStatus(), is(noLoggingResult.getStatus()));
        assertThat(withLoggingResult.getStringHeaders().getFirst("Content-Type"), is(noLoggingResult.getStringHeaders().getFirst("Content-Type")));
        assertThat(withLoggingResult.getStringHeaders().getFirst("Cache-Control"), is(noLoggingResult.getStringHeaders().getFirst("Cache-Control")));
        String withLoggingEntity = normaliseServiceName(withLoggingResult.readEntity(String.class));

        assertThat(withLoggingEntity, is(noLoggingResult.readEntity(String.class)));
    }

    private String normaliseServiceName(String response) {
        return response.replace(LoggingHealthCheckTestService.class.getSimpleName(), HealthCheckTestService.class.getSimpleName());
    }

    public static abstract class AbstractTestService  extends Application<HealthCheckTestConfig> {
        @Override
        public void initialize(Bootstrap<HealthCheckTestConfig> bootstrap) {
            bootstrap.addBundle(new AdvancedHealthCheckBundle());
        }

        @Override
        public void run(HealthCheckTestConfig configuration, Environment environment) throws Exception {
            environment.jersey().register(new HealthCheckTestRootResource());
            environment.servlets().addFilter("",new HTMLWrapperFilter("test")).addMappingForUrlPatterns(
                    EnumSet.of(DispatcherType.REQUEST), false, "/__healthcheck.html");

            configureChecks(environment);
        }

        public abstract void configureChecks(Environment environment);
    }

    public static class HealthCheckTestService extends AbstractTestService {

        public void configureChecks(Environment environment) {
            environment.healthChecks().register("TestAdvancedHealthCheck", new TestAdvancedHealthCheck(OK));
        }
    }

    public static class LoggingHealthCheckTestService extends AbstractTestService {

        public void configureChecks(Environment environment) {
            environment.healthChecks().register("TestAdvancedHealthCheck", new LoggingAdvancedHealthCheck(new TestAdvancedHealthCheck(OK)));
        }
    }

    public static class TestAdvancedHealthCheck extends AdvancedHealthCheck {

        private final AdvancedResult.Status status;

        protected TestAdvancedHealthCheck(AdvancedResult.Status status) {
            super("Advanced Check Test");
            this.status = status;
        }

        @Override
        protected AdvancedResult checkAdvanced() throws Exception {
            switch (status) {
                case OK: return AdvancedResult.healthy("Everything is fine");
                case WARN: return AdvancedResult.warn(this, "Hmmm...not looking so good");
            default:
                return AdvancedResult.error(this, "The audit host's RAID array is on fire");
            }
        }

        @Override
        protected String id() {
            return "testfield";
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

    private Response getHealthCheckPage(DropwizardAppRule<HealthCheckTestConfig> rule) {
        return client.target(url("/__health", rule)).request().get();
    }

    private Response getHealthCheckHtmlPage(DropwizardAppRule<HealthCheckTestConfig> rule) {
        return client.target(url("/__health.html", rule)).request().get();
    }

    private String url(String pathAndQuery, DropwizardAppRule<HealthCheckTestConfig> rule) {
        return "http://localhost:" + rule.getLocalPort() + pathAndQuery;
    }

}

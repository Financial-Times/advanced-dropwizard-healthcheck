package com.ft.platform.dropwizard;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.platform.dropwizard.html.HTMLWrapperFilter;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.servlet.DispatcherType;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.File;
import java.util.EnumSet;

import static com.ft.platform.dropwizard.AdvancedResult.Status.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AdvancedHealthCheckIntegrationTest {

    @Rule
    public DropwizardAppRule<HealthCheckTestConfig> app = new DropwizardAppRule<>(HealthCheckTestService.class, testYamlPath());

    Client client;

    @Before
    public void init() {
        client = app.getConfiguration().buildJerseyClient(app.getEnvironment());
    }

    @Test
    public void shouldReturn200WhenCheckIsOK() {
        TestAdvancedHealthCheck.status = OK;
        ClientResponse result = getHealthCheckPage();

        assertThat(result.getStatus(), is(200));
        assertThat(result.getHeaders().getFirst("Content-Type"), is("application/json"));
        assertThat(result.getHeaders().getFirst("Cache-Control"), containsString("no-cache"));
        assertThat(result.getEntity(String.class), containsString("ok"));
    }

    @Test
    public void shouldReturn200WhenHtmlCheckIsOK() {
        TestAdvancedHealthCheck.status = OK;
        ClientResponse result = getHealthCheckHtmlPage();

        assertThat(result.getStatus(), is(200));
        assertThat(result.getHeaders().getFirst("Content-Type"), is("text/html; charset=UTF-8"));
        assertThat(result.getHeaders().getFirst("Cache-Control"), containsString("no-cache"));
        String entity = result.getEntity(String.class);
        assertThat(entity, containsString("ok"));
        assertThat(entity, containsString("\"technicalSummary\":\"The audit system has failed\""));
        assertThat(entity, containsString("\"businessImpact\":\"The CEO will go to prison\""));
    }

    @Test
    public void shouldReturn200WhenCheckIsWARN() {
        TestAdvancedHealthCheck.status = WARN;
        ClientResponse result = getHealthCheckPage();

        assertThat(result.getStatus(), is(200));
        assertThat(result.getHeaders().getFirst("Cache-Control"), containsString("no-cache"));
        assertThat(result.getEntity(String.class), containsString("Hmmm...not looking so good"));
    }

    @Test
    public void shouldReturn200WhenCheckIsERROR() {
        TestAdvancedHealthCheck.status = ERROR;
        ClientResponse result = getHealthCheckPage();

        assertThat(result.getStatus(), is(200));
        String entity = result.getEntity(String.class);
        assertThat(entity, containsString("The audit host's RAID array is on fire"));
        assertThat(entity, containsString("\"technicalSummary\":\"The audit system has failed\""));
        assertThat(entity, containsString("\"businessImpact\":\"The CEO will go to prison\""));
    }

    private ClientResponse getHealthCheckPage() {
        return client.resource(url("/__health")).get(ClientResponse.class);
    }

    private ClientResponse getHealthCheckHtmlPage() {
        return client.resource(url("/__health.html")).get(ClientResponse.class);
    }

    private String url(String pathAndQuery) {
        return "http://localhost:" + app.getLocalPort() + pathAndQuery;
    }

    static String testYamlPath() {
        try {
            return new File(Resources.getResource("test.yaml").toURI()).getCanonicalPath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class HealthCheckTestConfig extends Configuration {

        @JsonProperty
        private String dummy = "";

        @JsonProperty
        private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

        public Client buildJerseyClient(Environment environment) {
            return new JerseyClientBuilder(environment).using(jerseyClient).using(environment).build("TestJersey");
        }
    }

    public static class HealthCheckTestService extends Application<HealthCheckTestConfig> {

        @Override
        public void initialize(Bootstrap<HealthCheckTestConfig> bootstrap) {
            bootstrap.addBundle(new AdvancedHealthCheckBundle());
        }

        @Override
        public void run(HealthCheckTestConfig configuration, Environment environment) throws Exception {
            environment.jersey().register(new HealthCheckTestRootResource());
            environment.servlets().addFilter("",new HTMLWrapperFilter("test")).addMappingForUrlPatterns(
                    EnumSet.of(DispatcherType.REQUEST), false, "/__healthcheck.html");

            environment.healthChecks().register("TestAdvancedHealthCheck", new TestAdvancedHealthCheck());
        }
    }

    public static class TestAdvancedHealthCheck extends AdvancedHealthCheck {

        public static AdvancedResult.Status status = OK;

        protected TestAdvancedHealthCheck() {
            super("Advanced Check Test");
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

    public static class OrdinaryHealthCheck extends HealthCheck {

        @Override
        protected HealthCheck.Result check() throws Exception {
            return Result.healthy();
        }
    }

    @Path("/")
    public static class HealthCheckTestRootResource {

        @GET
        public String root() {
            return "Test for advanced health checks";
        }

    }
}

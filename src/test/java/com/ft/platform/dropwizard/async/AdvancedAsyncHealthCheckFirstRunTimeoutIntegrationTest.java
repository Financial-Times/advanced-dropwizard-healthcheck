package com.ft.platform.dropwizard.async;

import com.ft.platform.dropwizard.testsupport.ConfigPathBuilder;
import com.ft.platform.dropwizard.testsupport.HealthCheckTestConfig;
import com.ft.platform.dropwizard.testsupport.HealthCheckTestService;
import com.ft.platform.dropwizard.testsupport.TestAdvancedHealthCheck;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static com.ft.platform.dropwizard.AdvancedResult.Status.OK;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AdvancedAsyncHealthCheckFirstRunTimeoutIntegrationTest {

    @Rule
    public final DropwizardAppRule<HealthCheckTestConfig> app =
        new DropwizardAppRule<>(
            TestService.class,
            ConfigPathBuilder.DEFAULT_PATH,
            ConfigOverride.config("asyncSchedule.startDelay", "10"),
            ConfigOverride.config("asyncSchedule.startupWaitTime", "0")
        );

    Client client;

    @Before
    public void init() {
        client = app.getConfiguration().buildJerseyClient(app.getEnvironment());
    }

    @Test
    public void shouldReturnNotCompleteWhenAccessedBeforeRunIsFinished() {
        TestAdvancedHealthCheck.status = OK;
        final Response result = getHealthCheckPage();

        assertThat(result.getStatus(), is(200));
        assertThat(result.getStringHeaders().getFirst("Content-Type"), is("application/json"));
        assertThat(result.getStringHeaders().getFirst("Cache-Control"), containsString("no-cache"));
        assertThat(result.readEntity(String.class), containsString("First run not complete"));
    }

    private Response getHealthCheckPage() {
        return client.target(url("/__health")).request().get();
    }

    private String url(final String pathAndQuery) {
        return "http://localhost:" + app.getLocalPort() + pathAndQuery;
    }

    public static class TestService extends HealthCheckTestService {
        @Override
        public void initialize(final Bootstrap<HealthCheckTestConfig> bootstrap) {
            bootstrap.addBundle(new AdvancedAsyncHealthCheckBundle());
        }
    }


}
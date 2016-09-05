package com.ft.platform.dropwizard;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class GoodToGoCheckIntegrationTest {

	@Rule
	public DropwizardAppRule<HealthCheckTestConfig> app = new DropwizardAppRule<>(
			GoodToGoTestService.class, testYamlPath());

	Client client;

	@Before
	public void init() {
		client = app.getConfiguration().buildJerseyClient(app.getEnvironment());
	}

	@Test
	public void shouldReturn200WhenCheckIsOK() {
		FixedResponseChecker.response = true;
		ClientResponse result = getHealthCheckPage();

		assertThat(result.getStatus(), is(200));
		assertThat(result.getHeaders().getFirst("Content-Type"),
				is("application/json"));
		assertThat(result.getHeaders().getFirst("Cache-Control"),
				containsString("no-cache"));
	}

	@Test
	public void shouldReturn503WhenCheckIsNotOK() {
		FixedResponseChecker.response = false;
		ClientResponse result = getHealthCheckPage();

		assertThat(result.getStatus(), is(503));
	}

	private ClientResponse getHealthCheckPage() {
		return client.resource(url("/__gtg")).get(ClientResponse.class);
	}

	private String url(String pathAndQuery) {
		return "http://localhost:" + app.getLocalPort() + pathAndQuery;
	}

	static String testYamlPath() {
		try {
			return new File(Resources.getResource("test.yaml").toURI())
					.getCanonicalPath();
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
			return new JerseyClientBuilder(environment).using(jerseyClient)
					.using(environment).build("TestJersey");
		}
	}

	public static class GoodToGoTestService extends
			Application<HealthCheckTestConfig> {
		
		@Override
		public void initialize(Bootstrap<HealthCheckTestConfig> bootstrap) {
			bootstrap.addBundle(new GoodToGoBundle(new FixedResponseChecker()));
		}

		@Override
		public void run(HealthCheckTestConfig configuration,
				Environment environment) throws Exception {
            environment.jersey().register(new GoodToGoTestRootResource());
		}
	}

	
	public static class FixedResponseChecker implements GoodToGoChecker {

		private static boolean response;

		@Override
		public boolean isGoodToGo(Environment env) {
			return response;
		}

	}
	
    @Path("/")
    public static class GoodToGoTestRootResource {

        @GET
        public String root() {
            return "Test for __gtg endpoint";
        }

    }
}

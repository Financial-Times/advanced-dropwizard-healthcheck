package com.ft.platform.dropwizard;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class GoodToGoConfiguredBundle implements ConfiguredBundle<ConfigWithGTG> {

	private final GoodToGoChecker checker;

	public GoodToGoConfiguredBundle(GoodToGoChecker checker) {
		this.checker = checker;
	}

	@Override
	public void run(ConfigWithGTG configuration, Environment environment) throws Exception {
		GoodToGoServlet servlet = new GoodToGoServlet(checker, environment, configuration.getGtg());
		environment.servlets().addServlet("GoodToGoServlet", servlet)
				.addMapping("/__gtg");
	}

	@Override
	public void initialize(Bootstrap<?> bootstrap) {
	}

}

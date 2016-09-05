package com.ft.platform.dropwizard;

import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Use GoodToGoConfiguredBundle instead, note this will give you a default response body of "OK" and content type of
 * text/plain for ok responses by default.
 */
@Deprecated()
public class GoodToGoBundle implements Bundle {

	private final GoodToGoChecker checker;

	public GoodToGoBundle(GoodToGoChecker checker) {
		this.checker = checker;
	}

	@Override
	public void initialize(Bootstrap<?> bootstrap) {
	}

	@Override
	public void run(Environment environment) {
		GoodToGoServlet servlet = new GoodToGoServlet(
				checker,
				environment,
				new GTGConfig("", "application/json") //for backwards compatibility
		);
		environment.servlets().addServlet("GoodToGoServlet", servlet)
				.addMapping("/__gtg");
	}
}

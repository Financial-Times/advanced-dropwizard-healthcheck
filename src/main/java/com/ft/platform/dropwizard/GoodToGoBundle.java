package com.ft.platform.dropwizard;

import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

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
		GoodToGoServlet servlet = new GoodToGoServlet(checker, environment);
		environment.servlets().addServlet("GoodToGoServlet", servlet)
				.addMapping("/__gtg");
	}
}

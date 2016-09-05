package com.ft.platform.dropwizard;

import io.dropwizard.setup.Environment;

/**
 * This implementation of {@link GoodToGoChecker} is based solely on the health
 * check responses. One or more error results implies not good to go. Otherwise
 * we're considered good.
 */
public class DefaultGoodToGoChecker implements GoodToGoChecker {

	@Override
	public boolean isGoodToGo(Environment environment) {
		for (AdvancedResult result : HealthChecks.runAdvancedHealthChecksIn(environment).values()) {
			if (result.status() == AdvancedResult.Status.ERROR) {
				return false;
			}
		}
		return true;
	}

}

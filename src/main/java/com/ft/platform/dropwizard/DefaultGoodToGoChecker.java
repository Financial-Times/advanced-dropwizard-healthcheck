package com.ft.platform.dropwizard;

import io.dropwizard.setup.Environment;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This implementation of {@link GoodToGoChecker} is based solely on the health
 * check responses. One or more error results implies not good to go. Otherwise
 * we're considered good.
 */
public class DefaultGoodToGoChecker implements GoodToGoChecker {

	private static final int DEFAULT_TIMEOUT = 3;
	private int timeOut;

	public DefaultGoodToGoChecker(int timeOut) {
		this.timeOut = timeOut;
	}

	public DefaultGoodToGoChecker() {
		this.timeOut = DEFAULT_TIMEOUT;
	}

	public GoodToGoResult runCheck(Environment environment) {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		try {
			return new GoodToGoResult(executorService.submit(() -> {
				for (AdvancedResult result : HealthChecks.runAdvancedHealthChecksIn(environment).values()) {
					if (result.status() == AdvancedResult.Status.ERROR) {
						return false;
					}
				}
				return true;
			}).get(this.timeOut, TimeUnit.SECONDS), "");
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			return new GoodToGoResult(false, "Timeout running status check");
		}
	}

}

package com.ft.platform.dropwizard;

import io.dropwizard.setup.Environment;

public interface GoodToGoChecker {
	GoodToGoResult runCheck(Environment env);
}

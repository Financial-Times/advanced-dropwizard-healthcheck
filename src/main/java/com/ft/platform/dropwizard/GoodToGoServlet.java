package com.ft.platform.dropwizard;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;
import static java.nio.charset.StandardCharsets.US_ASCII;

import io.dropwizard.setup.Environment;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class GoodToGoServlet extends HttpServlet {
    private static final String ASCII = US_ASCII.name();
    
	private final GoodToGoChecker checker;
	private final Environment environment;
	private GTGConfig gtgConfig;

	public GoodToGoServlet(GoodToGoChecker checker, Environment environment, GTGConfig gtgConfig) {
		this.checker = checker;
		this.environment = environment;
		this.gtgConfig = gtgConfig;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
	    
	    resp.setCharacterEncoding(ASCII);
		resp.setContentType(gtgConfig.getContentType());
		resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");

		if (checker.isGoodToGo(environment)) {
			resp.setStatus(SC_OK);
			resp.getWriter().append(gtgConfig.getOkBody());
		} else {
			resp.setStatus(SC_SERVICE_UNAVAILABLE);
		}

		resp.getWriter().close();
	}
}

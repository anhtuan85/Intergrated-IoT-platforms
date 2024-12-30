package com.kmk.app;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Main {
	public static void main(String[] args) {
		Server server = new Server(8081);
		String uriArr[] = new String[] { "http://192.168.0.3:8080/deploy", "http://192.168.0.3:8081/deploy", "http://192.168.0.3:8082/deploy" };
		server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", -1);
		ServletContextHandler context = new ServletContextHandler(server, "");
		context.addServlet(new ServletHolder(new DroolsCommonProxy(uriArr)), "/proxy");
		context.addServlet(new ServletHolder(new DroolsCommonRuleDeployer()), "/deploy");
		context.addServlet(new ServletHolder(new ActionHandler()), "/action");
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

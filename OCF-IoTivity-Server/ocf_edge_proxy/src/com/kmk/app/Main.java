package com.kmk.app;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.iotivity.OCBufferSettings;
import org.iotivity.OCMain;

public class Main {
	static private Thread mainThread;
	static private Thread shutdownHook = new Thread() {
		public void run() {
			System.out.println("Calling main_shutdown.");
			OCMain.mainShutdown();
			mainThread.interrupt();
		}
	};

	public static void main(String[] args) {
		System.out.println(MyUtil.L + "OCF Rule Proxy");
		mainThread = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(shutdownHook);
		OCBufferSettings.setMaxAppDataSize(1000000);
		String uriArr[] = new String[] { "http://192.168.0.3:8080/deploy", "http://192.168.0.3:8081/deploy", "http://localhost:8082/deploy" };
		int init_ret = OCMain.mainInit(new OcfCommonProxy("/proxy", uriArr));
		if (init_ret < 0) {
			System.exit(init_ret);
		}

		Server server = new Server(8082);
		server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", -1);
		ServletContextHandler context = new ServletContextHandler(server, "");
		context.addServlet(new ServletHolder(new OcfCommonRuleDeployer()), "/deploy");
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

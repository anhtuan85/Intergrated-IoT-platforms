package com.kmk.app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.json.JSONObject;
import com.sun.net.httpserver.HttpServer;
import kr.re.keti.ncube.Registration;
import kr.re.keti.ncube.httpserver.HttpServerHandler;
import kr.re.keti.ncube.httpserver.HttpServerTestHandler;
import kr.re.keti.ncube.mqttclient.MqttClientKetiSub;
import kr.re.keti.ncube.resource.AE;
import kr.re.keti.ncube.resource.CSEBase;
import kr.re.keti.ncube.resource.ResourceRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DroolsRulesEngineApplication {
	private static CSEBase hostingCSE = new CSEBase();
	private static AE hostingAE = new AE();
	private static InetAddress ip;
	private static boolean windows = true;
	public static MqttClientKetiSub requestClient;

	private static void configurationFileLoader() throws Exception {
		System.out.println("[&CubeThyme] Configuration file loading...");

		String jsonString = "";
		BufferedReader br = new BufferedReader(new FileReader("./conf.json"));
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;
			jsonString += line;
		}
		br.close();

		JSONObject conf = new JSONObject(jsonString);

		JSONObject cseObj = conf.getJSONObject("cse");
		hostingCSE.CSEHostAddress = cseObj.getString("cbhost");
		System.out.println("[&CubeThyme] CSE - cbhost : " + hostingCSE.CSEHostAddress);
		hostingCSE.CSEPort = cseObj.getString("cbport");
		System.out.println("[&CubeThyme] CSE - cbport : " + hostingCSE.CSEPort);
		hostingCSE.CSEName = cseObj.getString("cbname");
		System.out.println("[&CubeThyme] CSE - cbname : " + hostingCSE.CSEName);
		hostingCSE.CSEId = cseObj.getString("cbcseid");
		System.out.println("[&CubeThyme] CSE - cbcseid : " + hostingCSE.CSEId);
		hostingCSE.mqttPort = cseObj.getString("mqttport");
		System.out.println("[&CubeThyme] CSE - mqttPort : " + hostingCSE.mqttPort);
		ResourceRepository.setCSEBaseInfo(hostingCSE);

		JSONObject aeObj = conf.getJSONObject("ae");
		hostingAE.aeId = aeObj.getString("aeid");
		System.out.println("[&CubeThyme] AE - aeId : " + hostingAE.aeId);
		hostingAE.appId = aeObj.getString("appid");
		System.out.println("[&CubeThyme] AE - appid : " + hostingAE.appId);
		hostingAE.appName = aeObj.getString("appname");
		System.out.println("[&CubeThyme] AE - appname : " + hostingAE.appName);
		hostingAE.appPort = aeObj.getString("appport");
		System.out.println("[&CubeThyme] AE - appport : " + hostingAE.appPort);
		hostingAE.bodyType = aeObj.getString("bodytype");
		System.out.println("[&CubeThyme] AE - bodytype : " + hostingAE.bodyType);
		hostingAE.tasPort = aeObj.getString("tasport");
		System.out.println("[&CubeThyme] AE - tasport : " + hostingAE.tasPort);
		ResourceRepository.setAEInfo(hostingAE);
	}

	public static void main(String[] args) throws Exception {
		System.out.println("[&CubeThyme] &CubeThyme SW start.......\n");
		// Load the &CubeThyme configuration file
		configurationFileLoader();
		// Initialize the HTTP server for receiving the notification messages
		System.out.println("[&CubeThyme] &CubeThyme initialize.......\n");

		if (windows) {
			HttpServer server = HttpServer.create(new InetSocketAddress(Integer.parseInt(hostingAE.appPort)), 0);
			server.createContext("/", new HttpServerTestHandler()); // HTTP server test url
			server.createContext("/notification", new HttpServerHandler()); // oneM2M notification url
			server.setExecutor(null); // creates a default executor
			server.start();
		} else {
			InetSocketAddress serverSocketAddress = new InetSocketAddress(ip.getHostAddress(), Integer.parseInt(hostingAE.appPort));
			HttpServer server = HttpServer.create(serverSocketAddress, 0);
			server.createContext("/", new HttpServerTestHandler()); // HTTP server test url
			server.createContext("/notification", new HttpServerHandler()); // oneM2M notification url
			server.setExecutor(null); // creates a default executor
			server.start();
		}

		// Registration sequence
		Registration regi = new Registration();
		regi.registrationStart();

		requestClient = new MqttClientKetiSub("tcp://192.168.0.3"); // subscribe
		requestClient.subscribe("/oneM2M/req/+/+/json");

		SpringApplication.run(DroolsRulesEngineApplication.class, args);
	}
}

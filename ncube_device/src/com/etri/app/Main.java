package com.etri.app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpServer;
import com.fasterxml.jackson.databind.JsonNode;

import kr.re.keti.ncube.Registration;
import kr.re.keti.ncube.httpserver.HttpServerHandler;
import kr.re.keti.ncube.httpserver.HttpServerTestHandler;
import kr.re.keti.ncube.mqttclient.MqttClientKetiPub;
import kr.re.keti.ncube.mqttclient.MqttClientKetiSub;
import kr.re.keti.ncube.resource.AE;
import kr.re.keti.ncube.resource.CSEBase;
import kr.re.keti.ncube.resource.Container;
import kr.re.keti.ncube.resource.ResourceRepository;
import kr.re.keti.ncube.resource.Subscription;
import kr.re.keti.ncube.tasserver.TasServer;
import java.util.Date;  
public class Main {

	private static CSEBase hostingCSE = new CSEBase();
	private static AE hostingAE = new AE();
	private static ArrayList<Container> containers = new ArrayList<Container>();
	private static ArrayList<Subscription> subscriptions = new ArrayList<Subscription>();
	private static InetAddress ip;

	private static boolean windows = true;

	public static final MqttClientKetiSub requestClient;
	public static final MqttClientKetiSub responseClient;
	public static final MqttClientKetiPub publishClient;

	/**
	 * configurationFileLoader Method Load the XML profile named 'thyme_conf.xml'
	 * from local storage for create the AE and container resources.
	 * 
	 * @throws Exception
	 */
	private static void configurationFileLoader() {

		System.out.println("[&CubeThyme] Configuration file loading...");

		String jsonString = "";
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

		JSONArray cntArr = conf.getJSONArray("cnt");
		for (int i = 0; i < cntArr.length(); i++) {
			Container tempContainer = new Container();

			tempContainer.parentpath = cntArr.getJSONObject(i).getString("parentpath");
			System.out.println("[&CubeThyme] Container - parentpath : " + tempContainer.parentpath);
			tempContainer.ctname = cntArr.getJSONObject(i).getString("ctname");
			System.out.println("[&CubeThyme] Container - ctname : " + tempContainer.ctname);

			containers.add(tempContainer);
		}
		ResourceRepository.setContainersInfo(containers);

		JSONArray subArr = conf.getJSONArray("sub");
		for (int i = 0; i < subArr.length(); i++) {
			Subscription tempSubscription = new Subscription();

			tempSubscription.parentpath = subArr.getJSONObject(i).getString("parentpath");
			System.out.println("[&CubeThyme] Subscription - parentpath : " + tempSubscription.parentpath);
			tempSubscription.subname = subArr.getJSONObject(i).getString("subname");
			System.out.println("[&CubeThyme] Subscription - subname : " + tempSubscription.subname);
			tempSubscription.nu = subArr.getJSONObject(i).getString("nu");
			System.out.println("[&CubeThyme] Subscription - nu : " + tempSubscription.nu);

			subscriptions.add(tempSubscription);
		}
		ResourceRepository.setSubscriptionsInfo(subscriptions);

	}

	/**
	 * main Method
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) {

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

//		// Registration sequence
		Registration regi = new Registration();
		regi.registrationStart();

		// TODO send data
		String dataJson = requestGet("http://192.168.0.50:5000/getdata");
		JsonNode jsonNode =null;
		ObjectMapper objectMapper = new ObjectMapper();
		String pm25;
		String pm10;
		String humidity;
		String data;
		String reponseStr = "";
		try {
			jsonNode = objectMapper.readTree(dataJson);
			pm25 = jsonNode.get("pm2.5").asText();
			pm10 = jsonNode.get("pm10").asText();
			humidity = jsonNode.get("humidity").asText();
			data = pm25 + " " + pm10 + " " + humidity;
			System.out.println(data);
			String oneM2M_uri = "http://192.168.0.3:7579/Mobius";
			String ae = "device01";
			String cnt = "data";
			reponseStr = toOnem2m(oneM2M_uri, ae, cnt, data);
			System.out.println(reponseStr);
		} catch (IOException e) {
			System.out.print(e.getMessage());
		}



		// String data = MyUtil.getBufferedString("data/temperature_data");
		// data = data.replace("\n", "\\n");
		// String reponseStr = "";
		
		// String oneM2M_uri = "http://192.168.0.3:7579/Mobius";
		// String ae = "device01";
		// String cnt = "data";
		// reponseStr = toOnem2m(oneM2M_uri, ae, cnt, data);

		
		
		// System.out.println("result : " + reponseStr);
		// System.out.println(reponseStr);
	}

	public static String requestPostJson(String uri, String json) {
		StringBuffer reponseStr = new StringBuffer();
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost(uri);
		try {
			request.setHeader("Accept", "application/json");
			request.setHeader("X-M2M-RI", "12345");
			request.setHeader("X-M2M-Origin", "SOrigin");
			request.setHeader("Content-type", "application/vnd.onem2m-res+json?ty=4");
			request.setEntity(new StringEntity(json));
			CloseableHttpResponse response;
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			System.out.print(e.getMessage());
		} catch (IOException e) {
			System.out.print(e.getMessage());
		}
		return reponseStr.toString();
	}

	private static String requestGet(String uri) {
		StringBuffer reponseStr = new StringBuffer();
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(uri);
		return reponseStr.toString();
	}
	
	private static String toOnem2m(String onem2m_uri,String ae, String cnt, String value) {
		long onem2m_time = new Date().getTime();
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode payload = objectMapper.createObjectNode();
		ObjectNode cin = objectMapper.createObjectNode();
		cin.put("con", value);
		payload.set("m2m:cin", cin);
		System.out.println(MyUtil.L + "toOnem2m()");
		System.out.println(payload.toPrettyString());
		String result = "";
		
		
		System.out.println(onem2m_uri + "/" + ae + "/" + cnt);
		result = requestPostJson(onem2m_uri + "/" + ae + "/" + cnt, payload.toString());
		long rtt = new Date().getTime() - onem2m_time;
		System.out.println("oneM2M RTT : ");
		System.out.println(rtt);
		
		
//		System.out.println(MyUtil.L + "toOnem2m()result : " + result);
		return result;
	}
}
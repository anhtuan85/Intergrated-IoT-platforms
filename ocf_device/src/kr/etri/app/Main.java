package kr.etri.app;

import org.iotivity.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.kmk.app.MyOcfClient.ResponseVo;
import java.io.IOException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import java.util.Date; 
public class Main {
	public static final String L = "-------------------------------";
	static int i = 0;

	public static void main(String argv[]) {
		OCBufferSettings.setMaxAppDataSize(1000000);
		int init_ret = OCMain.mainInit(new OCMainInitHandler() {
			@Override
			public int initialize() {
				System.out.println("-----------------------------------------------initiliaze()");
				int ret = OCMain.initPlatform("jnu");
				ret |= OCMain.addDevice("/oic/d", "oic.d.iot", "app", "ocf.0.0.0", "ocf.res.0.0.0");
				return ret;
			}

			@Override
			public void registerResources() {
				System.out.println("-----------------------------------------------registerResources()");
			}

			@Override
			public void requestEntry() {
				System.out.println("-----------------------------------------------requestEntry()");
			}
		});

		if (init_ret < 0) {
			System.exit(init_ret);
		}
		// String value = MyUtil.getBufferedString("data/data01");
		// ObjectNode payload = objectMapper.createObjectNode();
		// String ocf_uri = "coap://192.168.0.3:12345";
		// String response;
		// response = toOcf(ocf_uri, "device02", "data", value);

		String dataJson = requestGet("http://192.168.0.48:5000/getdata");
		JsonNode jsonNode =null;
		ObjectMapper objectMapper = new ObjectMapper();
		String temp = "";
		String pressure = "";
		String data;
		try {
			jsonNode = objectMapper.readTree(dataJson);
			temp = jsonNode.get("temp").asText();
			System.out.println(temp);
			String ocf_uri = "coap://192.168.0.3:12345";
			String response;
			response = toOcf(ocf_uri, "device02", "data", temp);
		} catch (IOException e) {
			System.out.print(e.getMessage());
		}
//		payload.put("device", "device01");
//		payload.put("deviceprofile", "deviceprofile01");
//		payload.put("resource", "data");
//		payload.put("value", value);
		
//		long time = new Date().getTime();
//		ResponseVo responseVo = new MyOcfClient().doReqeust("coap://192.168.0.12:12345", "/device/proxy", null, payload.toString(), OCMethod.OC_POST);
//		long rtt = new Date().getTime() - time;
//		System.out.println("OCF RTT : ");
//		System.out.println(rtt);
		
		
//		String ocf_uri = "coap://192.168.0.3:12345";
//		String response;
//		response = toOcf(ocf_uri, "device02", "data", value);
//		System.out.println(L + "code : " + responseVo.getCode());
//		System.out.println(L + "payload : " + responseVo.getResultJsonStr());
	}
	
	private static String toOcf(String ocf_uri, String device, String resource, String value) {
		long ocf_time = new Date().getTime();
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode sensorPayload = objectMapper.createObjectNode();
		sensorPayload.put("value", value);
		// sensorPayload.put("pm10", value.get("pm10").asText());
		sensorPayload.put("deviceName",device);
		System.out.println(MyUtil.L + "toOcf()");
		// System.out.println(sensorPayload.toPrettyString());
		
		ResponseVo responseVo = new MyOcfClient().doReqeust(ocf_uri, "/" + resource, "", sensorPayload.toString(), OCMethod.OC_POST);
		long rtt = new Date().getTime() - ocf_time;
		System.out.println("OCF RTT : ");
		System.out.println(rtt);
		
		System.out.println(MyUtil.L + "toOcf()code : " + responseVo.getCode());
		return responseVo.getCode();
	}

	private static String requestGet(String uri) {
		StringBuffer reponseStr = new StringBuffer();
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(uri);
		try {
			CloseableHttpResponse response;
			response = client.execute(request);
			
			while ((line = bufferedReader.readLine()) != null) {
				reponseStr.append(line);
			}
		} catch (ClientProtocolException e) {
			System.out.print(e.getMessage());
		} catch (IOException e) {
			System.out.print(e.getMessage());
		}
		return reponseStr.toString();
	}
}

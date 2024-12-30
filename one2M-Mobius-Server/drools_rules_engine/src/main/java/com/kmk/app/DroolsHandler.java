package com.kmk.app;

import org.kie.api.runtime.KieSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DroolsHandler {
	public void handleEvent(String payload) {
		System.out.println("DroolsHandler.handleEvent :");
		System.out.println(payload);
		ObjectMapper objectMapper = new ObjectMapper();
		String ae = "";
		String cnt = "";
		String value = "";
		try {
			JsonNode payloadJson = objectMapper.readTree(payload);
			ae = payloadJson.get("pc").get("m2m:sgn").get("sur").asText().split("/")[1];
			cnt = payloadJson.get("pc").get("m2m:sgn").get("sur").asText().split("/")[2];
			if (payloadJson.get("pc").get("m2m:sgn").get("nev").get("rep").get("m2m:cin").has("con")) {
				value = payloadJson.get("pc").get("m2m:sgn").get("nev").get("rep").get("m2m:cin").get("con").asText();
			}
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		Action action = new Action();
		KieSession kieSession = new DroolsConfig().getKieSession();
		kieSession.setGlobal("action", action);
		kieSession.insert(new Condition(ae, cnt, value));
		kieSession.fireAllRules();
		kieSession.dispose();
		System.out.println("action : ");
		System.out.println(action.getDataTemplate());
		String data = action.getDataTemplate();
		if (data.contains("{{." + cnt + "}}")) {
			value = value.replace("\n", "\\n");
			data = data.replace("{{." + cnt + "}}", value);
		}
		System.out.println(action.getUrl());
		System.out.println("data :");
		System.out.println(data);
		// action request
		String result = new MyHttpClient().requestPostString("http://192.168.0.3:8081" + action.getUrl(), data);
		System.out.println(result);
	}
}

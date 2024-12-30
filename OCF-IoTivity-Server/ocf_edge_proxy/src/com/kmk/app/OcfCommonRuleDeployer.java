package com.kmk.app;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.iotivity.OCMethod;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kmk.app.MyOcfClient.ResponseVo;

public class OcfCommonRuleDeployer extends HttpServlet {
	public static String L = "-------------------------------";
	private static final long serialVersionUID = 1L;
	private String uri = "coap://192.168.0.3:12345";
	ResponseVo responseVo;
	String rule;
	String scenevalue;
	String sceneproperty;
	boolean ruleenable;
	boolean actionenable;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		rule = "";
		scenevalue = "";
		sceneproperty = "";
		ruleenable = true;
		actionenable = true;
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = null;
		try {
			jsonNode = objectMapper.readTree(req.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(jsonNode);

		JsonNode conditions = jsonNode.get("conditions");
		for (JsonNode condition : conditions) {
			rule += (condition.get("opt").asText().equals("or") ? " | " : " & ") + condition.get("parameter").asText()
					+ condition.get("operator").asText() + condition.get("value");
		}
		rule = rule.substring(3, rule.length());

		JsonNode actions = jsonNode.get("actions");
		scenevalue = actions.get(0).get("url").asText();
		sceneproperty = actions.get(0).get("command").asText();
		ruleenable = jsonNode.get("ruleenable") == null ? true : jsonNode.get("ruleenable").asBoolean();
		actionenable = jsonNode.get("actionenable") == null ? true : jsonNode.get("actionenable").asBoolean();
		System.out.println(rule);
		System.out.println(scenevalue);
		System.out.println(sceneproperty);
		System.out.println(ruleenable);
		System.out.println(actionenable);

		new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println(MyOcfClient.L + "/ruleaction");
				ObjectNode ruleactionPayload = objectMapper.createObjectNode();
				ruleactionPayload.put("scenevalue", scenevalue);
				ruleactionPayload.put("sceneproperty", sceneproperty);
				System.out.println(ruleactionPayload.toPrettyString());
				responseVo = new MyOcfClient().doReqeust(uri, "/ruleaction", "", ruleactionPayload.toString(), OCMethod.OC_POST);
				System.out.println(MyOcfClient.L + "code : " + responseVo.getCode());
				System.out.println(MyOcfClient.L + "payload : " + responseVo.getResultJsonStr());

				System.out.println(MyOcfClient.L + "/ruleexpression");
				ObjectNode ruleExpressionPayload = objectMapper.createObjectNode();
				ruleExpressionPayload.put("rule", rule);
				ruleExpressionPayload.put("ruleenable", ruleenable);
				ruleExpressionPayload.put("actionenable", actionenable);
				System.out.println(ruleExpressionPayload.toPrettyString());
				responseVo = new MyOcfClient().doReqeust(uri, "/ruleexpression", "", ruleExpressionPayload.toString(), OCMethod.OC_POST);
				System.out.println(MyOcfClient.L + "code : " + responseVo.getCode());
				System.out.println(MyOcfClient.L + "payload : " + responseVo.getResultJsonStr());
			}
		}).start();
		resp.setContentType("text/plain; charset=UTF-8");
		resp.setStatus(HttpStatus.OK_200);
	}
}
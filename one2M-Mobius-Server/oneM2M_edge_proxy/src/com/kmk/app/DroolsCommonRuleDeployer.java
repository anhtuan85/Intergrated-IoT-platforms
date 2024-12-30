package com.kmk.app;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DroolsCommonRuleDeployer extends HttpServlet {
	public static String L = "-------------------------------";
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = null;
		try {
			jsonNode = objectMapper.readTree(req.getInputStream());
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		System.out.println(jsonNode);

		String rule_id = jsonNode.get("id") != null ? jsonNode.get("id").asText() : "rule" + MyUtil.getCurrentTime();
		String rule_condition = "";
		JsonNode conditions = jsonNode.get("conditions");
		for (JsonNode condition : conditions) {
			rule_condition += (condition.get("opt").equals("or") ? " | " : " & ") + condition.get("parameter").asText()
					+ condition.get("operator").asText() + condition.get("value");
		}
		rule_condition = rule_condition.substring(3, rule_condition.length());

		JsonNode actions = jsonNode.get("actions");
		String rule_url = actions.get(0).get("url").asText();
		String rule_data = actions.get(0).get("command").asText();
		System.out.println(rule_id);
		System.out.println(rule_condition);
		System.out.println(rule_url);
		System.out.println(rule_data);
		rule_condition = rule_condition.replace("\"", "\\\"");
		rule_data = rule_data.replace("\"", "\\\"");

		String temp_rule = MyUtil.getBufferedString("temp_rule");
		temp_rule = temp_rule.replace("{{rule_id}}", rule_id);
		temp_rule = temp_rule.replace("{{rule_condition}}", rule_condition);
		temp_rule = temp_rule.replace("{{rule_url}}", rule_url);
		temp_rule = temp_rule.replace("{{rule_data}}", rule_data);

		System.out.println("Drools Common Rule Deployer : ");
		System.out.println(temp_rule);
		String responseStr = new MyHttpClient().requestPostString("http://192.168.0.3:8088/rule", temp_rule);
		resp.getWriter().println(responseStr);
		resp.setContentType("text/plain; charset=UTF-8");
		resp.setStatus(HttpStatus.OK_200);
	}
}
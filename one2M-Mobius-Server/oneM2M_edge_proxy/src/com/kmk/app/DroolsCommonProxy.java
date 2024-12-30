package com.kmk.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DroolsCommonProxy extends HttpServlet {
	public static String L = "-------------------------------";
	private static final long serialVersionUID = 1L;
	private String[] uriArr;

	public DroolsCommonProxy(String[] uriArr) {
		this.uriArr = uriArr;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		StringBuilder sb = new StringBuilder();
		for (int ch; (ch = req.getInputStream().read()) != -1;) {
			sb.append((char) ch);
		}
		String rule = sb.toString();
		String profile = translateDrools2Common(rule);
		System.out.println(profile);
		String result = "";
		for (String u : uriArr) {
			System.out.println(u);
			result += new MyHttpClient().requestPostJson(u, profile);
			System.out.println(MyUtil.L + result);
		}
		resp.getWriter().println(result);
		resp.setContentType("text/plain; charset=UTF-8");
		resp.setStatus(HttpStatus.OK_200);
	}

	private String translateDrools2Common(String rule) {
		String id = rule.split("rule \"")[1].split("when")[0].split("\"")[0];
		System.out.println("rule : " + id);
		System.out.println(rule);

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode profileObj = objectMapper.createObjectNode();
		profileObj.put("id", id);
		ArrayNode conditions = objectMapper.createArrayNode();
		rule = rule.replace("\\", "");
		String ruleTemp = rule.split("checkCondition\\(\"")[1].split("then")[0].split("\"\\)\\)")[0];
		String url = rule.split("setUrl\\(\"")[1].split("\"\\);")[0];
		String command = rule.split("setDataTemplate\\(\"")[1].split("\"\\);")[0];
		System.out.println("ruleTemp : " + ruleTemp);
		System.out.println("url : " + url);
		System.out.println("command : " + command);
		List<RuleExpression> ruleExpressionList = parse1(ruleTemp);
		for (RuleExpression ruleExpression : ruleExpressionList) {
			ObjectNode condition = objectMapper.createObjectNode();
			String parameter = ruleExpression.getRuleOperator().getKey();
			String operator = ruleExpression.getRuleOperator().getOperator();
			String value = ruleExpression.getRuleOperator().getValue().replace("\"", "");
			condition.put("parameter", parameter);
			condition.put("operator", operator);
			condition.put("value", value);
			condition.put("opt", ruleExpression.getAnd_or()); // and is necessary
			conditions.add(condition);
		}
		profileObj.set("conditions", conditions);

		ArrayNode actions = objectMapper.createArrayNode();
		ObjectNode action = objectMapper.createObjectNode();
		action.put("url", url);
		action.put("command", command);
		actions.add(action);
		profileObj.set("actions", actions);
		return profileObj.toString();
	}

	public List<RuleExpression> parse1(String ruleTemp) {
		List<RuleExpression> ruleExpressionList = new ArrayList<>();
		String tempAndArr[] = ruleTemp.split("&");
		if (tempAndArr[0].contains("|")) {
			String tempOrArr[] = tempAndArr[0].split("|");
			for (String temp : tempOrArr) {
				ruleExpressionList.add(new RuleExpression("or", parse2(temp)));
			}
		} else {
			ruleExpressionList.add(new RuleExpression("or", parse2(tempAndArr[0])));
		}

		for (int i = 1; i < tempAndArr.length; i++) {
			if (tempAndArr[i].contains("|")) {
				String tempOrArr[] = tempAndArr[i].split("|");
				ruleExpressionList.add(new RuleExpression("and", parse2(tempOrArr[0])));
				for (int j = 1; j < tempOrArr.length; j++) {
					ruleExpressionList.add(new RuleExpression("or", parse2(tempOrArr[j])));
				}
			} else {
				ruleExpressionList.add(new RuleExpression("and", parse2(tempAndArr[i])));
			}
		}
		return ruleExpressionList;
	}

	private RuleOperator parse2(String temp) {
		RuleOperator ruleOperator = new RuleOperator();
		if (temp.contains("!=")) {
			ruleOperator.setOperator("!=");
			ruleOperator.setKey(temp.split("!=")[0].trim());
			ruleOperator.setValue(temp.split("!=")[1].trim());
		} else if (temp.contains(">=")) {
			ruleOperator.setOperator(">=");
			ruleOperator.setKey(temp.split(">=")[0].trim());
			ruleOperator.setValue(temp.split(">=")[1].trim());
		} else if (temp.contains("<=")) {
			ruleOperator.setOperator("<=");
			ruleOperator.setKey(temp.split("<=")[0].trim());
			ruleOperator.setValue(temp.split("<=")[1].trim());
		} else if (temp.contains("=")) {
			ruleOperator.setOperator("=");
			ruleOperator.setKey(temp.split("=")[0].trim());
			ruleOperator.setValue(temp.split("=")[1].trim());
		} else if (temp.contains(">")) {
			ruleOperator.setOperator(">");
			ruleOperator.setKey(temp.split(">")[0].trim());
			ruleOperator.setValue(temp.split(">")[1].trim());
		} else if (temp.contains("<")) {
			ruleOperator.setOperator("<");
			ruleOperator.setKey(temp.split("<")[0].trim());
			ruleOperator.setValue(temp.split("<")[1].trim());
		}
		return ruleOperator;
	}

	class RuleExpression {
		private String and_or;
		private RuleOperator ruleOperator;

		public RuleExpression(String and_or, RuleOperator ruleOperator) {
			this.and_or = and_or;
			this.ruleOperator = ruleOperator;
		}

		public String getAnd_or() {
			return and_or;
		}

		public void setAnd_or(String and_or) {
			this.and_or = and_or;
		}

		public RuleOperator getRuleOperator() {
			return ruleOperator;
		}

		public void setRuleOperator(RuleOperator ruleOperator) {
			this.ruleOperator = ruleOperator;
		}
	}

	class RuleOperator {
		private String operator;
		private String key;
		private String value;

		public String getOperator() {
			return operator;
		}

		public void setOperator(String operator) {
			this.operator = operator;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}
	}
}
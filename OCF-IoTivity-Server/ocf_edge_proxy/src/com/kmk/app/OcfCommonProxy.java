package com.kmk.app;

import java.util.ArrayList;
import java.util.List;
import org.iotivity.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OcfCommonProxy implements OCMainInitHandler {
	private String uri;
	private String[] uriArr;

	public OcfCommonProxy() {
	}

	public OcfCommonProxy(String uri, String[] uriArr) {
		this.uri = uri;
		this.uriArr = uriArr;
	}

	@Override
	public int initialize() {
		System.out.println("inside initialize()");
		int ret = OCMain.initPlatform("jnu");
		ret |= OCMain.addDevice("/oic/d", "oic.d.iot", "app", "ocf.0.0.0", "ocf.res.0.0.0");
		return ret;
	}

	@Override
	public void registerResources() {
		System.out.println("inside MyInitHandler.registerResources()");
		OCResource resource = OCMain.newResource("common", uri, (short) 1, 0); // name, URI, no of types, device index
		OCMain.resourceBindResourceType(resource, "oic.r.common");
		OCMain.resourceBindResourceInterface(resource, OCInterfaceMask.RW);
		OCMain.resourceSetDefaultInterface(resource, OCInterfaceMask.RW);
		OCMain.resourceSetDiscoverable(resource, true);
		OCMain.resourceSetRequestHandler(resource, OCMethod.OC_POST, new PostHandler());
		OCMain.addResource(resource);

		RuleServer ruleServer = new RuleServer();
		ruleServer.ruleInit();
		ruleServer.registerResources();
		ActionResource actionResource = new ActionResource();
		actionResource.registerAction();
	}

	@Override
	public void requestEntry() {
		System.out.println("inside MyInitHandler.requestEntry()");
	}

	class PostHandler implements OCRequestHandler {
		@Override
		public void handler(OCRequest request, int interfaces) {
			System.out.println("Inside the POST RequestHandler");
			String id = null;
			String rule = null;
			String ra_lastscene = null;
			String lastscene_property = null;
			boolean ruleenable = false;
			boolean actionenable = false;
			OCRepresentation rep = request.getRequestPayload();
			while (rep != null) {
				if (rep.getType() == OCType.OC_REP_STRING && rep.getName().equals("id")) {
					id = rep.getValue().getString();
				} else if (rep.getType() == OCType.OC_REP_STRING && rep.getName().equals("scenevalue")) {
					ra_lastscene = rep.getValue().getString();
				} else if (rep.getType() == OCType.OC_REP_STRING && rep.getName().equals("sceneproperty")) {
					lastscene_property = rep.getValue().getString();
				} else if (rep.getType() == OCType.OC_REP_STRING && rep.getName().equals("rule")) {
					rule = rep.getValue().getString();
				} else if (rep.getType() == OCType.OC_REP_BOOL && rep.getName().equals("ruleenable")) {
					ruleenable = rep.getValue().getBool();
				} else if (rep.getType() == OCType.OC_REP_BOOL && rep.getName().equals("actionenable")) {
					actionenable = rep.getValue().getBool();
				}
				rep = rep.getNext();
			}
			System.out.println(id);
			System.out.println(rule);
			System.out.println(ra_lastscene);
			System.out.println(lastscene_property);
			System.out.println(ruleenable);
			System.out.println(actionenable);

			String profile = translateOcf2Common(id, rule, ra_lastscene, lastscene_property, ruleenable, actionenable);
			System.out.println(profile);
			String result = "";
			for (String u : uriArr) {
				System.out.println(MyUtil.L + u);
				result += new MyHttpClient().requestPostJson(u, profile);
				System.out.println(MyUtil.L + result);
			}

			CborEncoder root = OCRep.beginRootObject();
			OCRep.setTextString(root, "result", result);
			OCRep.endRootObject();
			OCMain.sendResponse(request, OCStatus.OC_STATUS_CHANGED);
		}
	}

	private String translateOcf2Common(String id, String rule, String ra_lastscene, String lastscene_property, boolean ruleenable,
			boolean actionenable) {
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode profileObj = objectMapper.createObjectNode();
		profileObj.put("id", id);
		ArrayNode conditions = objectMapper.createArrayNode();
		List<RuleExpression> ruleExpressionList = parse1(rule);
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
		action.put("url", ra_lastscene);
		action.put("command", lastscene_property.replace("\n", "").replace("\t", ""));
		actions.add(action);
		profileObj.set("actions", actions);
		profileObj.put("ruleenable", ruleenable);
		profileObj.put("actionenable", actionenable);
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

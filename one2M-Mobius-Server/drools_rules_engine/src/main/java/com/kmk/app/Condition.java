package com.kmk.app;

import java.util.ArrayList;
import java.util.List;

public class Condition {
	public String ae;
	public String cnt;
	public String value;

	public Condition(String ae, String cnt, String value) {
		this.ae = ae;
		this.cnt = cnt;
		this.value = value;
	}

	public boolean checkCondition(String condition) {
		System.out.println(condition);
		System.out.println(ae);
		System.out.println(cnt);
		System.out.println(value);

		boolean result = ruleEval(condition, ae, cnt, value);
		System.out.println("ruleEval : " + result);
		return result;
	}

	private boolean ruleEval(String rule, String deviceName, String resourceName, String sensorValue) {
		boolean result = false;
		System.out.println("ruleEval : ");
		System.out.println(rule);
		List<RuleExpression> ruleExpressionList = parse1(rule);
		RuleExpression deviceEx = ruleExpressionList.get(0);
		if (deviceEx.getRuleOperator().getKey().equals("meta(deviceName)") && deviceEx.getRuleOperator().getValue().replace("\"", "").equals(deviceName)) {
			ruleExpressionList.remove(deviceEx);
			result = true;
		} else {
			return result;
		}

		boolean oresult = false;
		for (RuleExpression ruleExpression : ruleExpressionList) {
			String parameter = ruleExpression.getRuleOperator().getKey();
			String operator = ruleExpression.getRuleOperator().getOperator();
			String value = ruleExpression.getRuleOperator().getValue().replace("\"", "");
			String opt = ruleExpression.getAnd_or();
			if (parameter.equals(resourceName)) {
				if (operator.equals("=")) {
					if (sensorValue.equals(value)) {
						oresult = true;
					}
				} else if (operator.equals("!=")) {
					if (!sensorValue.equals(value)) {
						oresult = true;
					}
				} else if (operator.equals(">")) {
					if (sensorValue.compareTo(value) > 0) {
						oresult = true;
					}
				} else if (operator.equals("<")) {
					if (sensorValue.compareTo(value) < 0) {
						oresult = true;
					}
				} else if (operator.equals(">=")) {
					if (sensorValue.compareTo(value) > 0 || sensorValue.equals(value)) {
						oresult = true;
					}
				} else if (operator.equals("<=")) {
					if (sensorValue.compareTo(value) < 0 || sensorValue.equals(value)) {
						oresult = true;
					}
				}
			}
			if (result == false && opt.equals("or") && oresult == true) {
				result = true;
			} else if (result == true && opt.equals("or") && oresult == true) {
				result = true;
			} else if (result == true && opt.equals("or") && oresult == false) {
				result = true;
			} else if (result == false && opt.equals("or") && oresult == false) {
				result = false;
			} else if (result == false && opt.equals("and") && oresult == true) {
				result = false;
			} else if (result == true && opt.equals("and") && oresult == true) {
				result = true;
			} else if (result == true && opt.equals("and") && oresult == false) {
				result = false;
			} else if (result == true && opt.equals("and") && oresult == false) {
				result = false;
			}
		}
		return result;
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

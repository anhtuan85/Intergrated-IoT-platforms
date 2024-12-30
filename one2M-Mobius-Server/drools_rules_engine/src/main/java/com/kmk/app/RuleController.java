package com.kmk.app;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RuleController {
	@PostMapping(value = "/rule")
	public String rule(HttpServletRequest req) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (int ch; (ch = req.getInputStream().read()) != -1;) {
			sb.append((char) ch);
		}
		String rule = sb.toString();
		String ruleName = rule.split("rule \"")[1].split("when")[0].split("\"")[0];
		ruleName = "src/main/resources/rules/" + ruleName + ".drl";
		System.out.println("rule :");
		System.out.println(ruleName);
		System.out.println(rule);
		MyUtil.setBufferedString(ruleName, rule);
		DroolsConfig.ruleName = ruleName;
		return ruleName;
	}
}
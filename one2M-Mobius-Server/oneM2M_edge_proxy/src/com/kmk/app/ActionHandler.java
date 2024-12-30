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

public class ActionHandler extends HttpServlet {
	public static String L = "-------------------------------";
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = null;
		try {
			StringBuilder sb = new StringBuilder();
			for (int ch; (ch = req.getInputStream().read()) != -1;) {
				sb.append((char) ch);
			}
			System.out.println(sb.toString());
			String dataJson = sb.toString().replace("\n", "\\n");
			jsonNode = objectMapper.readTree(dataJson);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		String data = jsonNode.get("data").asText();
		System.out.println(data);
		String query = req.getQueryString();
		System.out.println("query : " + query);
		String result = new MyHttpClient().requestPostString("http://127.0.0.1:5000/dopredictiondnn?" + query, data);
		System.out.println("action result : ");
		System.out.println(result);
		resp.setContentType("text/plain; charset=UTF-8");
		resp.setStatus(HttpStatus.OK_200);
	}
}
package com.kmk.app;

import org.iotivity.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ActionResource {
	public void registerAction() {
		System.out.println(MyUtil.L + "registerAction()");
		OCResource resource = OCMain.newResource("action", "/action", (short) 1, 0); // name, URI, no of types, device index
		OCMain.resourceBindResourceType(resource, "oic.r.app");
		OCMain.resourceBindResourceInterface(resource, OCInterfaceMask.RW);
		OCMain.resourceSetDefaultInterface(resource, OCInterfaceMask.RW);
		OCMain.resourceSetDiscoverable(resource, true);
		OCMain.resourceSetRequestHandler(resource, OCMethod.OC_POST, new OCRequestHandler() {
			@Override
			public void handler(OCRequest request, int interfaces) {
				System.out.println(MyUtil.L + "Inside the ActionResource POST RequestHandler");
				String payload = OCRep.toJSON(request.getRequestPayload(), false);// rep, pretty_print
				payload = payload.replace("\n", "\\n");
				String query = request.getQuery().substring(0, (int) request.getQueryLen());
				System.out.println(payload);
				System.out.println(query);
				ObjectMapper objectMapper = new ObjectMapper();
				String data = null;
				try {
					JsonNode jsonNode = objectMapper.readTree(payload);
					data = jsonNode.get("data").asText();
					System.out.println(MyUtil.L + "data : ");
					System.out.println(data);
					//String result = new MyHttpClient().requestPostString("http://127.0.0.1:5000/dopredictiondnn?" + query, data);
					
					
//					String result = new MyHttpClient().requestPostString("http://127.0.0.1:5000/dopredictiondnn?" + query, data);
//					System.out.println("action result : ");
//					System.out.println(result);
				} catch (JsonProcessingException e) {
					System.out.println("*******************************************TRY***********************");
					e.printStackTrace();
				}
				OCMain.sendResponse(request, OCStatus.OC_STATUS_CHANGED);
			}
		});
		OCMain.addResource(resource);
	}
}

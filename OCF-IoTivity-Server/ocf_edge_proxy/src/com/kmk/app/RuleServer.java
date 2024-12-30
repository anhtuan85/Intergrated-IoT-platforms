package com.kmk.app;

import java.util.ArrayList;
import java.util.List;
import org.iotivity.*;

import com.kmk.app.MyOcfClient.ResponseVo;
import com.kmk.app.OcfCommonProxy.RuleExpression;

public class RuleServer implements OCMainInitHandler {
	public static String L = "-------------------------------";
	public String deviceName = "device01";
	public String resourceName = "data";

	/* global property variables for path: "/sensor" */
	private String sensor_value;
	/* global property variables for path: "/actuator" */
	private String actuator_value;
	/* global property variables for path: /ruleaction and /scenecollection */
	private String lastscene;
	private String ra_lastscene;
	private String[] scenevalues;

	/* global property variables for path: /ruleexpression */
	private boolean ruleresult;
	private boolean ruleenable;
	private boolean actionenable;
	private String rule;
	private String sceneproperty;

	/* Resource handles */
	private OCResource res_sensor;
	private OCResource res_actuator;
	private OCResource res_scenemember1;
	private OCResource res_scenecol1;
	private OCResource res_scenelist;
	private OCResource res_ruleexpression;
	private OCResource res_ruleaction;
	private OCResource res_ruleinputcol;
	private OCResource res_ruleactioncol;
	private OCResource res_rule;

	private List<Scene> slist;

	// struct scenemappings_t
	private class Scene {
		private String scene;
		private String key;
		private String value;

		public String getScene() {
			return scene;
		}

		public void setScene(String scene) {
			this.scene = scene;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	@Override
	public int initialize() {
//		oc_activate_interrupt_handler(toggle_switch);//FIXME
		System.out.println("inside initialize()");
		int ret = OCMain.initPlatform("jnu");
		ret |= OCMain.addDevice("/oic/d", "oic.d.iot", "app", "ocf.0.0.0", "ocf.res.0.0.0");

		ruleInit();

		// Ignore IDD
		return ret;
	}

	public void ruleInit() {
		sensor_value = "";
		actuator_value = "";

		lastscene = "normalaudio";
		ra_lastscene = "loudaudio";
		scenevalues = new String[] { lastscene, ra_lastscene };

		ruleresult = false;
		ruleenable = false;
		actionenable = false;
		rule = "(switch:value = true)";

		slist = new ArrayList<Scene>();
		Scene sceneNormalAudio = new Scene();
		sceneNormalAudio.setScene("normalaudio");
		sceneNormalAudio.setKey("volume");
//		sceneNormalAudio.setValue(40);
		slist.add(sceneNormalAudio);
		Scene sceneloudAudio = new Scene();
		sceneloudAudio.setScene("loudaudio");
		sceneloudAudio.setKey("volume");
//		sceneloudAudio.setValue(60);
		sceneNormalAudio.setValue("");
		slist.add(sceneloudAudio);
	}

	@Override
	public void requestEntry() {
		System.out.println("inside OcfHandler.requestEntry()");
	}

	@Override
	public void registerResources() {
		registerSensor();
		registerActuator();
		registerSceneMember1();
		registerSceneCollection1();
		registerSceneList();
		registerRuleExpression();
		registerRuleAction();
		registerRuleInputCollection();
		registerRuleActionCollection();
		registerRuleCollection();
	}

	private void registerSensor() {
		System.out.println(L + "Register Resource with local path \"/sensor\"");
		res_sensor = OCMain.newResource("Sensor", "/" + resourceName, (short) 1, 0);
		OCMain.resourceBindResourceType(res_sensor, "oic.r.sensor");
		OCMain.resourceBindResourceInterface(res_sensor, OCInterfaceMask.A);
		OCMain.resourceSetDefaultInterface(res_sensor, OCInterfaceMask.A);
		OCMain.resourceSetDiscoverable(res_sensor, true);
		OCMain.resourceSetPeriodicObservable(res_sensor, 1);
		OCMain.resourceSetRequestHandler(res_sensor, OCMethod.OC_GET, new OCRequestHandler() {
			@Override
			public void handler(OCRequest request, int interfaces) {
				// get_sensor
				System.out.println(L + "get_sensor: interface " + interfaces);
				CborEncoder root = OCRep.beginRootObject();
				switch (interfaces) {
				case OCInterfaceMask.BASELINE: {
					OCMain.processBaselineInterface(request.getResource());
				}
				case OCInterfaceMask.RW: {
					OCRep.setTextString(root, "data", sensor_value);
					System.out.println(L + "data : " + sensor_value);
					break;
				}
				default:
					break;
				}
				OCRep.endRootObject();
				OCMain.sendResponse(request, OCStatus.OC_STATUS_OK);
			}
		});
		OCMain.resourceSetRequestHandler(res_sensor, OCMethod.OC_POST, new OCRequestHandler() {
			@Override
			public void handler(OCRequest request, int interfaces) {
				// post_sensor
				System.out.println(L + "post_sensor:");
				boolean error_state = false;
				OCRepresentation rep = request.getRequestPayload();
				/* loop over the request document to check if all inputs are ok */
				while (rep != null) {
					System.out.println(L + "key: (check) " + rep.getName());
					if (rep.getName().equals("value")) {
						/* property "value" of type boolean exist in payload */
						if (rep.getType() != OCType.OC_REP_STRING) {
							error_state = true;
							System.out.println(L + "   property 'value' is not of type bool " + rep.getType());
						}
					}
					rep = rep.getNext();
				}
				/*
				 * if the input is ok, then process the input document and assign the global
				 * variables
				 */
				if (error_state == false) {
					/* loop over all the properties in the input document */
					rep = request.getRequestPayload();
					while (rep != null) {
						System.out.println(L + "key: (assign) " + rep.getName());
						/* no error: assign the variables */
						if (rep.getName().equals("value")) {
							/* assign "value" */
							sensor_value = rep.getValue().getString();
							System.out.println(L + "sensor_value :");
							System.out.println(sensor_value);
						}
						rep = rep.getNext();
					}
					/* set the response */
					System.out.println("Set response ");

//					CborEncoder root = OCRep.beginRootObject();
//					OCRep.setTextString(root, "data", sensor_value);
//
//					OCRep.endRootObject();
					OCMain.sendResponse(request, OCStatus.OC_STATUS_CHANGED);
					rule_notify_and_eval();
				} else {
					OCMain.sendResponse(request, OCStatus.OC_STATUS_NOT_MODIFIED);
				}
			}
		});
		OCMain.addResource(res_sensor);
	}

	private void registerActuator() {
		System.out.println(L + "Register Resource with local path \"/actuator\"");
		res_actuator = OCMain.newResource("Actuator", "/actuator", (short) 1, 0);
		OCMain.resourceBindResourceType(res_actuator, "oic.r.actuator");
		OCMain.resourceBindResourceInterface(res_actuator, OCInterfaceMask.A);
		OCMain.resourceSetDefaultInterface(res_actuator, OCInterfaceMask.A);
		OCMain.resourceSetDiscoverable(res_actuator, true);
		OCMain.resourceSetPeriodicObservable(res_actuator, 1);
		OCMain.resourceSetRequestHandler(res_actuator, OCMethod.OC_GET, new OCRequestHandler() {
			@Override
			public void handler(OCRequest request, int interfaces) {
				// get_actuator
				System.out.println(L + "get_actuator: interface " + interfaces);
				CborEncoder root = OCRep.beginRootObject();
				switch (interfaces) {
				case OCInterfaceMask.BASELINE: {
					OCMain.processBaselineInterface(request.getResource());
				}
				case OCInterfaceMask.RW: {
					OCRep.setTextString(root, "value", actuator_value);
					break;
				}
				default:
					break;
				}
				OCRep.endRootObject();
				OCMain.sendResponse(request, OCStatus.OC_STATUS_OK);
			}
		});
		OCMain.resourceSetRequestHandler(res_actuator, OCMethod.OC_POST, new OCRequestHandler() {
			@Override
			public void handler(OCRequest request, int interfaces) {
				// post_audio
				System.out.println(L + "post_audio:");
				OCRepresentation rep = request.getRequestPayload();
				while (rep != null) {
					switch (rep.getType()) {
					case OC_REP_STRING:
						actuator_value = rep.getValue().getString();
						System.out.println(L + "value: " + actuator_value);
						break;
					default:
						OCMain.sendResponse(request, OCStatus.OC_STATUS_BAD_REQUEST);
						return;
					}
					rep = rep.getNext();
				}
				OCMain.sendResponse(request, OCStatus.OC_STATUS_CHANGED);
			}
		});
		OCMain.addResource(res_actuator);
	}

	private void registerSceneMember1() {
		System.out.println(L + "Register Resource with local path \"/scenemember1\"");
		res_scenemember1 = OCMain.newResource("Scene Member 1", "/scenemember1", (short) 1, 0);
		OCMain.resourceBindResourceType(res_scenemember1, "oic.wk.scenemember");
		OCMain.resourceBindResourceInterface(res_scenemember1, OCInterfaceMask.A);
		OCMain.resourceSetDefaultInterface(res_scenemember1, OCInterfaceMask.A);
		OCMain.resourceSetDiscoverable(res_scenemember1, true);
		OCMain.resourceSetPeriodicObservable(res_scenemember1, 1);
		OCMain.resourceSetRequestHandler(res_scenemember1, OCMethod.OC_GET, new OCRequestHandler() {
			@Override
			public void handler(OCRequest request, int interfaces) {
				// get_scenemember
				System.out.println(L + "get_scenemember: interface " + interfaces);
				CborEncoder root = OCRep.beginRootObject();
				switch (interfaces) {
				case OCInterfaceMask.BASELINE: {
					OCMain.processBaselineInterface(request.getResource());

					// "link" Property
					CborEncoder link = OCRep.openObject(root, "link");
					OCRep.setTextString(link, "href", res_actuator.getUri());
					OCRep.setStringArray(link, "rt", res_actuator.getTypes());
					OCCoreRes.encodeInterfacesMask(link, res_actuator.getInterfaces());
					OCRep.closeObject(root, link);

					// SceneMappings array
					CborEncoder sceneMappings = OCRep.openArray(root, "SceneMappings");
					for (Scene scene : slist) {
						CborEncoder item = OCRep.objectArrayBeginItem(sceneMappings);
						OCRep.setTextString(item, "scene", scene.getScene());
						OCRep.setTextString(item, "memberProperty", scene.getKey());
						OCRep.setTextString(item, "memberValue", "" + scene.getValue());
						OCRep.objectArrayEndItem(sceneMappings, item);
					}
					OCRep.closeArray(root, sceneMappings);
				}
				default:
					break;
				}
				OCRep.endRootObject();
				OCMain.sendResponse(request, OCStatus.OC_STATUS_OK);
			}
		});
		OCMain.addResource(res_scenemember1);
	}

	private void registerSceneCollection1() {
		System.out.println(L + "Register Collection with local path \"/scenecollection1\"");
		res_scenecol1 = OCMain.newCollection("Scene Collection 1", "/scenecollection1", (short) 1, 0);
		OCMain.resourceBindResourceType(res_scenecol1, "oic.wk.scenecollection");
		OCMain.resourceBindResourceInterface(res_scenecol1, OCInterfaceMask.BASELINE);
		OCMain.resourceSetDefaultInterface(res_scenecol1, OCInterfaceMask.LL);
		OCMain.resourceSetDiscoverable(res_scenecol1, true);

		OCLink linkScenemember1 = OCMain.newLink(res_scenemember1);
		OCMain.collectionAddLink(res_scenecol1, linkScenemember1);
		OCMain.collectionAddMandatoryResourceType(res_scenecol1, "oic.wk.scenemember");
		OCMain.collectionAddSupportedResourceType(res_scenecol1, "oic.wk.scenemember");

		OCMain.resourceSetPropertiesHandlers(res_scenecol1, new OCGetPropertiesHandler() {
			@Override
			public void handler(OCResource resource, int iface_mask) {
				// get_scenecol_properties
				System.out.println(L + "get_scenecol_properties");
				switch (iface_mask) {
				case OCInterfaceMask.BASELINE:
					CborEncoder root = OCRep.beginRootObject();
					OCRep.setTextString(root, "lastScene", lastscene);
					OCRep.setStringArray(root, "sceneValues", scenevalues);
					OCRep.endRootObject();
					break;
				default:
					break;
				}
			}
		}, new OCSetPropertiesHandler() {
			@Override
			public boolean handler(OCResource resource, OCRepresentation rep) {
				// set_scenecol_properties
				System.out.println(L + "set_scenecol_properties");
				while (rep != null) {
					switch (rep.getType()) {
					case OC_REP_STRING:
						if (rep.getName().equals("lastScene")) {
							boolean match = false;
							for (int i = 0; i < scenevalues.length; i++) {
								String scenevalue = scenevalues[i];
								if (scenevalue.equals(rep.getValue().getString())) {
									match = true;
									break;
								}
							}
							if (!match) {
								return false;
							}
							lastscene = rep.getValue().getString();
							OCMain.setDelayedHandler(new OCTriggerHandler() {
								@Override
								public OCEventCallbackResult handler() {
									set_scene();
									return OCEventCallbackResult.OC_EVENT_DONE;
								}
							}, 0);
						}
						break;
					default:
						break;
					}
					rep = rep.getNext();
				}
				return true;
			}
		});
		OCMain.addCollection(res_scenecol1);
	}

	private void registerSceneList() {
		System.out.println(L + "Register Collection with local path \"/scenelist\"");
		res_scenelist = OCMain.newCollection("Scene List", "/scenelist", (short) 1, 0);
		OCMain.resourceBindResourceType(res_scenelist, "oic.wk.scenelist");
		OCMain.resourceBindResourceInterface(res_scenelist, OCInterfaceMask.BASELINE);
		OCMain.resourceSetDefaultInterface(res_scenelist, OCInterfaceMask.LL);
		OCMain.resourceSetDiscoverable(res_scenelist, true);

		OCLink linkScenemember1 = OCMain.newLink(res_scenecol1);
		OCMain.collectionAddLink(res_scenelist, linkScenemember1);

		OCMain.collectionAddMandatoryResourceType(res_scenelist, "oic.wk.scenecollection");
		OCMain.collectionAddSupportedResourceType(res_scenelist, "oic.wk.scenecollection");

		OCMain.addCollection(res_scenelist);
	}

	private void registerRuleExpression() {
		System.out.println(L + "Register Resource with local path \"/ruleexpression\"");
		res_ruleexpression = OCMain.newResource("Rule Expression", "/ruleexpression", (short) 1, 0);
		OCMain.resourceBindResourceType(res_ruleexpression, "oic.r.rule.expression");
		OCMain.resourceBindResourceInterface(res_ruleexpression, OCInterfaceMask.RW);
		OCMain.resourceSetDefaultInterface(res_ruleexpression, OCInterfaceMask.RW);
		OCMain.resourceSetDiscoverable(res_ruleexpression, true);
		OCMain.resourceSetPeriodicObservable(res_ruleexpression, 1);
		OCMain.resourceSetRequestHandler(res_ruleexpression, OCMethod.OC_GET, new OCRequestHandler() {
			@Override
			public void handler(OCRequest request, int interfaces) {
				// get_ruleexpression
				System.out.println(L + "get_ruleexpression: interface " + interfaces);
				CborEncoder root = OCRep.beginRootObject();
				switch (interfaces) {
				case OCInterfaceMask.BASELINE: {
					OCMain.processBaselineInterface(request.getResource());
				}
				case OCInterfaceMask.RW: {
					OCRep.setBoolean(root, "ruleresult", ruleresult);
					OCRep.setBoolean(root, "ruleenable", ruleenable);
					OCRep.setBoolean(root, "actionenable", actionenable);
					OCRep.setTextString(root, "rule", rule);
					break;
				}
				default:
					break;
				}
				OCRep.endRootObject();
				OCMain.sendResponse(request, OCStatus.OC_STATUS_OK);
			}
		});
		OCMain.resourceSetRequestHandler(res_ruleexpression, OCMethod.OC_POST, new OCRequestHandler() {
			@Override
			public void handler(OCRequest request, int interfaces) {
				// post_ruleexpression
				System.out.println(L + "post_ruleexpression:");
				OCRepresentation rep = request.getRequestPayload();
				while (rep != null) {
					System.out.println(L + rep.getName() + " : ");
					switch (rep.getType()) {
					case OC_REP_BOOL:
						if (rep.getName().equals("ruleenable")) {
							ruleenable = rep.getValue().getBool();
							/* If the rule has been newly enabled evaluate the rule expression */
							if (ruleenable) {
								rule_notify_and_eval();
							}
						} else if (rep.getName().equals("actionenable")) {
							actionenable = rep.getValue().getBool();
						} else if (rep.getName().equals("ruleresult")) {
							/* Attempt to set the result, verify rule is disabled and actions are enabled */
							if (!ruleenable && actionenable) {
								ruleresult = rep.getValue().getBool();
								if (ruleresult) {
									perform_rule_action();
								}
							} else {
								// Invalid state for setting ruleresult by a client; fail the request
								OCMain.sendResponse(request, OCStatus.OC_STATUS_METHOD_NOT_ALLOWED);
								return;
							}
						}
						break;
					case OC_REP_STRING:
						if (rep.getName().equals("rule")) {
							rule = rep.getValue().getString();
						}
						break;
					default:
						OCMain.sendResponse(request, OCStatus.OC_STATUS_BAD_REQUEST);
						return;
					}
					rep = rep.getNext();
				}
				System.out.println(L + "rule expression : ");
				System.out.println("rule : ");
				System.out.println(rule);
				OCMain.sendResponse(request, OCStatus.OC_STATUS_CHANGED);
			}
		});
		OCMain.addResource(res_ruleexpression);
	}

	private void registerRuleAction() {
		System.out.println(L + "Register Resource with local path \"/ruleaction\"");
		res_ruleaction = OCMain.newResource("Rule Action", "/ruleaction", (short) 1, 0);
		OCMain.resourceBindResourceType(res_ruleaction, "oic.r.rule.action");
		OCMain.resourceBindResourceInterface(res_ruleaction, OCInterfaceMask.RW);
		OCMain.resourceSetDefaultInterface(res_ruleaction, OCInterfaceMask.RW);
		OCMain.resourceSetDiscoverable(res_ruleaction, true);
		OCMain.resourceSetPeriodicObservable(res_ruleaction, 1);
		OCMain.resourceSetRequestHandler(res_ruleaction, OCMethod.OC_GET, new OCRequestHandler() {
			@Override
			public void handler(OCRequest request, int interfaces) {
				// get_ruleaction
				System.out.println(L + "get_ruleaction: interface " + interfaces);
				CborEncoder root = OCRep.beginRootObject();
				switch (interfaces) {
				case OCInterfaceMask.BASELINE: {
					OCMain.processBaselineInterface(request.getResource());
				}
				case OCInterfaceMask.RW: {
					OCRep.setTextString(root, "scenevalue", ra_lastscene);
					CborEncoder link = OCRep.beginObject(root);
					OCRep.setTextString(link, "href", "/scenecollection1");
					OCRep.setStringArray(link, "rt", res_scenecol1.getTypes());
					OCCoreRes.encodeInterfacesMask(link, res_scenecol1.getInterfaces());
					OCRep.closeObject(root, link);
					break;
				}
				default:
					break;
				}
				OCRep.endRootObject();
				OCMain.sendResponse(request, OCStatus.OC_STATUS_OK);
			}
		});
		OCMain.resourceSetRequestHandler(res_ruleaction, OCMethod.OC_POST, new OCRequestHandler() {
			@Override
			public void handler(OCRequest request, int interfaces) {
				// post_ruleaction
				System.out.println(L + "post_ruleaction:");
				OCRepresentation rep = request.getRequestPayload();
				while (rep != null) {
					if (rep.getType() == OCType.OC_REP_STRING && rep.getName().equals("scenevalue")) {
						// TODO original spec states, must pre-define the scene list
//						boolean match = false;
//						for (int i = 0; i < scenevalues.length; i++) {
//							String scenevalue = scenevalues[i];
//							if (scenevalue.equals(rep.getValue().getString())) {
//								match = true;
//								break;
//							}
//						}
//						if (!match) {
//							OCMain.sendResponse(request, OCStatus.OC_STATUS_BAD_REQUEST);
//						}
						ra_lastscene = rep.getValue().getString();
					} else {
						OCMain.sendResponse(request, OCStatus.OC_STATUS_BAD_REQUEST);
					}

					if (rep.getType() == OCType.OC_REP_STRING && rep.getName().equals("sceneproperty")) {
						sceneproperty = rep.getValue().getString();
					}
					rep = rep.getNext();
				}
				System.out.println(L + "rue action : ");
				System.out.println("scenevalue : ");
				System.out.println(ra_lastscene);
				System.out.println("sceneproperty : ");
				System.out.println(sceneproperty);
				OCMain.sendResponse(request, OCStatus.OC_STATUS_CHANGED);
			}
		});
		OCMain.addResource(res_ruleaction);
	}

	private void registerRuleInputCollection() {
		System.out.println(L + "Register Collection with local path \"/ruleinputcollection\"");
		res_ruleinputcol = OCMain.newCollection("Rule Input Collection", "/ruleinputcollection", (short) 1, 0);
		OCMain.resourceBindResourceType(res_ruleinputcol, "oic.r.rule.inputcollection");
		OCMain.resourceBindResourceInterface(res_ruleinputcol, OCInterfaceMask.BASELINE);
		OCMain.resourceSetDefaultInterface(res_ruleinputcol, OCInterfaceMask.LL);
		OCMain.resourceSetDiscoverable(res_ruleinputcol, true);

		OCLink linkSensor = OCMain.newLink(res_sensor);
		linkSensor.setRel(new String[] { "ruleinput" });
		OCMain.linkAddLinkParameter(linkSensor, "anchor", "sensor");
		linkSensor.setInterfaces(OCInterfaceMask.A);
		OCMain.collectionAddLink(res_ruleinputcol, linkSensor);

		OCMain.collectionAddMandatoryResourceType(res_ruleinputcol, "oic.r.sensor");
		OCMain.collectionAddSupportedResourceType(res_ruleinputcol, "oic.r.sensor");

		OCMain.addCollection(res_ruleinputcol);
	}

	private void registerRuleActionCollection() {
		System.out.println(L + "Register Collection with local path \"/ruleactioncollection\"");
		res_ruleactioncol = OCMain.newCollection("Rule Action Collection", "/ruleactioncollection", (short) 1, 0);
		OCMain.resourceBindResourceType(res_ruleinputcol, "oic.r.rule.actioncollection");
		OCMain.resourceBindResourceInterface(res_ruleactioncol, OCInterfaceMask.BASELINE);
		OCMain.resourceSetDefaultInterface(res_ruleactioncol, OCInterfaceMask.LL);
		OCMain.resourceSetDiscoverable(res_ruleactioncol, true);

		OCLink linkRuleaction = OCMain.newLink(res_ruleaction);
		OCMain.collectionAddLink(res_ruleactioncol, linkRuleaction);

		OCMain.collectionAddMandatoryResourceType(res_ruleactioncol, "oic.r.rule.action");
		OCMain.collectionAddSupportedResourceType(res_ruleactioncol, "oic.r.rule.action");

		OCMain.addCollection(res_ruleactioncol);
	}

	private void registerRuleCollection() {
		System.out.println(L + "Register Collection with local path \"/rule\"");
		res_rule = OCMain.newCollection("Rule", "/rule", (short) 1, 0);
		OCMain.resourceBindResourceType(res_rule, "oic.r.rule");
		OCMain.resourceBindResourceInterface(res_rule, OCInterfaceMask.BASELINE);
		OCMain.resourceSetDefaultInterface(res_rule, OCInterfaceMask.LL);
		OCMain.resourceSetDiscoverable(res_rule, true);

		OCLink linkRuleexpression = OCMain.newLink(res_ruleexpression);
		OCMain.collectionAddLink(res_rule, linkRuleexpression);

		OCLink linkRuleinputcol = OCMain.newLink(res_ruleactioncol);
		OCMain.collectionAddLink(res_rule, linkRuleinputcol);

		OCLink linkRuleactioncol = OCMain.newLink(res_ruleactioncol);
		OCMain.collectionAddLink(res_rule, linkRuleactioncol);

		OCMain.collectionAddMandatoryResourceType(res_rule, "oic.r.rule.expression");
		OCMain.collectionAddMandatoryResourceType(res_rule, "oic.r.rule.inputcollection");
		OCMain.collectionAddMandatoryResourceType(res_rule, "oic.r.rule.actioncollection");

		OCMain.collectionAddSupportedResourceType(res_rule, "oic.r.rule.expression");
		OCMain.collectionAddSupportedResourceType(res_rule, "oic.r.rule.inputcollection");
		OCMain.collectionAddSupportedResourceType(res_rule, "oic.r.rule.actioncollection");

		OCMain.addCollection(res_rule);
	}

	private boolean ruleEval(String rule, String deviceName, String resourceName, String sensorValue) {
		boolean result = false;
		System.out.println("ruleEval : ");
		System.out.println(rule);
		List<RuleExpression> ruleExpressionList = new OcfCommonProxy().parse1(rule);
		RuleExpression deviceEx = ruleExpressionList.get(0);
		if (deviceEx.getRuleOperator().getKey().equals("meta(deviceName)")
				&& deviceEx.getRuleOperator().getValue().replace("\"", "").equals(deviceName)) {
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

	private void rule_notify_and_eval() {
		/*
		 * rule expression value has changed
		 */
		if (ruleenable) {
			/*
			 * rule is enabled
			 */
			if (ruleEval(rule, deviceName, resourceName, sensor_value)) {
				ruleresult = true;
			} else {
				ruleresult = false;
			}

			OCMain.notifyObservers(res_ruleexpression);

			if (actionenable && ruleresult) {
				perform_rule_action();
			}
		} else {
			ruleresult = false;
		}
	}

	private void perform_rule_action() {
		/*
		 * Set lastscene on the target scenecollection
		 */
		if (actionenable) {
			lastscene = ra_lastscene;
			// set_scene();
			System.out.println(L + "do action : ");
			System.out.println("scenevalue : ");
			System.out.println(lastscene);
			System.out.println("sceneproperty : ");
			System.out.println(sceneproperty);

			if (sceneproperty.contains("{{.data}}")) {
				sensor_value = sensor_value.replace("\n", "\\n");
				sceneproperty = sceneproperty.replace("{{.data}}", sensor_value);
			}

			System.out.println(L + "sceneproperty");
			System.out.println(sceneproperty);
			System.out.println(MyUtil.L + "action");
			String query = "?" + lastscene.split("\\?")[1];
			lastscene = lastscene.split("\\?")[0];
			System.out.println(lastscene);
			System.out.println(query);
			new Thread(new Runnable() {
				@Override
				public void run() {
					ResponseVo responseVo = new MyOcfClient().doReqeust("coap://192.168.0.3:12345", lastscene, query, sceneproperty,
							OCMethod.OC_POST);
					System.out.println(MyUtil.L + "code : " + responseVo.getCode());
					System.out.println(MyUtil.L + "payload : " + responseVo.getResultJsonStr());
				}
			}).start();
		}
	}

	private void set_scene() {
		for (Scene scene : slist) {
			if (scene.getScene().equals(lastscene)) {
				if (scene.getKey().equals("valoume")) {
					actuator_value = scene.getValue();
					OCMain.notifyObservers(res_actuator);
					break;
				}
			}
		}
	}
}

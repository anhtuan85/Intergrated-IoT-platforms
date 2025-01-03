/*
 * ------------------------------------------------------------------------
 * Copyright 2014 Korea Electronics Technology Institute
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ------------------------------------------------------------------------
 */

package kr.re.keti.ncube.httpserver;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class HttpServerParser {
	
	public static ArrayList<String> notificationParse(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource xmlSource = new InputSource();
		xmlSource.setCharacterStream(new StringReader(xml));
		Document document = builder.parse(xmlSource);
		
		String content = "";
		String subr = "";
		
		NodeList conNodeList = document.getElementsByTagName("con");
		if (conNodeList.getLength() > 0 && conNodeList.item(0).getChildNodes().getLength() > 0) {
			Node conNode = conNodeList.item(0).getChildNodes().item(0);
			content = conNode.getNodeValue();
		}
		NodeList surNodeList = document.getElementsByTagName("sur");
		if (surNodeList.getLength() > 0 && surNodeList.item(0).getChildNodes().getLength() > 0) {
			Node surNode = surNodeList.item(0).getChildNodes().item(0);
			subr = surNode.getNodeValue();
		}
		
		ArrayList<String> returnArray = new ArrayList<String>();
		returnArray.add(content);
		returnArray.add(subr);
		
		return returnArray;
	}
}
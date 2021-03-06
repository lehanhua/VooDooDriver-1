/*
Copyright 2011 SugarCRM Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License. 
You may may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0 
   
Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
Please see the License for the specific language governing permissions and 
limitations under the License.
*/

package voodoodriver;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SodaTypes {
	private Document doc = null;
	private SodaElementsList datatypes = null;
	
	public SodaTypes() {
		File testFD = null;
		DocumentBuilderFactory dbf = null;
		DocumentBuilder db = null;

		try {
			String className = this.getClass().getName().replace('.', '/');
			String classJar =  this.getClass().getResource("/" + className + ".class").toString();
			dbf = DocumentBuilderFactory.newInstance();
			db = dbf.newDocumentBuilder();
			
			if (classJar.startsWith("jar:")) {
				doc = db.parse(getClass().getResourceAsStream("SodaElements.xml"));
			} else {
				testFD = new File(getClass().getResource("SodaElements.xml").getFile());
				doc = db.parse(testFD);
			}
			
			datatypes = this.parse(doc.getDocumentElement().getChildNodes());
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}
	
	private SodaElementsList parse(NodeList node) {
		SodaElementsList dataList = null; 
		
		int len = 0;
		try {
			dataList = new SodaElementsList();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		
		len = node.getLength();
		for (int i = 0; i <= len -1; i++) {
			SodaHash data = new SodaHash();
			Node child = node.item(i);
			String name = child.getNodeName();
			
			if (name.startsWith("#") || name.contains("comment")) {
				continue;
			}
			
			data.put(name, 0);
			if (SodaElements.isMember(name.toUpperCase())) {
				data.put("type", SodaElements.valueOf(name.toUpperCase()));
				if (child.hasChildNodes()) {
					NodeList kids = child.getChildNodes();
					for (int x = 0; x <= kids.getLength() -1; x++) {
						Node tmp = kids.item(x);
						String kid_name = tmp.getNodeName();
						if (kid_name.contains("soda_attributes") || kid_name.contains("accessor_attributes")) {
							data.put(kid_name, parseAccessors(tmp.getChildNodes()));
						}
					}
				}
			} else {
				System.out.printf("(!)Error: Unknown type: '%s'!\n", name);
			}
			
			if (!data.isEmpty()) {
				dataList.add(data);
			}
		}
		
		return dataList;
	}
	
	private SodaHash parseAccessors(NodeList nodes) {
		SodaHash hash = new SodaHash();
		int len = nodes.getLength() -1;
		
		for (int i = 0; i <= len; i++) {
			String node_name = nodes.item(i).getNodeName();
			if (node_name == "#text") {
				continue;
			}
			
			if (node_name != "action") {
				String value = nodes.item(i).getTextContent();
				if (value.isEmpty() || value.startsWith("\n")) {
					continue;
				}
				hash.put(value, 0);
			} else {
				SodaHash act_hash = new SodaHash();
				NodeList actions = nodes.item(i).getChildNodes();
				int actlen = actions.getLength() -1;
				
				for(int x = 0; x <= actlen; x++) {
					String act = actions.item(x).getTextContent();
					String act_name = actions.item(x).getNodeName();
					
					if (act_name == "name") {
						act_hash.put(act_name, act);
					}
				}
				hash.put(node_name, act_hash);
			}
		}
		
		return hash;
	}
	
	public SodaElementsList getTypes() {
		return datatypes;
	}

	// this needs to be redone //
	public boolean isValid(String name) {
		boolean result = false;
		int len = datatypes.size() -1;
		
		for (int i = 0; i <= len; i++) {
			if (datatypes.get(i).containsKey(name)) {
				result = true;
				break;
			} else {
				result = false;
			}
		}
		
		return result;
	}
}

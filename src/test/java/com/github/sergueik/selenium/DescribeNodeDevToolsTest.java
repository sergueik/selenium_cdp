package com.github.sergueik.selenium;

/**
 * Copyright 2023,2024 Serguei Kouzmine
 */


import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v138.dom.DOM;
import org.openqa.selenium.devtools.v138.dom.DOM.EnableIncludeWhitespace;
import org.openqa.selenium.devtools.v138.dom.model.Node;
import org.openqa.selenium.devtools.v138.dom.model.NodeId;

import com.google.gson.Gson;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocument
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-querySelectorAll
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#type-Node
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-describeNode
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
public class DescribeNodeDevToolsTest extends BaseDevToolsTest {

	private static String baseURL = "https://www.wikipedia.org";
	private final String selector = "div.central-featured-lang[lang='ru']";

	private static Node result;
	private static Map<String, Object> results = new HashMap<>();
	private NodeId nodeId = null;
	private static List<NodeId> nodeIds = new ArrayList<>();

	private static List<String> attributes = new ArrayList<>();

	@After
	public void afterTest() {
		chromeDevTools.send(DOM.disable());
		driver.get("about:blank");
	}

	@Before
	public void beforeTest() {
		driver.get(baseURL);
		chromeDevTools.send(DOM.enable(Optional.of(EnableIncludeWhitespace.ALL)));
	}

	@Test
	public void test1() {
		try {
			result = chromeDevTools
					.send(DOM.getDocument(Optional.of(-1), Optional.of(true)));
			nodeId = result.getNodeId();
			result = chromeDevTools
					.send(DOM.describeNode(Optional.of(nodeId), Optional.empty(),
							Optional.empty(), Optional.of(-1), Optional.of(true)));
			assertThat(result.getDocumentURL(), notNullValue());
			System.err
					.println("document node:  " + new Gson().toJson(result, Node.class));

			nodeIds = chromeDevTools.send(DOM.querySelectorAll(nodeId, selector));
			for (NodeId nodeId : nodeIds) {
				attributes = chromeDevTools.send(DOM.getAttributes(nodeId));
				assertThat(attributes.size(), greaterThan(2));
				results = listToMap(attributes);
				assertThat(results, hasKey("lang"));
				String lang = (String) results.get("lang");
				System.err.println(String.format("lang: %s", lang));

				System.err.println(String.format("Id: %s", nodeId.toString()));
				System.err.println(chromeDevTools.send(DOM.getOuterHTML(Optional.of(nodeId),
						Optional.empty(), Optional.empty())));

				result = chromeDevTools
						.send(DOM.describeNode(Optional.of(nodeId), Optional.empty(),
								Optional.empty(), Optional.of(1), Optional.of(false)));

				System.err.println(
						"nodeId:  " + nodeId + ": " + new Gson().toJson(result, Node.class));

			}
		} catch (DevToolsException e) {
			System.err.println("Exception (rethrown) " + e.getMessage());
			throw e;
		}
	}

	// origin:
	// http://www.java2s.com/example/java/java.util/convert-array-to-map.html
	//
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <K, V> Map<K, V> listToMap(List<String> objects) {
		HashMap map = new HashMap();
		Object key = null;
		for (final String object : objects) {
			// System.err.println("processing " + object.toString());
			if (key == null) {
				key = object;
			} else {
				map.put(key, object);
				key = null;
			}
		}
		return map;
	}

}

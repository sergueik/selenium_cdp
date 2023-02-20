package com.github.sergueik.selenium;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;

import com.google.gson.JsonSyntaxException;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocument
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-performSearch
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-describeNode
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#type-Node
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class DescribeNodeCdpTest extends BaseCdpTest {

	private static String baseURL = "https://www.wikipedia.org";
	private static String selector = null;
	private static String command = null;

	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> result2 = new HashMap<>();
	private static List<Long> results = new ArrayList<>();
	private static List<String> attributes = new ArrayList<>();

	private static List<Map<String, Object>> results2 = new ArrayList<>();
	public static Long nodeId = (long) -1;

	@After
	public void afterTest() {
		command = "DOM.disable";
		result = driver.executeCdpCommand(command, new HashMap<String, Object>());
		driver.get("about:blank");
	}

	@Before
	public void beforeTest() {
		command = "DOM.enable";
		params = new HashMap<String, Object>();
		params.put("includeWhitespace", "all");
		result = driver.executeCdpCommand(command, params);
		// Arrange
		driver.get(baseURL);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void test1() {

		try {
			command = "DOM.getDocument";
			params = new HashMap<>();
			params.put("pierce", false);
			params.put("depth", -1);
			// Act
			result = driver.executeCdpCommand(command, params);
			assertThat(result, hasKey("root"));
			result2 = (Map<String, Object>) result.get("root");
			nodeId = Long.parseLong(result2.get("nodeId").toString());
			System.err
					.println(String.format("Found document root node id %d", nodeId));
			assertThat(result2, hasKey("childNodeCount"));
			System.err.println(
					String.format("Found %d child nodes", result2.get("childNodeCount")));
			assertThat(result2, hasKey("children"));
			results2 = (List<Map<String, Object>>) result2.get("children");
			results2.stream().forEach(o -> System.err.println(o.get("localName")));
			selector = "div.central-featured-lang";
			
			command = "DOM.querySelectorAll";
			params.clear();
			params.put("nodeId", nodeId);
			params.put("selector", selector);

			result = driver.executeCdpCommand(command, params);
			assertThat(result, hasKey("nodeIds"));
			results = (List<Long>) result.get("nodeIds");
			// Assert
			assertThat(results, notNullValue());
			assertTrue(results.size() != 0);
			System.err.println(String.format("Found %d Node Ids:", results.size()));
			results.forEach(nodeId -> {
				command = "DOM.describeNode";
				params.clear();
				params.put("nodeId", nodeId);
				params.put("pierce", true);
				params.put("depth", -1);

				result = driver.executeCdpCommand(command, params);
				assertThat(result, notNullValue());
				assertThat(result, hasKey("node"));
				result2 = (Map<String, Object>) result.get("node");
				assertTrue(result2.containsKey("nodeName"));

				assertTrue(result2.containsKey("attributes"));

				attributes = (List<String>) result2.get("attributes");
				assertThat(attributes.size(), greaterThan(1));

				result = listToMap(attributes);
				System.err.println(String.format("Id: %s\nName: %s", nodeId.toString(),
						result2.get("nodeName")));
				result.entrySet().forEach(o -> System.err.println(
						String.format("%s: %s", o.getKey(), o.getValue().toString())));

			});

		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			e.printStackTrace();
			throw (new RuntimeException(e));
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

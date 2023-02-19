package com.github.sergueik.selenium;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.devtools.v110.dom.DOM;

import com.google.gson.JsonSyntaxException;
import static org.junit.Assert.assertTrue;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocument
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getOuterHTML
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getAttributes
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#type-Node
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getContainerForNode
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class DOMNodeAttributesCdpTest extends BaseCdpTest {

	private static String baseURL = "https://www.wikipedia.org";
	private final String selector = "div.central-featured-lang[lang='ru']";

	private static String command = "DOM.getDocument";

	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> result = new HashMap<>();
	public static Long nodeId = (long) -1;

	@After
	public void afterTest() {
		driver.get("about:blank");
		command = "DOM.disable";
		result = driver.executeCdpCommand(command, new HashMap<String, Object>());
	}

	@Before
	public void beforeTest() {
		command = "DOM.enable";
		params = new HashMap<String, Object>();
		params.put("includeWhitespace", "all");
		result = driver.executeCdpCommand(command, params);
		driver.get(baseURL);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test1() {

		try {
			command = "DOM.getDocument";
			params = new HashMap<>();
			params.put("pierce", false);
			params.put("depth", 1);
			// Act
			result = driver.executeCdpCommand(command, params);
			nodeId = Long.parseLong(
					((Map<String, Object>) result.get("root")).get("nodeId").toString());
			System.err.println(String.format("nodeid: %d", nodeId));
			command = "DOM.querySelector";
			params.clear();

			params.put("nodeId", nodeId);
			params.put("selector", selector);

			result = driver.executeCdpCommand(command, params);

			assertThat(result, hasKey("nodeId"));
			nodeId = (Long) result.get("nodeId");
			System.err.println(String.format("nodeid: %d", nodeId));
			// NOTE: getContainerForNode returns nothing
			// command = "DOM.getContainerForNode";
			command = "DOM.getAttributes";
			params.clear();
			params.put("nodeId", nodeId);

			result = driver.executeCdpCommand(command, params);
			System.err.println(String.format("result: %s", result.toString()));
			assertThat(result, hasKey("attributes"));
			List<Object> attributes = (List<Object>) result.get("attributes");
			assertThat(attributes.size(), greaterThan(2));
			Map<Object, Object> result2 = listToMap(attributes);
			assertThat(result2, hasKey("lang"));
			String lang = (String) result2.get("lang");
			System.err.println(String.format("lang: %s", lang));

			command = "DOM.getOuterHTML";
			params.clear();
			params.put("nodeId", nodeId);
			result = driver.executeCdpCommand(command, params);
			assertThat(result, notNullValue());
			assertTrue(result.containsKey("outerHTML"));
			System.err.println(result.get("outerHTML"));
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
	public static <K, V> Map<K, V> listToMap(List<Object> objects) {
		HashMap map = new HashMap();
		Object key = null;
		for (final Object object : objects) {
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

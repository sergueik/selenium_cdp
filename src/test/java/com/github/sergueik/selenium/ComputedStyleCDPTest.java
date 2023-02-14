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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.google.gson.JsonSyntaxException;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocument
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getOuterHTML
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#type-Node
 * https://chromedevtools.github.io/devtools-protocol/tot/CSS/#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/CSS/#method-disable
 * https://chromedevtools.github.io/devtools-protocol/tot/CSS/#method-getComputedStyleForNode
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class ComputedStyleCDPTest extends BaseCdpTest {

	private static String baseURL = "https://getbootstrap.com/docs/4.0/components/buttons/";
	private final ArrayList<String> classes = new ArrayList<String>(
			Arrays.asList("btn-primary", "btn-secondary", "btn-success", "btn-danger",
					"btn-warning", "btn-info", "btn-light", "btn-dark", "btn-link"));

	private static String selector = null;

	private static String command = "DOM.getDocument";
	private WebElement element;

	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> result = new HashMap<>();
	private static List<Object> propArray = new ArrayList<>();
	public static Long nodeId = (long) -1;
	public static Long rootNodeId = (long) -1;
	private static String data;
	private boolean debug = false;

	@After
	public void afterTest() {
		driver.get("about:blank");
		command = "DOM.disable";
		driver.executeCdpCommand(command, new HashMap<String, Object>());
		command = "CSS.disable";
		driver.executeCdpCommand(command, new HashMap<String, Object>());
	}

	@Before
	public void beforeTest() {
		command = "DOM.enable";
		params = new HashMap<String, Object>();
		params.put("includeWhitespace", "all");
		driver.executeCdpCommand(command, params);
		command = "CSS.enable";
		driver.executeCdpCommand(command, new HashMap<String, Object>());
		driver.get(baseURL);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test1() {
		driver.get(baseURL);
		for (String data : classes) {
			selector = String.format("div.bd-example button.%s", data);
			element = driver.findElement(By.cssSelector(selector));
			assertThat(element, notNullValue());
			String value = styleOfElement(element, "background-color");

			System.err.println(
					element.getText() + " computed style: background-color: " + value);
		}

		try {
			command = "DOM.getDocument";
			params = new HashMap<>();
			params.put("pierce", false);
			params.put("depth", 1);
			// Act
			result = driver.executeCdpCommand(command, params);
			rootNodeId = Long.parseLong(
					((Map<String, Object>) result.get("root")).get("nodeId").toString());
			System.err.println(String.format("Root nodeid: %d", rootNodeId));

			for (String data : classes) {

				selector = String.format("div.bd-example button.%s", data);
				System.err.println(String.format("query Selector: \"%s\"", selector));
				params.clear();
				command = "DOM.querySelector";
				params.put("nodeId", rootNodeId);
				params.put("selector", selector);

				result = driver.executeCdpCommand(command, params);

				assertThat(result, hasKey("nodeId"));
				nodeId = (Long) result.get("nodeId");
				if (debug)
					System.err.println(String.format("result: nodeId=%s", nodeId));

				command = "DOM.getOuterHTML";
				params.clear();
				params.put("nodeId", nodeId);
				result = driver.executeCdpCommand(command, params);
				assertThat(result, notNullValue());
				assertTrue(result.containsKey("outerHTML"));
				System.err.println(result.get("outerHTML"));

				command = "CSS.getComputedStyleForNode";
				params.clear();
				params.put("nodeId", nodeId);
				result = driver.executeCdpCommand(command, params);
				assertThat(result, notNullValue());

				assertTrue(result.containsKey("computedStyle"));
				propArray = (List<Object>) result.get("computedStyle");

				assertThat(propArray.size(), greaterThan(1));
				propArray.stream().forEach(o -> {
					if (debug)
						System.err.println(String.format("element: %s", o.toString()));
					if (o.toString().contains("background-color")) {
						result = (Map<String, Object>) o;
						if (result.get("name").toString().contains("background-color"))
							System.err.println(
									String.format("computed style: %s", result.get("value")));
					}
				});

				command = "DOM.getOuterHTML";
				params.clear();
				params.put("nodeId", nodeId);
				result = driver.executeCdpCommand(command, params);
				assertThat(result, notNullValue());
				assertTrue(result.containsKey("outerHTML"));
				System.err.println(result.get("outerHTML"));
			}
		} catch (

		JsonSyntaxException e) {
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

	protected String styleOfElement(WebElement element, Object... arguments) {
		return (String) Utils.executeScript(Utils.getScriptContent("getStyle.js"),
				element, arguments);
	}

}
